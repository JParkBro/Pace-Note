# pacenote (런닝앱)

입문~중급 러너를 위한 "혼자 꾸준히 뛰는" 러닝 기록 앱. 지도 없이 경로를 흰 선으로 보여주고, 로그인 없이 바로 쓸 수 있으며 Google 로그인하면 기록을 백업한다. Android 전용.

## 저장소 구조

| 경로 | 내용 | 세부 지침 |
|---|---|---|
| 루트 (`app/`, `core/`, `feature/`, `widget/`, `build-logic/`) | Android 앱 (Kotlin, Compose, 멀티 모듈) | 이 파일의 "Android 앱" 섹션 |
| `widget/` | Glance 홈 위젯 | `widget/CLAUDE.md` |
| `firebase/` | Firestore 보안 규칙 · 인덱스 · 에뮬레이터 설정, Hosting 설정 | `firebase/CLAUDE.md` |
| `policy-site/` | Firebase Hosting(`public/`): 개인정보 처리방침 · 이용약관 · 위치정보 이용약관 | `docs/run/infra-and-billing-protection.md` 2-4 |
| `config/detekt/` | detekt 규칙 | – |
| `scripts/` | `generate-theme-colors.py` (theme-tokens.json → ThemeTokens.kt), `generate-gpx-fixtures.py` (GPX fixture · 기대값) | – |
| `docs/common/` | 범용 규칙 (아키텍처, 컨벤션, 테스트, Git, 출시 · 업데이트) | – |
| `docs/run/` | 이 프로젝트의 기획 · 추적 · 데이터 · 인프라 결정 | – |

Cloud Functions는 없다 (Spark 요금제라 쓸 수 없고, v1에 필요한 서버 로직도 없음).

아직 없는 모듈 · 폴더는 `docs/run/module-map.md` 9장 순서대로 만든다. 현재 Gradle 모듈은 `:app`뿐이다.

## 문서 지도

작업 전에 관련 문서를 읽는다. 문서와 코드가 다르면 **문서가 기준**이고, 의도적으로 바꿔야 하면 먼저 사용자에게 확인한다.

| 작업 | 먼저 읽을 문서 |
|---|---|
| 화면 · 기능 구현 | `docs/run/product-spec.md`, `docs/run/module-map.md`, 시안 `docs/run/design/README.md` (화면 ID별 경로, 시안과 다른 부분은 product-spec 11장) |
| **위치 추적 · 러닝 기록 · 계산** | `docs/run/tracking.md` |
| 모듈 추가 · 구조 변경 | `docs/common/ANDROID_ARCHITECTURE.md`, `docs/run/module-map.md` |
| 코드 작성 전반 | `docs/common/CODING_CONVENTIONS.md` |
| 테스트 작성 | `docs/common/TESTING.md`, `docs/run/tracking.md` 8장 |
| UI · 색상 | `docs/run/theme-guide.md`, 값 원본 `docs/run/assets/theme-tokens.json` |
| DB · 데이터 모델 · 백업 | `docs/run/data-model-erd.md` |
| Firebase · 과금 | `docs/run/infra-and-billing-protection.md` |
| 기능 추가 · 버전 계획 | `docs/run/roadmap.md` |
| 광고 | `docs/run/ads-strategy.md` (v1은 광고 없음) |
| 출시 · 인앱 업데이트 | `docs/common/RELEASE_AND_UPDATE.md` |
| 브랜치 · 커밋 · PR | `docs/common/GIT_CONVENTIONS.md` |

## 절대 어기면 안 되는 결정

