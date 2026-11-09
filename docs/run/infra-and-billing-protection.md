# 인프라 구성 · 과금 방어

Firebase Spark(무료) 한 곳만 쓰는 구성, 각 흐름의 구현 포인트, 무료 한도를 다 썼을 때의 대응을 정리합니다.

> 데이즈는 Cloud Functions · Storage가 필요해 Blaze로 옮겼지만(2026-11), 런닝앱 v1은 서버 로직 · 파일 저장이 필요 없어 **Spark에 결제 수단 없이** 갑니다. 그래서 과금 자체가 일어날 수 없고, 방어의 목적은 "한도 소진으로 백업이 멈추는 것"을 막는 데 있습니다.

## 1. 전체 구성

```
[Android 앱]
   │
   ├─ Google 로그인 (Credential Manager) ──▶ Firebase Auth (Spark)
   ├─ 기록 백업 · 복원 ──────────────────────▶ Firestore (Spark)  users/{uid}/runs · goals
   ├─ 차단 스위치 · 업데이트 정책 ─────────────▶ Remote Config
   ├─ 위변조 앱 차단 ──────────────────────────▶ App Check (Play Integrity)
   └─ 크래시 수집 ────────────────────────────▶ Crashlytics

[Firebase Hosting (Spark)] 개인정보 처리방침 · 이용약관 · 위치정보 이용약관
```

지도 SDK, Cloud Functions, Cloud Storage, Cloudflare(Worker · R2 · Pages)는 쓰지 않습니다.

### 서비스별 역할과 무료 범위 (Spark)

| 서비스 | 역할 | 무료 범위 | 넘으면 |
|---|---|---|---|
| Firebase Auth | Google 로그인 | 기본 Authentication(Identity Platform 아님)은 일반 제공자 사용자 수 제한 없음 | – |
| Firestore | 백업 | 저장 1GiB, 읽기 5만 · 쓰기 2만 · 삭제 2만 / 일, 네트워크 전송 10GiB / 월 | 그날(월) 남은 시간 동안 요청 실패 |
| Remote Config | 차단 스위치, 업데이트 정책, 기능 플래그 | 무료 | – |
| App Check | 위변조 앱 차단 | App Check 자체는 무료. Play Integrity API에는 일일 호출 할당량이 있음 | 토큰 발급 실패 → Firestore 요청 거부 |
| Crashlytics | 크래시 | 무료 | – |
| Hosting | 정책 문서 | 저장 10GB, 전송 360MB / 일 | 그날 남은 시간 동안 실패 |

> 수치는 2026-11 기준입니다. 출시 전 Firebase 요금제 페이지에서 다시 확인합니다.

## 2. 흐름별 구현

### 2-1. Google 로그인

```
앱: Credential Manager로 Google ID 토큰 받기 (default_web_client_id)
  → GoogleAuthProvider.getCredential(idToken) → FirebaseAuth.signInWithCredential
  → 성공: uid 확보 → 약관 동의 기록 업로드(게스트 때 동의했으면 그 기록) → 게스트 기록 이전 확인 → Pull
```

- 로그인 실패(취소 · 네트워크)는 게스트 상태를 유지하고 스낵바만 띄웁니다.
- 탈퇴 전 재인증도 같은 흐름으로 새 ID 토큰을 받아 `reauthenticate`합니다.
- 운영 앱 · 출시 키의 SHA-1 · SHA-256을 Firebase에 등록해야 Google 로그인이 동작합니다 (Play 앱 서명 키 포함).

### 2-2. 백업 · 복원

`data-model-erd.md` 5장. 핵심만:

- 실시간 리스너 없음. Push는 저장할 때 Worker로, Pull은 로그인 직후 · 24시간마다 · 사용자가 복원을 누를 때.
- 쿼리는 `serverUpdatedAt > 커서`, 페이지 50개.
- 원시 GPS 점은 올리지 않고, 문서 하나에 요약 + 단순화 경로(최대 500점).

### 2-3. App Check

- 제공자: prod Play Integrity, dev 디버그 제공자(디버그 토큰을 콘솔에 등록).
- **Firestore에 강제(enforce)** 합니다. 강제 전에 콘솔의 App Check 지표에서 "확인됨" 비율이 충분히 높은지 1~2주 관찰한 뒤 켭니다.
- Play Integrity 일일 할당량을 넘지 않도록 App Check 토큰 유효 시간을 기본값보다 길게(예: 수 시간) 두는 것을 검토하고, 사용자가 늘면 할당량 증가를 신청합니다. 정확한 할당량은 Play Console에서 확인합니다.

