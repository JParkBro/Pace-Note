# 모듈 구성 (런닝앱 적용)

공통 문서 [ANDROID_ARCHITECTURE.md](../common/ANDROID_ARCHITECTURE.md)의 구조를 런닝앱에 적용한 결과입니다. 패턴 · 규칙 설명은 공통 문서를 따르고, 이 문서는 **실제 모듈 · 키 · 의존 관계**만 정리합니다.

## 1. 자리표시자 값

| 자리표시자 | 값 |
|---|---|
| `{app}` | `pacenote` |
| `{base.package}` | `jpark.bro.pacenote` |

## 2. 저장소 구조

```
pacenote/
├─ app/ core/ feature/ widget/ build-logic/   # Android 앱 (루트가 Gradle 프로젝트)
├─ firebase/         # firestore.rules, firestore.indexes.json, firebase.json(에뮬레이터 · Hosting), 규칙 테스트
├─ policy-site/      # Firebase Hosting: 개인정보 처리방침, 이용약관, 위치정보 이용약관
├─ scripts/          # generate-theme-colors.py (theme-tokens.json → ThemeTokens.kt)
└─ docs/
   ├─ common/        # 범용 문서
   └─ run/           # 이 프로젝트 문서
```

## 3. Android 모듈

```
pacenote/
├─ app/
├─ build-logic/
├─ core/
│  ├─ model/          [JVM] Run, RunSplit, RoutePoint, RawLocation, LiveMetrics, Goal, WeeklySummary, UserProfile, DistanceUnit
│  ├─ common/         [JVM] Result, suspendRunCatching, Clock · ElapsedClock, 날짜 유틸
│  ├─ domain/         [JVM] PointFilter, PaceWindow, SplitCalculator, AutoPauseDetector, CalorieEstimator,
│  │                        RunMetricsAccumulator, AnnouncementScheduler, WeeklyProgress,
│  │                        PolylineSimplifier · PolylineCodec, RouteProjector, decideUpdate
│  ├─ navigation/     Navigator, 탭별 백스택 (홈 / 기록 / 내 정보), AppIntents (탭별 백스택은 탭 화면과 함께 추가)
│  ├─ designsystem/   AppColors(31 토큰) · 8 팔레트, 타이포 · 간격, Run* 컴포넌트, RouteCanvas
│  ├─ ui/             MviViewModel, ObserveEvents, RunFormat(거리 · 시간 · 페이스 표기), 권한 요청 헬퍼, RunWebView (권한 헬퍼 · RunWebView는 쓰는 화면과 함께 추가)
│  ├─ database/       Room (run, run_point, run_split, goal_history, sync_cursor)
│  ├─ datastore/      테마 · 모드, 온보딩 · 약관 동의, 러닝 설정, 체중 · 단위, 배터리 안내 표시 여부
│  ├─ location/       LocationSource (Fused 구현 · GPX 재생 구현), 위치 설정 확인
│  ├─ tracking/       RunTracker(공개 인터페이스) · RunTrackingService · TrackingEngine · RunRecorder ·
│  │                  VoiceAnnouncer · TrackingNotification
│  ├─ firebase/       Auth, Firestore 원격 데이터 소스(백업 · 동의), Remote Config, App Check, Crashlytics 설정
│  ├─ data/           Repository: Run, Goal, Stats, Settings, Onboarding(약관 동의 · 온보딩 완료), Auth, Account, Backup · HomeWidgets · FeatureFlags 인터페이스
│  ├─ sync/           BackupPushWorker, BackupPull, 게스트 기록 이전
│  └─ testing/        Fake(LocationSource, ElapsedClock, Repository), MainDispatcherRule, `core:domain` testFixtures(GPX fixture) 재공개
├─ feature/
│  ├─ onboarding/  api/ impl/     O0 ~ O3
│  ├─ home/        api/ impl/     H1
│  ├─ run/         api/ impl/     P1 ~ P6, P8
│  ├─ record/      api/ impl/     R1 ~ R4, P7
│  └─ settings/    api/ impl/     M1 ~ M10
└─ widget/           Glance 위젯 (widget/CLAUDE.md)
```

공통 구조에 추가된 전용 모듈: `core:location`, `core:tracking`, `core:firebase`, `core:sync`, `widget`. 공통 구조에서 빠진 모듈: `core:network` (외부 HTTP 없음).