- **Firebase는 Spark(무료), 결제 수단을 등록하지 않는다.** Blaze가 필요한 기능(Cloud Functions, Cloud Storage, Firestore TTL, Identity Platform)을 쓰지 않는다. 사용량을 늘리는 변경(쓰기 · 읽기 · 리스너)은 이유와 최악의 경우를 설명하고, 사용자 1,000명 · 하루 기준 사용량이 Spark 한도 50% 이내인지 계산한다.
- **지도를 쓰지 않는다.** 지도 SDK · 지도 타일 · 위성 이미지를 넣지 않는다. 경로는 `route-canvas` 위 흰 선(Canvas)으로만 그린다.
- **Room이 원본.** 화면은 Room과 추적 상태 Flow만 구독한다. Firestore는 로그인한 사용자의 백업 통로다.
- **원시 GPS 점(`run_point`)은 기기 밖으로 보내지 않는다.** 서버에는 요약 + 단순화한 경로(인코딩 polyline)만 올리고, 기록 1건 = Firestore 문서 1개.
- **Firestore는 필드를 추가만 한다.** 이름 변경 · 삭제 · 타입 변경 금지. 모든 기록 문서에 `schemaVersion`, `source`, `activityType`, `planId`를 둔다 (`roadmap.md` 6장).
- **Room은 `exportSchema = true`, `fallbackToDestructiveMigration` 금지.**
- **미완성 기능은 기능 플래그(`feature_{name}_enabled`, 기본값 false) 뒤에 둔다.** 플래그는 `core:data`의 `FeatureFlags` 한 곳에서만 읽는다.
- **백그라운드 위치 권한(`ACCESS_BACKGROUND_LOCATION`)을 요청하지 않는다.** 추적은 사용자가 앱 화면에서 "시작"을 눌러 띄운 위치 타입 포그라운드 서비스로만 한다.
- **러닝 중에는 어떤 광고도 띄우지 않는다** (v1은 광고 자체가 없음).
- **거리 · 페이스 · 스플릿 · 칼로리 · 자동 일시정지 계산은 `core:domain`에만 둔다.** 서비스 · 화면 · 위젯 · 테스트가 모두 이것을 쓴다.
- **ID는 클라이언트 생성 UUID 문자열**, 날짜는 `yyyy-MM-dd`(LocalDate), 시각은 epoch millis. 자동 증가 ID 금지 (`run_point`의 순번 `seq`는 기록 안 순서라 예외).
- **색은 테마 토큰으로만.** `Color(0xFF…)` 하드코딩 금지.
- 문서의 "범위 밖" 기능(소셜 · 피드 · 랭킹 · 지도 · 심박수 · 음악)과 v2 이후 기능(`roadmap.md`)을 임의로 추가하지 않는다.

## 작업 방식

- 여러 모듈에 걸친 변경, DB 스키마 변경, 보안 규칙 변경, 추적 서비스 변경은 **먼저 계획을 보여주고** 진행한다.
- 기획이 문서에 없거나 모호하면 추측해서 구현하지 말고 질문한다.
- 결정이 바뀌면 관련 `docs/` 문서도 같은 PR에서 고친다. 공통 규칙에서 벗어나는 결정은 `docs/run/README.md`의 "공통 문서와 다르게 가는 부분"에 기록한다.
- 새 기능은 `docs/run/roadmap.md` 4장 절차(기능 정의 → 영향 점검 → 플래그 OFF로 병합)를 따른다.
- 작업을 끝내기 전에 해당 영역의 "완료 전 확인"을 실행한다. Android는 이 파일의 "Android 앱" 섹션, Firebase는 `firebase/CLAUDE.md`, 위젯은 `widget/CLAUDE.md`.

## Git

- 브랜치: `{type}/{이슈번호}-{영문-요약}` (예: `feat/12-run-auto-pause`)
- 커밋 · PR 제목: `[Type] scope: 요약` (예: `[Feat] run: 자동 일시정지 추가`)
  - Type: `Feat` `Fix` `Design` `Refactor` `Perf` `Test` `Docs` `Build` `CI` `Chore` `Revert`
  - 요약은 한국어, "~ 추가 / 수정 / 제거 / 변경"으로 끝낸다
- `main`에 직접 커밋 · push하지 않는다. force push는 본인 작업 브랜치에만 `--force-with-lease`.
- 훅을 우회하는 `--no-verify`를 쓰지 않는다.
- 포맷 변경과 로직 변경을 한 커밋에 섞지 않는다.

## 비밀 정보

다음을 커밋하거나 코드에 넣지 않는다. 필요하면 환경 변수 · `local.properties` · `keystore.properties` · GitHub Secrets를 쓴다.

- 키스토어(`*.jks`, `*.keystore`)와 비밀번호 (`keystore.properties`)
- 서비스 계정 JSON, API 비밀 키, 토큰, `google-services.json`
- `local.properties`, `keystore.properties`, `.env`

