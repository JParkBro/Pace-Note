# 인앱 업데이트 (Play In-App Updates)

Play 스토어에 새 버전이 있으면 **앱 시작 시 컨펌 다이얼로그 → 수락하면 백그라운드 다운로드 → 다운로드가 끝나면 재시작 컨펌 → 설치**하는 구조. FLEXIBLE 방식만 쓴다.

AniPick(`../anipick/docs/in-app-update.md`)의 구현을 이 앱의 규칙(MVI `MviViewModel`, `strings.xml`, `suspendRunCatching`, `Run*` 컴포넌트)에 맞춰 옮겼다. 다른 점은 **러닝 중에는 다이얼로그를 띄우지 않는다**는 것 하나다.

> 2026-11-23 결정: 이 FLEXIBLE 흐름은 Remote Config · 콘솔 설정 없이 Play 라이브러리만 쓰므로 **1.x에 넣는다** (원래 계획은 2.0.0 `cloud`). 같은 날, Remote Config로 정하는 강제 업데이트(`RELEASE_AND_UPDATE.md` 6장의 IMMEDIATE · U1 강제 업데이트 화면)는 **2.0.0에서도 하지 않기로** 했다. M8 앱 정보만 `module-map.md` 9장 10단계(2.0.0)에 남는다. 공통 문서와 다른 부분은 `README.md` "공통 문서와 다르게 가는 부분".

## 한눈에 보기

```
onCreate (savedInstanceState == null) ── AppStarted
   │  UPDATE_AVAILABLE && isFlexibleUpdateAllowed
   ▼
dialog = Available(info) ──▶ "업데이트 안내"  [업데이트] [나중에 → None]
   │ 업데이트
   ▼
Event.StartUpdateFlow(info) ──▶ MainActivity: startUpdateFlowForResult(FLEXIBLE)
   │  Play 시스템 화면에서 동의 → 백그라운드 다운로드 (앱은 계속 사용 가능)
   ▼
InstallStateUpdatedListener: DOWNLOADED ──┐
onResume ── AppResumed: DOWNLOADED ───────┤
AppStarted: DOWNLOADED ───────────────────┤
                                          ▼
dialog = ReadyToInstall ──▶ "설치 준비 완료"  [재시작] [나중에 → None, 다음 onResume에 다시 뜸]
   │ 재시작
   ▼
Event.CompleteUpdate ──▶ MainActivity: appUpdateManager.completeUpdate() → 앱 재시작 · 설치

러닝 중(RunTracker.state가 Idle이 아님)에는 dialog를 None으로 내보내고, Idle로 돌아오면 그때 띄운다.
```

## 정해 둔 것

- **FLEXIBLE만 쓴다.** IMMEDIATE(전체 화면, 끝날 때까지 앱 사용 불가)는 쓰지 않는다. 강제 업데이트는 하지 않는다 (이유는 `README.md` "공통 문서와 다르게 가는 부분", 필요해지면 맨 아래 "확장").
- **Play 화면을 바로 띄우지 않고 앱 다이얼로그로 먼저 묻는다.** 다운로드 후 재시작도 한 번 더 묻는다. 컨펌 없이 업데이트 · 재시작하지 않는다.
- **러닝 중에는 띄우지 않는다.** 재시작(`completeUpdate()`)은 프로세스를 끝내 추적 포그라운드 서비스까지 멈추고, 러닝 화면을 가리는 것도 안 된다. 판단은 `RunTracker.state`(`TrackingState.Idle`인지)로 한다.
- **판단 · 상태는 ViewModel, 실행은 Activity.** `startUpdateFlowForResult()`는 Activity에 묶인 `ActivityResultLauncher`가 필요하고 `completeUpdate()`는 앱을 재시작시키므로, ViewModel은 Event만 보내고 MainActivity가 호출한다.
- **다이얼로그는 MainActivity 최상위 `Box` 안, `AppNavDisplay` 위(마지막 자식)에 띄운다.** 어느 화면에 있든 뜬다.
- **실패는 조용히 넘긴다 (`suspendRunCatching`).** 업데이트 확인 실패가 앱 사용을 막으면 안 된다. Play에서 설치하지 않은 빌드(Android Studio 설치 등)는 여기서 실패하거나 `UPDATE_NOT_AVAILABLE`이 온다.
- **app 모듈에만 둔다.** feature 모듈은 이 기능을 모른다.
- **안내 빈도 제한은 두지 않는다.** 새로 시작할 때마다 묻는다. 회전 · 다크 모드 변경으로 Activity가 다시 만들어질 때는 묻지 않는다 (`savedInstanceState == null`일 때만 `AppStarted`).
- 문구는 `app/src/main/res/values/strings.xml`의 `app_update_*`.