### 2-4. 정책 문서 (Firebase Hosting)

- `policy-site/public/`에 `privacy.html` · `terms.html` · `location.html`, `firebase.json`의 `hosting`(`cleanUrls`).
- 주소: `https://{firebase-project-id}.web.app/{privacy|terms|location}`. Play 스토어 개인정보 처리방침 URL로 사용.
- 앱의 정책 문서 화면은 `RunWebView`(이 사이트만 열고, 다른 링크는 막음)로 엽니다.
- 테마 토큰 CSS(`docs/run/assets/theme-tokens.css`)를 복사해 앱과 같은 색으로 보이게 합니다 (`?theme=volt&mode=dark`).

### 2-5. Remote Config 키

| 키 | 기본값 | 용도 |
|---|---|---|
| `backup_enabled` | true | 백업 Push · Pull 전체 중단 (버그 · 한도 소진 시) |
| `force_update_min_version_code` | 1 | 강제 업데이트 기준 (`RELEASE_AND_UPDATE.md` 6장) |
| `soft_update_min_version_code` | 1 | 선택 업데이트 기준 |
| `soft_update_interval_days` | 3 | 선택 업데이트 안내 간격 |
| `feature_{name}_enabled` | false | 새 기능 플래그 (`roadmap.md` 5장) |

## 3. 과금 방어의 원칙

Spark에 결제 수단이 없으므로 **청구는 일어나지 않습니다.** 대신 한도를 넘으면 그날 모든 사용자의 백업이 멈추므로, 아래 겹으로 한도 소진을 막습니다.

| 층 | 방법 | 막는 대상 |
|---|---|---|
| 1. 결제 수단 없음 | Firebase · Google Cloud 어디에도 결제 계정을 연결하지 않음 | 청구 자체 |
| 2. 서버 검증 | 보안 규칙(본인만, 필드 · 길이 · 쿼리 limit 제한), App Check 강제 | 앱을 우회한 직접 호출, 남용 |
| 3. 앱 | 리스너 없음, 디바운스, 커서, 지수 백오프, 차단 스위치 | 내 코드의 버그 |
| 4. 모니터링 | Firebase 콘솔 사용량 주간 확인, 50% 도달 시 대응 시작 | 사용자 증가 |

### 앱에서만 막으면 안 되는 이유

Firebase 설정 값은 APK 안에 들어 있어 추출할 수 있고, 앱을 거치지 않고 Firestore를 직접 호출할 수 있습니다. 그래서 실제 방어는 보안 규칙과 App Check에서 합니다. 다만 한도 소진은 외부 공격보다 **무한 재시도 · 반복 쓰기 같은 내 버그**에서 더 자주 생기므로 앱 쪽 제한도 반드시 둡니다.

## 4. 플랫폼 설정

- **Blaze로 업그레이드하지 않습니다.** 콘솔에서 Functions · Storage · Extensions · Firestore TTL처럼 Blaze를 요구하는 기능을 켜려 하면 업그레이드를 요구하므로, 그 화면에서 진행하지 않습니다.
- **Identity Platform으로 업그레이드하지 않습니다** (Auth 설정 화면의 업그레이드 제안).
- 같은 Google Cloud 프로젝트에 다른 유료 API(지도 등)를 켜지 않습니다. 결제 계정 연결을 요구하며, 연결하면 Firebase도 Blaze가 됩니다.
- Firebase · Google 계정 2단계 인증.

## 5. 앱 쪽 방어 (내 버그 대비)