- `core:tracking`은 Android 서비스를 포함하지만, feature가 보는 것은 `RunTracker` 인터페이스와 `TrackingState`뿐입니다.
- `core:sync`는 feature가 아니므로 `core:database` · `core:firebase`를 직접 참조합니다.
- 진행 중 알림은 `core:tracking` 안에 둡니다 (v1에는 다른 알림이 없어 `core:notification`을 만들지 않음. v2 리마인더 때 분리).

## 4. 내비게이션 키

### 4-1. api에 공개하는 키

| 모듈 | 키 | 인자 |
|---|---|---|
| `onboarding:api` | `OnboardingKey` | – |
| `home:api` | `HomeKey` | – |
| `run:api` | `RunKey` | `recoverRunId: String?` (null = 새 러닝, 값 = P8 복구) |
| `record:api` | `RecordKey` | – (탭) |
| | `RunDetailKey` | `runId: String` |
| | `ShareCardKey` | `runId: String` (결과 P6 · 상세 R3에서 연다) |
| `settings:api` | `SettingsKey` | – (탭, 내 정보) |
| | `GoalKey` | – (홈의 "목표 정하기"에서도 연다) |
| | `PolicyDocKey` | `type: PolicyType` (온보딩 약관 동의에서도 연다) |

### 4-2. impl 내부 키 (internal)

| 모듈 | 키 |
|---|---|
| `onboarding:impl` | `StartKey`(O0), `TermsKey`(O1), `BasicInfoKey`(O2), `FirstGoalKey`(O3) |
| `run:impl` | `RunResultKey(runId)`(P6). P1 ~ P5 · P8은 `RunKey` 한 화면 안의 단계 상태 |
| `record:impl` | `RunEditKey(runId)`(R4) |
| `settings:impl` | `RunSettingsKey`(M3), `ProfileEditKey`(M4), `BackupKey`(M6), `PolicyListKey`(M7), `AppInfoKey`(M8), `AccountKey`(M10), `WithdrawKey` |

> P1 ~ P5를 화면 하나(`RunKey`)의 단계로 두는 이유: 권한 → GPS → 카운트다운 → 기록 → 일시정지가 같은 `TrackingState`를 보며 이어지고, 뒤로 가기 동작이 단계마다 달라서(기록 중 뒤로 = 앱 백그라운드) 백스택 항목으로 나누면 오히려 복잡해집니다.

### 4-3. app에서 결정하는 목적지

| 상황 | 결과 타입 (api) | app의 처리 |
|---|---|---|
| 앱 시작 | – | 온보딩 완료 전 → `OnboardingKey`(그동안 알림 · 위젯 진입 무시) / 진행 중 기록 있음 → `[HomeKey, RunKey(recoverRunId)]` / 그 외 → `HomeKey` |
| 온보딩 완료 | `OnboardingResult.Done` | `replaceAll(HomeKey)` |
| 러닝 결과 "완료" | `RunResult.Finished` | `replaceAll(HomeKey)` (러닝 흐름 제거) |
| 알림 탭 (`EXTRA_OPEN_RUN`) | – | 진행 중 기록이 있으면 `[HomeKey, RunKey()]`, 없으면 무시 |
| 위젯 "달리기 시작" (`EXTRA_START_RUN`) | – | `[HomeKey, RunKey()]` |
| 로그아웃 · 탈퇴 | `SessionEnded` | `replaceAll(HomeKey)` (게스트 상태로) |

## 5. 기능 간 의존 관계

| impl | 의존하는 api |
|---|---|
| `onboarding:impl` | `settings` (정책 문서 보기). 끝나면 결과만 알리고 app이 목적지 결정 |
| `home:impl` | `run`, `record` (상세), `settings` (목표) |
| `run:impl` | `record` (공유 카드) |
| `record:impl` | `run` (빈 상태의 "달리기 시작") |
| `settings:impl` | – |
| `widget` | – (`AppIntents`로 앱을 열고 app이 백스택을 만든다) |

## 6. 화면별 MVI 구성

화면 레이아웃은 [design/README.md](design/README.md)의 화면 ID별 시안을 참고합니다.

