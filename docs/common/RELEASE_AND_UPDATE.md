# 출시 · 업데이트 가이드 (범용)

> 빌드를 스토어에 올리는 흐름(CI · CD), 버전 코드, 인앱 업데이트(강제 · 선택) 정책을 정리합니다.
> PR 검사 구성은 [TESTING.md 6장](TESTING.md#6-ci-구성), 브랜치 · 태그 규칙은 [GIT_CONVENTIONS.md 6장](GIT_CONVENTIONS.md#6-버전--태그--릴리스)을 따릅니다.

## 목차

1. 한눈에 보기
2. CI (PR마다)
3. 버전 이름 · 버전 코드
4. CD: 태그 → 내부 테스트 트랙
5. 프로덕션 승격 (수동 실행)
6. 인앱 업데이트 정책
7. 비밀 값 목록
8. 출시 체크리스트

---

## 1. 한눈에 보기

```
PR ──▶ CI (ktlint · detekt · Lint · 단위 테스트 · 스크린샷 · PR 제목)
 │
 ▼ Squash merge
main
 │
 ▼ GitHub Release 작성 (태그 v1.3.0)
CD ──▶ 서명된 .aab 빌드 ──▶ Play 내부 테스트 트랙 자동 업로드
 │
 ▼ 내부 테스트 확인 후, Actions에서 "Promote" 수동 실행 (versionCode · 비율 · 우선순위 입력)
Play 프로덕션 (단계적 출시 10% → 50% → 100%)
 │
 ▼ 100% 출시 후에만
Remote Config force_update_min_version_code 올림 (필요할 때)
```

| 단계 | 사람이 하는 일 | 자동 |
|---|---|---|
| PR | 리뷰 · 병합 | 검사 전부 |
| 내부 테스트 | Release 작성 (태그) | 빌드 · 서명 · 업로드 |
| 프로덕션 | Promote 실행, 비율 올리기 | Play API 호출 |
| 강제 업데이트 | Remote Config 값 변경 | – |

## 2. CI (PR마다)

[TESTING.md 6장](TESTING.md#6-ci-구성)의 `pr.yml`과 [GIT_CONVENTIONS.md 8-3](GIT_CONVENTIONS.md)의 PR 제목 검사를 그대로 씁니다. `main` 보호 규칙에서 이 두 검사를 필수로 지정합니다.

## 3. 버전 이름 · 버전 코드

- **태그가 원본입니다.** `v1.3.0` 태그에서 `versionName = "1.3.0"`을 만들고, 버전 코드는 아래 식으로 계산합니다.

  ```
  versionCode = MAJOR * 1_000_000 + MINOR * 10_000 + PATCH * 100 + 빌드번호
  v1.3.0, 빌드번호 0 → 1030000
  v1.3.0, 빌드번호 1 → 1030001   (같은 버전 재업로드)
  ```

- 빌드번호는 0~99. 같은 버전을 다시 올릴 때만 올립니다 (CD 실행 입력값, 기본 0).
- Gradle은 `-PreleaseVersionName=1.3.0 -PreleaseVersionCode=1030000`처럼 속성으로 받고, 속성이 없으면(로컬 빌드) `gradle/libs.versions.toml`의 `projectVersionName` · `projectVersionCode`를 씁니다.

```kotlin
// build-logic의 application 플러그인
val versionNameProp = providers.gradleProperty("releaseVersionName")
    .orElse(libs.version("projectVersionName"))
val versionCodeProp = providers.gradleProperty("releaseVersionCode")
    .orElse(libs.version("projectVersionCode"))

defaultConfig {
    versionName = versionNameProp.get()
    versionCode = versionCodeProp.get().toInt()
}
```

## 4. CD: 태그 → 내부 테스트 트랙

`.github/workflows/release.yml` — GitHub Release 발행(태그 push) 시 실행.

| 단계 | 내용 |
|---|---|
| 1 | 태그(`v1.3.0`)를 파싱해 versionName · versionCode 계산. 형식이 다르면 실패 |
| 2 | 키스토어를 Secrets(base64)에서 복원 → `keystore.properties` 생성 (작업이 끝나면 삭제) |
| 3 | `google-services.json`을 Secrets에서 복원 |
| 4 | `./gradlew :app:bundleProdRelease -PreleaseVersionName=… -PreleaseVersionCode=…` |
| 5 | Play Developer API로 **내부 테스트 트랙**에 업로드 (Gradle Play Publisher 또는 Play 업로드 액션, 버전 고정) |
| 6 | `.aab`와 매핑 파일(`mapping.txt`)을 Release 첨부 파일로 보관 |

- Play 업로드에는 Play Console API 권한만 준 **전용 서비스 계정**을 씁니다 (권한: 해당 앱의 출시 관리). JSON은 GitHub Secrets에만 둡니다.
- 첫 번째 업로드(앱 등록 직후)는 Play Console에서 직접 해야 합니다. API 업로드는 두 번째부터 가능합니다.
- 릴리스 노트는 Release 본문(라벨 기준 자동 생성, GIT_CONVENTIONS 6-4)에서 사용자용 문구만 골라 `whatsnew/ko-KR`로 넣습니다 (500자 이내).

## 5. 프로덕션 승격 (수동 실행)

`.github/workflows/promote.yml` — `workflow_dispatch`로만 실행.

| 입력 | 예 | 설명 |
|---|---|---|
| `versionCode` | `1030000` | 내부 테스트 트랙에 있는 빌드 |
| `userFraction` | `0.1` / `0.5` / `1.0` | 단계적 출시 비율 |
| `inAppUpdatePriority` | `0` ~ `5` | Play 업데이트 우선순위. **API로만 설정 가능하고 출시 후 바꿀 수 없음** |

- 비율을 올릴 때도 같은 워크플로를 `userFraction`만 바꿔 다시 실행합니다.
- 우선순위 기준: 일반 기능 `1`, 중요 버그 수정 `3`, 데이터 손실 · 보안 · 과금 위험 수정 `5`.
- 각 단계에서 Crashlytics의 "크래시 없는 사용자" 비율과 새 이슈를 확인하고 다음 비율로 올립니다. 문제가 있으면 Play Console에서 출시를 일시 중지합니다.

## 6. 인앱 업데이트 정책

Play In-App Updates로 업데이트 창을 띄우되, **강제할지 선택으로 둘지는 Remote Config가 결정**합니다. Play의 우선순위 값만으로 판단하면 출시 후 바꿀 수 없어서, 이미 나간 버전의 정책을 고칠 수 없기 때문입니다.

### 6-1. Remote Config 키

| 키 | 기본값 | 의미 |
|---|---|---|
| `force_update_min_version_code` | `1` | 이 값보다 작은 버전은 강제 업데이트 |
| `soft_update_min_version_code` | `1` | 이 값보다 작은 버전은 선택 업데이트 안내 |
| `soft_update_interval_days` | `3` | 선택 업데이트를 다시 안내하기까지 간격 |

### 6-2. 판단 흐름 (앱 시작 시 1회, 포그라운드 복귀 시 재확인)

```
Remote Config 가져오기 (최소 가져오기 간격 1시간, 실패하면 마지막 값 · 기본값)
AppUpdateManager.appUpdateInfo 조회
│
├─ 내 versionCode < force_min
│    ├─ Play에 업데이트 있음 + IMMEDIATE 허용 → IMMEDIATE 흐름 시작
│    │    └─ 사용자가 취소하거나 실패 → 강제 업데이트 화면(닫기 없음, [업데이트] = 다시 시도 또는 스토어 열기)
│    └─ Play에 업데이트 없음 (단계적 출시 대상 아님 · 캐시 지연) → 막지 않고 통과
│
├─ 내 versionCode < soft_min + 마지막 안내 후 interval_days 지남
│    └─ Play에 업데이트 있음 + FLEXIBLE 허용 → FLEXIBLE 흐름 (다운로드 완료 시 스낵바 "다시 시작")
│
└─ 그 외 → 아무것도 하지 않음
```

- **"Play에 업데이트가 없으면 막지 않는다"가 핵심입니다.** 단계적 출시 중이거나 Play 캐시가 늦어 업데이트를 받을 수 없는 사용자를 강제 화면에 가두지 않기 위함입니다.
- 그래서 `force_update_min_version_code`는 **해당 버전이 100% 출시된 뒤에만** 올립니다.
- IMMEDIATE가 동작하지 않고 선택 업데이트처럼 보이던 문제는, 앱이 FLEXIBLE만 요청했거나 IMMEDIATE 허용 여부(`isUpdateTypeAllowed`)를 확인하지 않은 경우가 대부분입니다. 위 흐름처럼 정책은 Remote Config로 정하고, 유형은 허용 여부를 확인한 뒤 고릅니다.
- IMMEDIATE 진행 중 앱이 백그라운드로 갔다 오면 `onResume`에서 `DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS` 상태를 확인해 흐름을 이어갑니다.
- 인앱 업데이트는 **Play에서 설치한 빌드에서만** 동작합니다. 테스트는 내부 앱 공유(Internal app sharing)로 낮은 버전을 설치한 뒤 높은 버전을 올려 확인합니다.

### 6-3. 코드 위치

| 역할 | 위치 |
|---|---|
| Remote Config 값 읽기 | `core:firebase` (또는 config 모듈)의 `UpdatePolicySource` |
| 판단 로직 (순수 함수) | `core:domain`의 `decideUpdate(current, policy, lastPromptAt, now): UpdateDecision` (`policy`: 강제 · 선택 기준과 안내 간격) — 단위 테스트 대상 |
| Play 연동 · 흐름 실행 | app (`MainActivity`), Activity가 필요한 API라서 |
| 강제 업데이트 화면 | app 또는 공용 UI 모듈의 전체 화면 |

## 7. 비밀 값 목록 (GitHub Secrets)

| 이름 | 내용 |
|---|---|
| `KEYSTORE_BASE64` | 업로드 키스토어 (base64) |
| `KEYSTORE_PASSWORD` · `KEY_ALIAS` · `KEY_PASSWORD` | 키스토어 비밀번호 · 별칭 |
| `GOOGLE_SERVICES_JSON_BASE64` | prod `google-services.json` |
| `PLAY_SERVICE_ACCOUNT_JSON` | Play Developer API용 서비스 계정 키 |

- Play 앱 서명(Play App Signing)을 켜서, 잃어버리면 안 되는 앱 서명 키는 Google이 보관하고 우리는 **업로드 키**만 관리합니다.
- 업로드 키 분실 시 Play Console에서 재설정을 요청할 수 있도록 개발자 계정 연락처를 최신으로 둡니다.

## 8. 출시 체크리스트

**매 출시**
- [ ] `main`의 CI 통과, 변경 PR에 라벨이 붙어 있는지 (릴리스 노트 분류)
- [ ] DB 스키마가 바뀌었다면 마이그레이션 테스트 통과 · 스키마 JSON 커밋
- [ ] 새 기능은 기능 플래그 기본값이 의도대로인지 (Remote Config 콘솔 값 포함)
- [ ] 내부 테스트 트랙 빌드를 실기기에서 설치해 핵심 흐름 확인
- [ ] 단계적 출시 각 단계에서 Crashlytics 확인
- [ ] 100% 출시 후 필요할 때만 `force_update_min_version_code` 변경

**첫 출시 전 1회**
- [ ] Play App Signing 사용, 업로드 키 백업 (비밀번호 포함, 저장소 밖 두 곳)
- [ ] Play Console API 서비스 계정 연결 · 권한 최소화
- [ ] 첫 `.aab`는 콘솔에서 직접 업로드
- [ ] Remote Config에 6-1의 키 3개 등록
- [ ] 내부 앱 공유로 인앱 업데이트(IMMEDIATE · FLEXIBLE) 동작 확인