## 파일 구성

```
app/src/main/java/jpark/bro/pacenote/
├── update/
│   ├── UpdateState.kt       # UpdateState(dialog) + UpdateDialog: None / Available(info) / ReadyToInstall
│   ├── UpdateAction.kt      # AppStarted · AppResumed + 다이얼로그 버튼 4개
│   ├── UpdateEvent.kt       # MainActivity가 실행할 것 2개
│   ├── UpdateViewModel.kt   # MviViewModel. AppUpdateManager · RunTracker 주입
│   └── UpdateDialogs.kt     # AlertDialog + RunPrimaryButton / RunSecondaryButton
├── di/AppModule.kt          # AppUpdateManager single + viewModelOf(::UpdateViewModel)
└── MainActivity.kt          # 런처, AppStarted / AppResumed, Event 처리, 다이얼로그
app/src/test/kotlin/jpark/bro/pacenote/update/UpdateViewModelTest.kt
```

## 1. 의존성

`gradle/libs.versions.toml`

```toml
[versions]
appUpdate = "2.1.0"

[libraries]
app-update = { module = "com.google.android.play:app-update", version.ref = "appUpdate" }
app-update-ktx = { module = "com.google.android.play:app-update-ktx", version.ref = "appUpdate" }
```

`app/build.gradle.kts`: `implementation(libs.app.update)`, `implementation(libs.app.update.ktx)`. `-ktx`는 `requestAppUpdateInfo()`(suspend)와 `isFlexibleUpdateAllowed` 확장 때문에 쓴다. `MviViewModel` · `ObserveEvents`(`core:ui`), `suspendRunCatching`(`core:common`)도 app의 `implementation`이다.

## 2. State / Action / Event

- 다이얼로그 두 개가 동시에 뜨지 않도록 상태를 Boolean 여러 개가 아니라 sealed interface `UpdateDialog` 하나로 둔다.
- 화면 입력은 `onAction` 하나로만 받는다 (CLAUDE.md MVI 규칙). 그래서 AniPick의 `checkForUpdate()` · `checkDownloadedUpdate()` 공개 함수 대신 `UpdateAction.AppStarted` · `AppResumed`를 쓴다.

| Action | 하는 일 |
|---|---|
| `AppStarted` | `requestAppUpdateInfo()`. 이미 `DOWNLOADED`면 `ReadyToInstall`, `UPDATE_AVAILABLE` + FLEXIBLE 허용이면 `Available(info)` |
| `AppResumed` | `requestAppUpdateInfo()`. `DOWNLOADED`면 `ReadyToInstall` |
| `UpdateConfirmed` | `Event.StartUpdateFlow(info)` + 닫기 |
| `InstallConfirmed` | `Event.CompleteUpdate` + 닫기 |
| `UpdateDismissed` · `InstallDismissed` | 닫기 |

## 3. ViewModel

- 띄울 다이얼로그는 private `pending: MutableStateFlow<UpdateDialog>`에 두고, `combine(pending, runTracker.state)`로 **추적 상태가 `Idle`일 때만** `state.dialog`에 내보낸다. 러닝 중 다운로드가 끝나도 러닝이 끝난 뒤에 뜬다.
- `InstallStateUpdatedListener`는 Activity가 아니라 ViewModel 생명주기에 건다 (`init`에서 등록, `onCleared()`에서 해제). 회전해도 등록이 유지되고, 해제를 빼먹을 일이 없다.
- 버튼을 누르면 `pending`을 바로 `None`으로 돌려 다이얼로그를 즉시 닫는다.

## 4. DI (Koin)

```kotlin
single<AppUpdateManager> { AppUpdateManagerFactory.create(androidContext()) }
viewModelOf(::UpdateViewModel)
```

ViewModel과 MainActivity가 **같은 `AppUpdateManager` 인스턴스**를 써야 리스너가 다운로드 상태를 받으므로 `single`로 둔다. `KoinModulesTest`(`verify()`)로 연결을 확인한다.

## 5. MainActivity 연결