1. **리스너 없음**: v1은 `addSnapshotListener`를 쓰지 않습니다. 쓰게 되면 이유 · 해제 시점 · 최악의 읽기 수를 PR에 적습니다.
2. **쓰기 디바운스**: 메모 입력은 멈춘 뒤 저장, 백업 Push는 5초 디바운스 고유 작업. 같은 기록을 몇 초 안에 반복해서 쓰지 않습니다.
3. **커서 기반 Pull**: 24시간에 한 번, 변경분만. 앱을 열 때마다 전체를 읽지 않습니다.
4. **실패 재시도 간격**: WorkManager 지수 백오프(30초부터, 최대 수 시간). 규칙 거부(`PERMISSION_DENIED`)는 재시도하지 않고 `FAILED`로 남깁니다. **실패 직후 즉시 무한 재시도는 한도 소진의 가장 흔한 원인입니다.**
5. **차단 스위치**: `backup_enabled = false`면 Push · Pull을 모두 건너뜁니다 (기록은 기기에 PENDING으로 남아 나중에 올라감).
6. **개발 빌드는 에뮬레이터**: dev 빌드는 기본으로 Auth · Firestore 에뮬레이터에 연결합니다.
7. **탈퇴 삭제는 페이지 단위**: 50개씩 조회 → batch delete. 한 번에 수천 개를 읽지 않습니다.

## 6. 출시 전 체크리스트

**Firebase**
- [ ] Firebase 프로젝트 · Google Cloud 프로젝트에 결제 계정이 연결되어 있지 않은지 확인 (요금제 Spark)
- [ ] Authentication이 Identity Platform으로 업그레이드되지 않았는지 확인
- [ ] Google 로그인 제공자 사용 설정, 출시 키 · Play 앱 서명 키 SHA-1 · SHA-256 등록
- [ ] 보안 규칙 · 인덱스 배포, 규칙 테스트 통과 (`firebase/CLAUDE.md` 시나리오)
- [ ] App Check(Play Integrity) 등록, 지표 관찰 후 Firestore 강제
- [ ] Remote Config 키 등록 (2-5)
- [ ] Hosting 배포, 정책 페이지 3종 접속 확인
- [ ] Firebase · Google 계정 2단계 인증

**정책 · 법**
- [ ] 개인정보 처리방침: 수집 항목(이메일, 운동 기록, 단순화한 경로, 기기 정보), 보관 위치(Google Firebase, 미국 등 해외 리전 여부), 보관 기간, 탈퇴 시 삭제
- [ ] 위치정보 이용약관 작성
- [ ] **위치정보 관련 신고 필요 여부 확인**: 개인의 위치 경로를 계정과 연결해 서버에 저장하므로, 위치기반서비스사업 신고 등 위치정보법상 의무에 해당하는지 출시 전 확인 (필요하면 신고 후 출시)
- [ ] Play Console: 포그라운드 서비스(위치) 신고 + 영상, 데이터 보안 섹션, 개인정보 처리방침 URL, 대상 연령

**앱**
- [ ] `tracking.md` 8-3 실기기 확인 전부
- [ ] Google 로그인 → 백업 → 앱 삭제 → 재설치 → 복원
- [ ] 게스트 기록 → 로그인 → 이전 확인
- [ ] 회원 탈퇴 후 Firestore 콘솔에서 `users/{uid}` 하위가 비었는지 확인
- [ ] `backup_enabled = false`에서 앱이 정상 동작하는지

## 7. 사용자가 늘었을 때

| 신호 | 대응 (순서대로) |
|---|---|
| Firestore 저장 0.5GiB 도달 | polyline 상한 500 → 300점, 1년 지난 기록의 경로를 더 강하게 단순화해 재업로드(일괄 작업, 쓰기 한도 고려해 며칠에 나눠) |
| 하루 쓰기 1만 도달 | Push 묶음 · 디바운스 간격 재검토, 편집 쓰기 합치기 |
| 위 조치 후에도 한도 근접 | Blaze 전환을 검토하되 데이즈와 같은 과금 방어(예산 알림 + 결제 자동 해제 함수)를 함께 설계. 또는 백업 대상을 최근 N년으로 제한 |
| Play Integrity 할당량 근접 | 할당량 증가 신청, App Check 토큰 유효 시간 조정 |

## 참고 링크

- Firebase 요금제: https://firebase.google.com/pricing
- Firestore 할당량 · 한도: https://firebase.google.com/docs/firestore/quotas
- App Check (Play Integrity): https://firebase.google.com/docs/app-check/android/play-integrity-provider
- Firebase Hosting 할당량: https://firebase.google.com/docs/hosting/usage-quotas-pricing
- Credential Manager로 Google 로그인: https://developer.android.com/identity/sign-in/credential-manager-siwg
