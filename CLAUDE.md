# pacenote (런닝앱)

혼자 꾸준히 뛰는 사람을 위한 러닝 기록 앱. 지도 없이 흰 선 경로로 기록하고, 로그인 없이도 쓸 수 있다. Android 전용.

## 저장소 구조

| 경로 | 내용 |
|---|---|
| 루트 (`app/`, `build-logic/`, 이후 `core/`, `feature/`, `widget/`) | Android 앱 (Kotlin, Compose, 멀티 모듈) |
| `docs/common/` | 범용 규칙 (아키텍처, 컨벤션, 테스트, Git) |
| `docs/run/` | 이 프로젝트의 기획 · 테마 · 운영 결정 |
| `config/detekt/` | detekt 규칙 |
| `scripts/` | `generate-theme-colors.py` (theme-tokens.json → ThemeTokens.kt) |

## 문서 지도

작업 전에 관련 문서를 읽는다. 문서와 코드가 다르면 **문서가 기준**이고, 의도적으로 바꿔야 하면 먼저 사용자에게 확인한다.

| 작업 | 먼저 읽을 문서 |
|---|---|
| 기능 범위 · 버전 · 기능 추가 절차 | `docs/run/roadmap.md` |
| 모듈 추가 · 구조 변경 | `docs/common/ANDROID_ARCHITECTURE.md` |
| 코드 작성 전반 | `docs/common/CODING_CONVENTIONS.md` |
| 테스트 작성 | `docs/common/TESTING.md` |
| UI · 색상 | `docs/run/theme-guide.md`, `docs/run/theme-foundation.md`, 값 원본 `docs/run/assets/theme-tokens.json` |
| 브랜치 · 커밋 · PR · 릴리스 | `docs/common/GIT_CONVENTIONS.md` |

## 절대 어기면 안 되는 결정

- **지도 SDK를 쓰지 않는다.** 경로는 `route-canvas` 위에 Canvas로 그린다.
- **Firebase는 Spark(결제 수단 없음).** Auth · Firestore · Remote Config · App Check · Crashlytics만. 결제 수단을 등록하거나 Blaze 전용 기능(Functions · Storage 등)을 추가하지 않는다. 읽기 · 쓰기를 늘리는 변경은 사용자 1,000명 · 하루 기준 사용량을 계산해 Spark 한도 50% 이내인지 설명한다.
- **로그인은 Google만, 선택 사항.** 로그인 없이 모든 기록 기능이 동작해야 한다.
- **Room이 원본. 원시 GPS 점은 기기에만.** 서버에는 기록 요약 + 단순화한 경로(인코딩 polyline)만 올리고, 기록 1건 = Firestore 문서 1개.
- **Firestore는 필드를 추가만 한다.** 이름 변경 · 삭제 · 타입 변경 금지. 모든 기록 문서에 `schemaVersion`, `source`, `activityType`, `planId`를 둔다 (roadmap 6장).
- **삭제는 soft delete(`deletedAt`).**
- **Room은 `exportSchema = true` + 버전마다 Migration.** `fallbackToDestructiveMigration` 금지.
- **미완성 기능은 기능 플래그(`feature_{name}_enabled`, 기본값 false) 뒤에 둔다.** 플래그는 `core:config`의 `FeatureFlags` 한 곳에서만 읽는다.
- **러닝 중 화면 · 알림에 광고를 넣지 않는다.**
- **ID는 클라이언트 생성 UUID 문자열**, 날짜는 `yyyy-MM-dd`(LocalDate), 시각은 epoch millis. 자동 증가 ID 금지.
- **색은 테마 토큰으로만.** `Color(0xFF…)` 하드코딩 금지. 경로는 항상 `route-canvas` 위에.
- roadmap의 v2 · v3 기능을 v1 작업에 임의로 추가하지 않는다.

## 작업 방식

- 여러 모듈에 걸친 변경, DB 스키마 변경, Firestore 구조 · 보안 규칙 변경, 새 권한 추가는 **먼저 계획을 보여주고** 진행한다.
- 기획이 문서에 없거나 모호하면 추측해서 구현하지 말고 질문한다.
- 새 기능은 roadmap 4장 절차(기능 정의 1페이지 · 영향 점검 체크리스트)를 따른다.
- 결정이 바뀌면 관련 `docs/` 문서도 같은 PR에서 고친다. 공통 규칙에서 벗어나는 결정은 `docs/run/README.md`의 "공통 문서와 다르게 가는 부분"에 기록한다.
- 작업을 끝내기 전에 "완료 전 확인"을 실행한다.

## Git