개인정보(토큰, 이메일, **위치 좌표**, 사용자 입력 원문)를 로그에 남기지 않는다. 디버그 로그에서도 좌표는 찍지 않는다.

## 언어

- 사용자에게 하는 설명, 커밋 메시지, PR 본문, 문서: **한국어**
- 코드 식별자, 패키지, 파일명: 영어
- 앱 UI 문자열 · 음성 안내 문구: `strings.xml`에 한국어로 추가 (하드코딩 금지)

## Android 앱

규칙 원문: `docs/common/ANDROID_ARCHITECTURE.md`, `docs/common/CODING_CONVENTIONS.md`, `docs/common/TESTING.md`
이 프로젝트의 모듈 · 키 · 의존 관계: `docs/run/module-map.md`

### 스택

Kotlin · Jetpack Compose · Navigation 3 · Koin(`koin-compose-navigation3`) · Room · DataStore · WorkManager · Glance · Fused Location Provider · TextToSpeech · Firebase(Auth, Firestore, Remote Config, App Check, Crashlytics) · Credential Manager(Google 로그인)

Ktor는 공통 스택이지만 v1에는 외부 HTTP가 없어 `core:network`를 만들지 않는다. 필요해지면 추가한다.

### 빌드 구성

- convention plugin은 `build-logic/` (`pacenote.android.application`, `pacenote.android.library`, `pacenote.android.compose`, `pacenote.jvm.library`, `pacenote.koin`, `pacenote.room`, `pacenote.feature.api`, `pacenote.feature.impl`).
- 버전 · SDK · applicationId는 `gradle/libs.versions.toml`의 `project*` 값. versionCode는 `MAJOR*1_000_000 + MINOR*10_000 + PATCH*100 + 빌드번호`.
- 기본 패키지는 `jpark.bro.pacenote`. dev flavor는 applicationId에 `.dev`가 붙는다.
- Gradle 데몬 JVM은 21 (`gradle/gradle-daemon-jvm.properties`). detekt 1.23이 JDK 25에서 동작하지 않는다.
- `google-services` · `firebase-crashlytics` 플러그인은 `app/google-services.json`을 받은 뒤 app에 적용한다.

### 명령어

> 빌드 변형은 `dev` / `prod` flavor × `debug` / `release`. 작업 이름이 실제와 다르면 `./gradlew tasks`로 확인하고 이 표를 고친다.

| 목적 | 명령 |
|---|---|
| 디버그 빌드 | `./gradlew :app:assembleDevDebug` (`google-services` 플러그인을 적용한 뒤에는 `app/google-services.json` 필요, 커밋하지 않음) |
| Firebase 에뮬레이터 (dev 빌드가 기본으로 연결) | `cd firebase && npx firebase emulators:start --only auth,firestore --project {firebase-project-id}`. 기기에서 `10.0.2.2` 연결이 안 되면 `adb reverse tcp:9099 tcp:9099` (8080도) + `local.properties` `firebase.emulator.host=127.0.0.1` |
| 전체 단위 테스트 | `./gradlew testDevDebugUnitTest` |
| 특정 모듈 테스트 | `./gradlew :core:domain:test` (JVM) · `./gradlew :feature:run:impl:testDevDebugUnitTest` |
| 포맷 자동 수정 | `./gradlew ktlintFormat` |
| 정적 분석 | `./gradlew ktlintCheck detekt lintDevDebug` |
| 테마 색 코드 생성 | `python3 scripts/generate-theme-colors.py` |
| DB 마이그레이션 테스트 | `./gradlew :core:database:connectedDevDebugAndroidTest` (기기 필요) |

스크린샷 테스트(Roborazzi)는 아직 구성하지 않았다. `docs/common/TESTING.md` 5장대로 도입하면 `verifyRoborazziDevDebug` · `recordRoborazziDevDebug`를 이 표에 추가한다.

### 모듈 규칙