- `updateFlowLauncher = registerForActivityResult(StartIntentSenderForResult()) {}`를 **필드**에 둔다 (STARTED 전에 등록해야 한다). 결과는 쓰지 않는다. Play 화면에서 취소해도 할 일이 없다 (다음 실행 때 다시 묻는다).
- `onCreate`: `savedInstanceState == null`일 때 `AppStarted`. `onResume`: `AppResumed`.
- `setContent`에서 `ObserveEvents(updateViewModel.event)`로 `StartUpdateFlow` → `startUpdateFlowForResult(info, launcher, AppUpdateOptions.newBuilder(FLEXIBLE).build())`, `CompleteUpdate` → `completeUpdate()`.
- `AppTheme { Box { AppNavDisplay(); UpdateDialogs(state.dialog, updateViewModel::onAction) } }`. 다이얼로그가 `Box`의 **마지막 자식**이어야 화면 위에 그려진다.

## 6. 알고 있는 동작 (버그 아님, 바꿀 때 참고)

| 상황 | 동작 | 바꾸려면 |
|---|---|---|
| "업데이트 안내"에서 "나중에" | 이번 실행 동안 다시 안 묻고, 다음에 새로 시작할 때 다시 묻는다 | 간격을 두려면 맨 아래 "확장" |
| "설치 준비 완료"에서 "나중에" | 앱으로 돌아올 때(`onResume`)마다 다시 뜬다. 다운로드된 업데이트는 설치를 계속 유도하라는 Play 권장 동작 | 덜 보이게 하려면 스낵바(액션 "재시작")로 바꾼다 |
| 러닝 중 다운로드 완료 | 러닝 중에는 안 뜨고, 종료 · 저장 후 추적 상태가 `Idle`이 되면 뜬다 | – |
| 다운로드 중 앱 종료 | Play가 다운로드를 계속하고, 다음 실행의 `AppStarted` / `AppResumed`에서 `DOWNLOADED`를 잡아 재시작 컨펌을 띄운다 | – |
| 디버그 · Android Studio 설치 빌드 | 업데이트 확인이 실패하거나 없음으로 와서 아무것도 안 뜬다 | – |

## 7. 확인 방법

- **단위 테스트**: `UpdateViewModelTest` (Robolectric). `FakeAppUpdateManager`로 `setUpdateAvailable()` → `userAcceptsUpdate()` → `downloadStarts()` → `downloadCompletes()` 상태 전이, 러닝 중 숨김을 검증한다. `./gradlew :app:testDevDebugUnitTest --tests 'jpark.bro.pacenote.update.*'`
- **실제 다이얼로그 · 다운로드는 Play에서 설치한 빌드에서만 동작한다.** 실기기 확인은 사용자가 한다.
- **내부 앱 공유(Internal app sharing)** 로 확인한다.
  1. versionCode가 낮은 빌드와 높은 빌드를 둘 다 내부 앱 공유에 올린다 (`CLAUDE.md` "AAB 만들기", versionCode만 다르면 된다).
  2. 기기의 Play 스토어 설정에서 "내부 앱 공유"를 켠다.
  3. 낮은 빌드 링크로 설치 → 높은 빌드 링크를 열기만 하고(설치 X) → 앱 실행.
  4. "업데이트 안내" → 업데이트 → 다운로드 → "설치 준비 완료" → 재시작 순서로 뜨는지 본다.
  5. 러닝을 시작한 상태에서 다운로드가 끝나면 다이얼로그가 안 뜨고, 러닝 종료 후 뜨는지 본다.
- 내부 테스트 트랙으로도 확인할 수 있다. 어느 쪽이든 **versionCode가 올라가 있어야** 업데이트로 인식된다.

## 확장 (지금은 안 함)

- **강제 업데이트** (하지 않기로 함. 옛 버전이 서버와 안 맞게 되는 등 꼭 필요해질 때만): `RELEASE_AND_UPDATE.md` 6장. Remote Config `force_update_min_version_code` 미만이면 `AppUpdateType.IMMEDIATE` + 닫을 수 없는 U1 화면. 판단은 남겨 둔 `core:domain`의 `decideUpdate`(`UpdatePolicy`는 `FeatureFlags.updatePolicy`, 지금은 아무도 쓰지 않음)를 쓰고, Remote Config 키 3개(`force_update_min_version_code` · `soft_update_*`)를 다시 등록한다. Play 우선순위(`updatePriority()`)만으로 판단하면 출시 후 값을 못 바꾸므로 기준으로 쓰지 않는다.
- **안내 빈도 조절**: 매 실행마다 묻는 게 부담이면 `decideUpdate`의 `soft_update_interval_days`처럼 마지막 "나중에" 시각을 DataStore에 저장해 간격을 둔다.
- **M8 앱 정보의 "최신 여부"**: `requestAppUpdateInfo()`의 `updateAvailability()`를 쓴다.