- 브랜치: `{type}/{이슈번호}-{영문-요약}` (예: `feat/12-run-countdown`)
- 커밋 · PR 제목: `[Type] scope: 요약` (예: `[Feat] tracking: 자동 일시정지 추가`)
  - Type: `Feat` `Fix` `Design` `Refactor` `Perf` `Test` `Docs` `Build` `CI` `Chore` `Revert`
  - 요약은 한국어, "~ 추가 / 수정 / 제거 / 변경"으로 끝낸다
- `main`에 직접 커밋 · push하지 않는다. force push는 본인 작업 브랜치에만 `--force-with-lease`.
- 훅을 우회하는 `--no-verify`를 쓰지 않는다.
- 포맷 변경과 로직 변경을 한 커밋에 섞지 않는다.

## 비밀 정보

다음을 커밋하거나 코드에 넣지 않는다. 필요하면 `local.properties` · `keystore.properties` · GitHub Secrets를 쓴다.

- 키스토어(`*.jks`, `*.keystore`)와 비밀번호 (`keystore.properties`)
- 서비스 계정 JSON, API 비밀 키, 토큰, `google-services.json`
- `local.properties`, `.env`

개인정보(토큰, 이메일, 위치 좌표, 사용자 입력 원문)를 로그에 남기지 않는다.

## 언어

- 사용자에게 하는 설명, 커밋 메시지, PR 본문, 문서: **한국어**
- 코드 식별자, 패키지, 파일명: 영어
- 앱 UI 문자열: `strings.xml`에 한국어로 추가 (하드코딩 금지)

## Android 앱

규칙 원문: `docs/common/ANDROID_ARCHITECTURE.md`, `docs/common/CODING_CONVENTIONS.md`, `docs/common/TESTING.md`

### 스택

Kotlin · Jetpack Compose · Navigation 3 · Koin(`koin-compose-navigation3`) · Room · DataStore · WorkManager · Glance · FusedLocationProvider(포그라운드 서비스) · Credential Manager(Google 로그인) · Firebase(Auth, Firestore, Remote Config, App Check, Crashlytics)

### 빌드 구성

- convention plugin은 `build-logic/` (`pacenote.android.application`, `pacenote.android.library`, `pacenote.android.compose`, `pacenote.jvm.library`, `pacenote.koin`, `pacenote.room`, `pacenote.feature.api`, `pacenote.feature.impl`).
- 버전 · SDK · applicationId는 `gradle/libs.versions.toml`의 `project*` 값. versionCode는 `MAJOR*1_000_000 + MINOR*10_000 + PATCH*100 + 빌드번호`.
- 빌드 변형은 `dev` / `prod` flavor × `debug` / `release`. dev는 applicationId에 `.dev`가 붙는다.
- Gradle 데몬 JVM은 21 (`gradle/gradle-daemon-jvm.properties`). detekt 1.23이 JDK 25에서 동작하지 않는다.
- `google-services` · `firebase-crashlytics` 플러그인은 `app/google-services.json`을 받은 뒤 app에 적용한다.

### 명령어

| 목적 | 명령 |
|---|---|
| 디버그 빌드 | `./gradlew :app:assembleDevDebug` |
| 전체 단위 테스트 | `./gradlew testDevDebugUnitTest` |
| 특정 모듈 테스트 | `./gradlew :feature:{name}:impl:testDevDebugUnitTest` |
| 포맷 자동 수정 | `./gradlew ktlintFormat` |
| 정적 분석 | `./gradlew ktlintCheck detekt lintDevDebug` |
| 테마 색 코드 생성 | `python3 scripts/generate-theme-colors.py` |

### 모듈 규칙

- 기능은 `feature:{name}:api`(NavKey만) / `feature:{name}:impl`(구현)로 나눈다.
- **impl은 다른 기능의 api만 참조한다. impl → impl 금지, api → api 금지.**
- api에는 `@Serializable` NavKey만 둔다. 내부 화면 키는 impl에 `internal`.
- feature는 `core:data`(Repository)로만 데이터에 접근한다. `core:database`, `core:firebase`를 직접 참조하지 않는다.
- `core:model`, `core:common`, `core:domain`은 순수 Kotlin(JVM) 모듈. Android 의존을 넣지 않는다.
- 거리 · 페이스 · 스플릿 · 칼로리 · 자동 일시정지 · 경로 단순화 계산은 `core:domain`에만 둔다. 러닝 화면 · 결과 · 위젯이 모두 이것을 쓴다.

### 화면 (MVI)

화면 하나 = 아래 파일 묶음 (`feature/{name}/impl/.../{name}/impl/`)