- 기능은 `feature:{name}:api`(NavKey만) / `feature:{name}:impl`(구현)로 나눈다.
- **impl은 다른 기능의 api만 참조한다. impl → impl 금지, api → api 금지.**
- api에는 `@Serializable` NavKey만 둔다. 외부에서 들어올 화면의 키만 공개하고, 내부 화면 키는 impl에 `internal`.
- feature는 `core:data`(Repository)와 `core:tracking`의 `RunTracker` 인터페이스로만 데이터 · 추적에 접근한다. `core:database`, `core:firebase`, `core:location`을 직접 참조하지 않는다.
- `core:model`, `core:common`, `core:domain`은 순수 Kotlin(JVM) 모듈. Android 의존을 넣지 않는다.
- 경로를 화면 좌표로 바꾸는 투영(`RouteProjector`)도 `core:domain`. 그리는 컴포넌트(`RouteCanvas`)는 `core:designsystem`.

### 화면 (MVI)

화면 하나 = 아래 파일 묶음 (`feature/{name}/impl/.../{name}/impl/`, 화면이 여럿이면 화면마다 하위 패키지)

```
{Feature}Action.kt     sealed interface — 사용자가 한 일 (PauseClicked, MemoChanged)
{Feature}State.kt      data class — 모든 프로퍼티 val + 기본값
{Feature}Event.kt      sealed interface — 일회성 효과 (NavigateBack, ShowSnackbar). 없으면 파일 생략, 타입 인자는 Nothing
{Feature}ViewModel.kt  MviViewModel<S, A, E> 상속
{Feature}Screen.kt     internal {Feature}Root + private {Feature}Screen + private Preview(@ScreenPreviews)
di/{Feature}Module.kt  viewModelOf + navigation<{Feature}Key>
```

- ViewModel 공개 멤버는 `state`, `event`, `onAction`뿐. 화면 입력은 **`onAction` 하나로만** 받는다.
- `onAction`과 Root의 Event 처리 `when`에는 **`else`를 쓰지 않는다.**
- 인자 없는 Action · Event는 `data object`, 있으면 `data class`.
- ViewModel은 `Context`, `Navigator`, Compose 타입을 모른다. **화면 이동은 Event → Root에서 처리.** 권한 요청 · 설정 화면 열기 · 공유 시트도 Root가 Event로 처리한다.
- 계속 보여야 하는 것(다이얼로그 표시, 입력값)은 State, 한 번만 일어나는 것은 Event.
- Root는 ViewModel 연결 · State 수집 · Event 처리만 한다. 레이아웃은 Screen에.
- Screen은 `state`, `onAction`, `modifier: Modifier = Modifier`를 받고 ViewModel을 모른다.
- **러닝 중 화면의 상태는 ViewModel이 아니라 `RunTracker.state`(서비스 쪽)가 원본이다.** ViewModel은 그것을 State로 옮기기만 하고, 시간 · 거리를 직접 계산하지 않는다 (`tracking.md` 3장).

### Navigation 3 · Koin

- 이동은 `Navigator`(`core:navigation`)의 `goTo` / `goBack` / `replaceAll`.
- `NavDisplay`의 `entryDecorators`에 `rememberViewModelStoreNavEntryDecorator()`를 빼지 않는다.
- Koin은 생성자 주입만. 일반 클래스에서 `KoinComponent` · `get()` 사용 금지 (서비스 · 위젯 리시버 · Worker 등 Android 진입점만 예외).
- `@KoinExperimentalAPI` 사용 시 `@OptIn`은 모듈 정의 파일에만.
- Koin 모듈을 추가 · 수정하면 `KoinModulesTest`(`verify()`)가 통과해야 한다.
- 온보딩 완료 · 로그아웃 · 진행 중 기록 복구 후 목적지는 impl이 아니라 app의 결과 핸들러가 결정한다.

### 데이터

- Repository: 스트림은 `observe…(): Flow<T>`, 1회성은 `suspend … : Result<T>`. 예외를 ViewModel까지 던지지 않는다.
- suspend 블록 예외 처리는 `runCatching` 대신 `suspendRunCatching` (CancellationException을 삼키지 않기 위해).
- 백업 대상(기록 · 목표 · 프로필) 저장은 Room에 `sync_state = PENDING`으로 쓰고 `core:sync`가 올린다. 화면에서 Firestore를 직접 호출하지 않는다.
- 기록 삭제는 soft delete(`deleted_at`). Firestore 문서를 실제로 지우는 것은 회원 탈퇴뿐이다.
- Room 컬럼명은 snake_case + `@ColumnInfo(name = …)`. 스키마를 바꾸면 Migration + 스키마 JSON 커밋 + 마이그레이션 테스트. 단, **출시 전까지는 version 1을 고쳐 쓴다** (`docs/run/README.md` 예외 항목).
- 현재 시각은 주입받은 `Clock`, 경과 시간은 주입받은 `ElapsedClock`(`SystemClock.elapsedRealtime` 래퍼), 디스패처도 주입받는다.