| 화면 | Event | 주요 Event |
|---|---|---|
| 시작 (O0) | 있음 | Google 로그인 창, 다음 이동, 로그인 실패 스낵바 |
| 약관 동의 (O1) | 있음 | 정책 문서 열기, 다음 이동 |
| 기본 정보 · 첫 목표 (O2 · O3) | 있음 | 다음 이동, 완료 결과 전달 |
| 홈 (H1) | 있음 | 러닝 시작 · 상세 · 목표 이동 |
| 러닝 (P1 ~ P5, P8) | 있음 | 권한 요청, 위치 설정 켜기, 배터리 설정 열기, 결과 이동, 뒤로(버림) |
| 결과 (P6) | 있음 | 공유 카드 이동, 완료 결과 전달 |
| 기록 목록 · 통계 (R1 · R2) | 있음 | 상세 이동, 러닝 시작 이동 |
| 기록 상세 (R3) | 있음 | 편집 · 공유 카드 이동, 뒤로(삭제됨) |
| 기록 편집 (R4) | 있음 | 저장 후 뒤로, 삭제 후 뒤로 |
| 공유 카드 (P7) | 있음 | 사진 선택기, 저장 완료 토스트, 공유 시트 |
| 내 정보 (M1) | 있음 | 하위 화면 이동, Google 로그인 창, 게스트 기록 이전 다이얼로그 |
| 목표 · 러닝 설정 · 내 정보 수정 (M2 ~ M4) | 있음 | 저장 후 뒤로, TTS 데이터 설치 화면 |
| 테마 시트 (M5) | 없음 (`Nothing`) | – |
| 데이터 · 백업 (M6) | 있음 | 백업 · 복원 결과 토스트, 전체 삭제 확인 |
| 계정 · 탈퇴 (M10) | 있음 | 재인증 창, 결과 전달 (세션 종료) |
| 정책 문서 보기 | 없음 (`Nothing`) | – |

## 7. Koin 모듈

| 모듈 | 정의 위치 |
|---|---|
| `appModule` | app — Navigator, 결과 핸들러, 인앱 업데이트, 전체 `includes` |
| `databaseModule`, `datastoreModule` | core |
| `locationModule` | core:location — `LocationSource` (prod: Fused, 디버그 메뉴에서 GPX 재생으로 교체 가능) |
| `trackingModule` | core:tracking — `RunTracker`(single, 앱 범위), `TrackingEngine` 팩토리. 서비스는 Android 진입점이라 `KoinComponent`로 꺼낸다 |
| `firebaseModule` | core:firebase — Auth · Firestore 데이터 소스, RemoteFlags |
| `dataModule`, `domainModule` | core |
| `syncModule` | core:sync — Worker는 Android 진입점이라 `KoinComponent` |
| `{feature}Module` × 5 | 각 impl |
| `widgetModule` | widget |

## 8. 빌드 변형

| flavor | Firebase 프로젝트 | 앱 ID | 비고 |
|---|---|---|---|
| `dev` | `{firebase-project-id}` (에뮬레이터 연결 기본) | `jpark.bro.pacenote.dev` | 디버그 메뉴(GPX 재생, 추적 로그 보기) |
| `prod` | `{firebase-project-id}` | `jpark.bro.pacenote` | |

- 데이즈와 같이 **Firebase 프로젝트 하나**를 dev · prod가 함께 쓰고, dev는 에뮬레이터를 기본으로 연결합니다 (`local.properties` `firebase.useEmulator` · `firebase.emulator.host`). Spark 한도를 개발 중 소모하지 않기 위함입니다.
- App Check: prod Play Integrity, dev 디버그 제공자.
- Firebase 연결(에뮬레이터 포함)은 9단계부터입니다. 그 전에는 두 flavor 모두 Firebase 없이 빌드됩니다 (9장).

## 9. 개발 순서

**위험한 것(추적)을 먼저** 실기기에서 검증하고, 서버(로그인 · 백업)는 앱이 혼자서 완성된 뒤에 붙입니다.

**2026-11-10 결정: 로컬 앱 먼저, 외부 설정은 최후순위.** 당분간 개인용 앱처럼 **Room · DataStore만** 씁니다. 1 ~ 8단계는 Firebase 프로젝트 · `google-services.json` · 콘솔 설정 없이 빌드 · 실행되어야 합니다. 외부 서비스나 사람이 콘솔에서 해야 하는 설정이 필요한 작업(Firebase 전체, Google 로그인, 정책 페이지 Hosting, 인앱 업데이트, Play Console)은 모두 9단계 이후로 미룹니다.

- 1 ~ 8단계에서 `firebase-*` 의존성, `google-services` · `firebase-crashlytics` 플러그인을 모듈에 적용하지 않습니다 (catalog 항목은 그대로 둠).
- 기능 플래그(`FeatureFlags`)는 9-2 전까지 코드 기본값만 돌려주는 로컬 구현을 씁니다 (`roadmap.md` 5장).
- 온보딩은 로컬 부분(O2 기본 정보 · O3 첫 목표)만 1부에 둡니다 (2026-11-10 결정). 시작 시 온보딩 분기는 "온보딩 완료" 기준이고, O0 시작 · O1 약관 동의는 9-3에서 앞에 붙입니다.