```
{Feature}Action.kt     sealed interface — 사용자가 한 일
{Feature}State.kt      data class — 모든 프로퍼티 val + 기본값
{Feature}Event.kt      sealed interface — 일회성 효과. 없으면 파일 생략, 타입 인자는 Nothing
{Feature}ViewModel.kt  MviViewModel<S, A, E> 상속
{Feature}Screen.kt     internal {Feature}Root + private {Feature}Screen + private Preview(@ScreenPreviews)
di/{Feature}Module.kt  viewModelOf + navigation<{Feature}Key>
```

- ViewModel 공개 멤버는 `state`, `event`, `onAction`뿐. 화면 입력은 **`onAction` 하나로만** 받는다.
- `onAction`과 Root의 Event 처리 `when`에는 **`else`를 쓰지 않는다.**
- ViewModel은 `Context`, `Navigator`, Compose 타입을 모른다. **화면 이동은 Event → Root에서 처리.**
- Root는 ViewModel 연결 · State 수집 · Event 처리만 한다. 레이아웃은 Screen에.
- 기능 플래그 ON · OFF 두 상태 모두 ViewModel 테스트를 쓴다.

### Navigation 3 · Koin

- 이동은 `Navigator`(`core:navigation`)의 `goTo` / `goBack` / `replaceAll`.
- `NavDisplay`의 `entryDecorators`에 `rememberViewModelStoreNavEntryDecorator()`를 빼지 않는다.
- Koin은 생성자 주입만. 일반 클래스에서 `KoinComponent` · `get()` 사용 금지 (서비스 · 위젯 리시버 등 Android 진입점만 예외).
- Koin 모듈을 추가 · 수정하면 `KoinModulesTest`(`verify()`)가 통과해야 한다.

### 데이터

- Repository: 스트림은 `observe…(): Flow<T>`, 1회성은 `suspend … : Result<T>`. 예외를 ViewModel까지 던지지 않는다.
- suspend 블록 예외 처리는 `runCatching` 대신 `suspendRunCatching`.
- Room 컬럼명은 snake_case + `@ColumnInfo(name = …)`. 스키마를 바꾸면 Migration + 스키마 JSON 커밋 + 마이그레이션 테스트.
- 현재 시각은 주입받은 `Clock`, 디스패처도 주입받는다.

### UI

- 색은 `AppThemeTokens.colors.*`만 사용. 보조 버튼 · 트랙 · 비활성 칩은 `fill`.
- 러닝 중 큰 숫자(시간 · 거리 · 페이스)는 `textPrimary` + tabular-nums. primary는 시작 · 일시정지 버튼에만.
- 공유 카드는 테마와 독립: 배경 `routeCanvas` 또는 사진 + `imageScrim`, 글자 `onImage`.
- 공통 컴포넌트는 `core:designsystem`의 `component/`를 쓴다. 기능 모듈에 비슷한 컴포넌트를 새로 만들지 않는다.
- 문자열은 `strings.xml` (`{feature}_{screen}_{purpose}`), 아이콘에 `contentDescription`.
- `LazyColumn` 항목에 `key`. 테스트에서 찾을 요소에 `Modifier.testTag("snake_case")`.

### 테스트

| 바꾼 것 | 필요한 테스트 |
|---|---|
| `core:domain` 계산 | 경계값 포함 단위 테스트 (GPS 튐, 일시정지 구간, km 경계 스플릿) |
| ViewModel | 주요 Action의 State · Event 테스트 (성공 · 실패, 플래그 ON · OFF) |
| Screen UI | Preview 추가 · 수정 |
| DB 스키마 | Migration 테스트 |
| Koin 모듈 | `verify()` 통과 |

- Fake는 `core:testing`에 두고 재사용한다. Mock보다 Fake 우선.
- 테스트 이름은 백틱 한국어 문장: `` `일시정지 중 이동한 거리는 합산하지 않음` ``.

### 새 기능(화면) 추가 순서

1. `settings.gradle.kts`에 `:feature:{name}:api`, `:feature:{name}:impl` 추가
2. api: NavKey / impl: Action · State · (Event) · ViewModel · Screen.kt · di 모듈
3. 이동할 기능의 api만 impl 의존성에 추가
4. app: impl 의존성 + `appModule`의 `includes`에 모듈 추가
5. ViewModel 테스트, Preview, Koin verify

### 완료 전 확인

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck detekt
./gradlew testDevDebugUnitTest      # 변경 범위가 작으면 해당 모듈만
```

ktlint · detekt 위반은 코드를 고쳐서 해결한다. baseline 파일에 새 위반을 추가하지 않는다.