### UI

- 화면 레이아웃은 `docs/run/design/{화면 ID 소문자}/`(`screen.png`, `code.html`)를 참고한다. 색 · 글꼴 · 문구 · 기능은 시안이 아니라 문서가 기준이고, product-spec 11장의 "시안과 다른 부분"은 구현하지 않는다.
- 색은 `AppThemeTokens.colors.*`만 사용. 보조 버튼 · 트랙 · 비활성 칩은 `fill`. 경로는 `routeCanvas` · `routeLine` · `routeLineMuted` · `routeStart`.
- 버튼 · 칩 · 뱃지 · 카드 · 입력창 · 시트 · 수치 타일 · 경로는 `core:designsystem`의 `component/Run*`을 쓴다. 기능 모듈에 비슷한 컴포넌트를 새로 만들지 않는다 (목록: `theme-guide.md` 11장).
- 러닝 중 큰 숫자(거리 · 시간 · 페이스)는 `textPrimary` + `metricHuge` / `metricLg` 스타일. primary는 시작 · 일시정지 버튼에만.
- Preview: 화면은 `@ScreenPreviews`(라이트 · 다크 · 글꼴 2배 · 가로), 공통 컴포넌트는 `@ComponentPreviews`(라이트 · 다크 · 글꼴 2배).
- 숫자에 tabular-nums. 페이스 표기는 `5′48″`(프라임 기호), 시간은 `30:12` / `1:12:40`. 포맷은 `core:ui`의 `RunFormat` 한 곳에서.
- 문자열은 `strings.xml` (`{feature}_{screen}_{purpose}`), 아이콘에 `contentDescription`.
- `LazyColumn` 항목에 `key`.
- 테스트에서 찾을 요소에 `Modifier.testTag("snake_case")`.

### 테스트

코드를 바꾸면 함께 작성 · 수정한다.

| 바꾼 것 | 필요한 테스트 |
|---|---|
| `core:domain` 계산 | 경계값 단위 테스트 + **GPX 재생 테스트** (`core:domain` testFixtures의 fixture, `tracking.md` 8장) |
| 추적 서비스 · `RunTracker` | Fake `LocationSource`로 시작 · 일시정지 · 재개 · 종료 · 복구 테스트 |
| ViewModel | 주요 Action의 State · Event 테스트 (성공 · 실패) |
| Screen UI | Preview 추가 · 수정 (Roborazzi 도입 후 스크린샷 기준 갱신) |
| DB 스키마 | Migration 테스트 |
| Koin 모듈 | `verify()` 통과 |

- Fake는 `core:testing`에 두고 재사용한다. Mock보다 Fake 우선.
- 테스트 이름은 백틱 한국어 문장: `` `정확도 30m를 넘는 점은 거리에 넣지 않음` ``.
- 샘플 데이터는 `docs/run/product-spec.md` 9장 시나리오(오늘 = 2026.10.29 목)를 쓴다.
- 추적 관련 변경은 단위 테스트와 별도로 **실기기 확인**(화면 끈 상태 30분, 강제 종료 후 복구)을 PR 본문에 적는다.

### 새 기능(화면) 추가 순서

1. `settings.gradle.kts`에 `:feature:{name}:api`, `:feature:{name}:impl` 추가
2. api: NavKey / impl: Action · State · (Event) · ViewModel · Screen.kt · di 모듈
3. 이동할 기능의 api만 impl 의존성에 추가
4. app: impl 의존성 + `appModule`의 `includes`에 모듈 추가
5. ViewModel 테스트, Preview, Koin verify
6. `docs/run/module-map.md`의 키 · 의존 표 갱신

### 완료 전 확인

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck detekt
./gradlew testDevDebugUnitTest      # 변경 범위가 작으면 해당 모듈만
```

ktlint · detekt 위반은 코드를 고쳐서 해결한다. `ktlint-baseline.xml` · `detekt-baseline.xml`에 새 위반을 추가하지 않는다.