**1부 — 로컬 앱 (Room · DataStore만)**

1. `core:model`, `core:common`, `core:domain` — 계산 전부 + GPX fixture 재생 테스트 (`tracking.md` 8장)
2. `core:designsystem` — 테마 토큰 8 팔레트(생성 스크립트), 타이포, `Run*` 컴포넌트, `RouteCanvas`
3. `core:database`, `core:datastore`, `core:data` — `run` · `run_point` · `run_split` + DAO 테스트, `RunRepository`, `StatsRepository`
4. `core:location`, `core:tracking` — 서비스 · 엔진 · 저장 · 복구 · 알림. **이 단계 끝에 실기기 30분 테스트를 통과해야 다음으로**
   - 2026-11-09 결정: 실기기 확인을 5단계와 병행한다. 확인 항목은 #31 · #32 본문 체크리스트에서 마무리하고, 문제가 나오면 5단계보다 먼저 고친다.
5. `feature:run` — P1 ~ P6, P8 (공유 카드 버튼은 6단계에서 연결)
6. `feature:home`, `feature:record` (R1 ~ R4), 공유 카드(P7)
7. `feature:settings` (로컬 설정: M1 ~ M5), 음성 안내 설정 연결 → **로컬 앱 완성**
   - M7 약관 및 정책은 정책 페이지(Hosting)가 필요해 9-3으로 미룹니다.
   - M8 앱 정보는 "최신 여부"가 인앱 업데이트와 함께라 10단계로 미룹니다. 버전은 M1 맨 아래에 표시합니다.
8. `widget` (`widget/CLAUDE.md`)
   - `feature:onboarding` O2 · O3 (로컬), 시작 시 온보딩 분기

**2부 — 외부 설정이 필요한 작업 (최후순위)**

9. 서버
   - 9-1 `firebase/` 보안 규칙 · 에뮬레이터 테스트
   - 9-2 `core:data` `FeatureFlags` 인터페이스 · 차단 스위치(`backup_enabled`, 업데이트 기준) 코드 기본값 구현 (콘솔 불필요, 먼저 진행)
   - 9-2 후반 (`app/google-services.json` 필요) `core:firebase` (App Check, 에뮬레이터 연결, Crashlytics), `google-services` · `firebase-crashlytics` 플러그인 적용, `FeatureFlags`를 Remote Config 구현으로 교체
   - 9-3 `policy-site/` Hosting, M7 약관 및 정책 · 오픈소스 · Pretendard 라이선스, M1 앱 평가하기(In-App Review), Google 로그인 (Credential Manager), `feature:onboarding` O0 · O1을 O2 앞에 추가 · 서버 동의 기록
   - 9-4 `core:sync` 백업 Push · Pull, 게스트 기록 이전, M6
   - 9-5 계정 · 회원 탈퇴 (M10)
10. 인앱 업데이트 · 강제 업데이트 화면 (`RELEASE_AND_UPDATE.md` 6장), M8 앱 정보
11. **직접 확인 · 등록 (사람이 콘솔에서)**: `infra-and-billing-protection.md` 6장 체크리스트, Play Console 포그라운드 서비스 신고 · 데이터 보안, 위치정보 관련 확인

## 10. widget 모듈 (Glance)

> 상태: 구현 완료, 실기기 확인 전. 기획은 `product-spec.md` 8장, 규칙은 `widget/CLAUDE.md`.

- [x] `:widget` 모듈 (`pacenote.android.library` + `pacenote.android.compose` + `pacenote.koin`), catalog의 `glance-appwidget` · `glance-preview` · `work-runtime`
- [x] 주간 목표 4×2 · 2×2 위젯 + 리시버 + `appwidget-provider` XML
- [x] `core:data`의 `HomeWidgets` 인터페이스 구현 (`WidgetUpdater` → `WidgetRefreshWorker`)
- [x] 갱신: 기록 저장 · 삭제, 목표 · 테마 · 거리 단위 변경, 매주 월요일 00:00 WorkManager, 시각 · 시간대 변경 (기록 복원은 9-4에서)
- [x] 탭: `AppIntents.EXTRA_START_RUN` / 홈
- [x] 크기별 Glance Preview, 주 경계 테스트
- [ ] 실기기: 위젯 추가 → 러닝 저장 후 갱신, 앱 프로세스가 죽은 뒤 탭
