# 위치 추적 설계

러닝앱에서 가장 중요한 부분입니다. 화면보다 이 구조가 앱의 신뢰도(기록이 끊기지 않는가, 거리가 맞는가)를 결정합니다. 사용자에게 보이는 규칙은 [product-spec.md](product-spec.md) 2장, 이 문서는 구현 기준입니다.

> Android · Play 정책(포그라운드 서비스, 위치 권한, 배터리 최적화)은 자주 바뀝니다. 수치와 정책은 2026-11 기준 초안이며, 구현 착수 · 출시 전에 공식 문서로 다시 확인합니다.

## 목차

1. 원칙
2. 구성 요소
3. 상태와 소유권
4. 위치 수집 · 점 필터
5. 계산
6. 저장 · 복구
7. 서비스 · 권한 · 알림
8. 테스트
9. 음성 안내
10. 경로 그리기 · 서버용 경로

---

## 1. 원칙

| 원칙 | 내용 |
|---|---|
| 기록은 끊기지 않는다 | 위치 타입 포그라운드 서비스 + 몇 초 단위 Room 저장. 프로세스가 죽어도 마지막 몇 초만 잃는다 |
| 화면은 추적을 모른다 | 화면은 `RunTracker.state`(Flow)와 Room만 구독. 화면이 사라져도 추적은 계속 |
| 계산은 한 곳 | 거리 · 페이스 · 스플릿 · 칼로리 · 자동 일시정지는 `core:domain` 순수 함수. 서비스 · 화면 · 테스트가 같은 코드를 쓴다 |
| 권한은 최소 | 정밀 위치(사용 중에만) + 알림 + 포그라운드 서비스. **백그라운드 위치 권한 없음** |
| 시간은 단조 시계 | 운동 시간은 `SystemClock.elapsedRealtime`으로 잰다. 기기 시각 변경 · 시간대 변경에 흔들리지 않게 |
| 테스트는 재생 | 위치 제공자를 인터페이스로 감싸 녹화된 GPX를 재생해 검증 |

## 2. 구성 요소

```
feature:run (P1~P8 화면)          widget · 알림 버튼
        │ RunTracker (interface)        │ PendingIntent
        ▼                               ▼
┌────────────────── core:tracking ───────────────────────┐
│ RunTrackerImpl ── start/pause/resume/finish ──▶ RunTrackingService (FGS, location)
│   state: StateFlow<TrackingState>  ◀──────────── TrackingEngine
│                                                  ├─ LocationSource (core:location)
│                                                  ├─ PointFilter · AutoPauseDetector (core:domain)
│                                                  ├─ RunMetricsAccumulator (core:domain)
│                                                  ├─ RunRecorder ──▶ RunTrackingDao (core:database)
│                                                  ├─ VoiceAnnouncer (TextToSpeech)
│                                                  └─ TrackingNotification
└────────────────────────────────────────────────────────┘
core:location: LocationSource = FusedLocationSource(실제) | GpxReplaySource(테스트 · 디버그)
```

| 구성 요소 | 모듈 | 역할 |
|---|---|---|
| `RunTracker` | core:tracking (공개 인터페이스) | 화면이 쓰는 유일한 입구: `state`, `prepare()`, `start()`, `pause()`, `resume()`, `finish(save)`, `recoverableRun()` |
| `RunTrackingService` | core:tracking | 포그라운드 서비스 껍데기. 알림 유지 · 생명주기만. 로직은 `TrackingEngine` |
| `TrackingEngine` | core:tracking | 점 받기 → 필터 → 누적 계산 → 저장 · 알림 · 음성 호출. Android 의존 최소화(테스트 대상) |
| `LocationSource` | core:location | `locations(): Flow<RawLocation>`(요청 값은 4-1 고정), `lastKnown()`, `isLocationEnabled()` |
| `PointFilter`, `AutoPauseDetector`, `RunMetricsAccumulator`, `SplitCalculator`, `PaceWindow`, `CalorieEstimator` | core:domain | 순수 Kotlin 계산 |
| `RunRecorder` | core:tracking | 점 버퍼 → Room 트랜잭션 (6장) |
| `VoiceAnnouncer` | core:tracking | 안내 시점 판단(도메인) + TTS 재생 |

- feature는 `core:tracking`의 `RunTracker`만 참조하고, `core:location` · `core:database`를 직접 참조하지 않습니다.
- 기록이 끝난 뒤 목록 · 상세 · 통계는 `core:data`의 `RunRepository`가 담당합니다 (추적과 분리).

## 3. 상태와 소유권

**추적 상태의 원본은 `RunTrackerImpl`의 `StateFlow`(앱 범위 싱글턴)이고, 영속 원본은 Room의 `run` 행입니다.** ViewModel은 이것을 화면 State로 옮기기만 하고 시간 · 거리를 계산하지 않습니다.

```kotlin
sealed interface TrackingState {
    data object Idle : TrackingState
    data class Preparing(val gps: GpsStatus) : TrackingState          // P1-c
    data class Recording(
        val runId: String,
        val metrics: LiveMetrics,          // distanceM, movingMs, currentPaceSecPerKm, avgPaceSecPerKm
        val route: List<RoutePoint>,       // 화면용 (단순화된 점, 최근 점은 그대로)
        val pause: PauseState,             // Running | AutoPaused | UserPaused
        val gps: GpsStatus,                // GOOD | WEAK | LOST
    ) : TrackingState
    data class Finishing(val runId: String) : TrackingState
}
```

- 화면의 시간 표시는 1초 단위로 다시 그려야 하므로, `LiveMetrics`에 `movingMsAtEmit` + `emittedAtElapsed`를 담고 화면이 `elapsedRealtime` 차이로 보간합니다 (점이 늦게 와도 시계는 매끄럽게).
- 카운트다운(P2)은 화면 상태입니다. 끝나는 순간 `start()`를 호출합니다.
- 상태 전이: `Idle → Preparing → Recording(Running) ⇄ Recording(Paused) → Finishing → Idle`. 그 외 전이는 무시하고 로그만 남깁니다.

## 4. 위치 수집 · 점 필터

### 4-1. 요청 값

| 값 | 기록 중 | 준비(P1-c) |
|---|---|---|
| 우선순위 | 높은 정확도 (`PRIORITY_HIGH_ACCURACY`) | 같음 |
| 간격 | 1초 | 1초 |
| 최소 갱신 간격 | 1초 | 1초 |
| 최소 이동 거리 | 0m (필터에서 처리) | 0m |
| 정확한 위치 대기 | 하지 않음 | 하지 않음 |

- GPS 준비 판정(`GpsStatusEvaluator`, core:domain): 점이 아직 없으면 `LOST`. 최근 3개 점 중 정확도(`accuracy`) ≤ 20m가 2개 이상이면 `GOOD`, 20~50m `WEAK`, 10초간 점 없음 `LOST`.
- 대략 위치만 허용된 상태(정확도 수백 m)는 `WEAK`로 보고 P1-a 안내로 돌려보냅니다.

### 4-2. 점 필터 (`PointFilter`, 순서대로)

| 순서 | 버리는 조건 | 초기값 | 이유 |
|---|---|---|---|
| 1 | 정확도 > 25m | 25m | 건물 사이 반사 · 실내 |
| 2 | 점 시각이 이전 점보다 같거나 이름 · 10초 넘게 오래된 점 | – | 캐시된 위치, 순서 뒤바뀜 |
| 3 | 마지막 **채택** 점 대비 속도 > 12 m/s (43 km/h) | 12 m/s | 순간 점프 (사람이 낼 수 없는 속도) |
| 4 | 이전 **채택** 점과 거리 < max(3m, 정확도 × 0.5) | 3m | 서 있을 때 흔들림(지터)이 거리로 쌓이는 것 방지 |

- 버린 점도 `run_point`에 `accepted = 0`으로 저장합니다 (나중에 필터 값을 조정할 때 재계산 · 분석용, 기기 밖으로 나가지 않음).
- 4번에서 버린 점은 거리에는 넣지 않지만 자동 일시정지 판단에는 씁니다 (멈춰 있다는 신호).
- 초기값은 실기기 · GPX 재생으로 조정하고, 바꾸면 이 표와 테스트 기대값을 함께 고칩니다.

### 4-3. 신호 끊김 (GPS 공백)

| 공백 | 처리 |
|---|---|
| 30초 이하 | 끊기기 전 점과 다시 잡힌 점을 직선으로 이어 거리에 포함 |
| 30초 초과 | 새 구간(`segment` +1)으로 시작. 공백 동안 거리는 넣지 않고 시간은 계속 (자동 일시정지가 켜져 있어도 신호 없음은 정지로 보지 않음). 경로 선은 끊긴 구간을 `route-line-muted` 점선으로 |

터널 · 지하 구간에서 거리가 짧게 나오는 것은 지도 없이 보정할 방법이 없으므로 그대로 둡니다.

## 5. 계산 (`core:domain`)

| 항목 | 방법 |
|---|---|
| 거리 | 채택된 연속 점 사이 하버사인 거리의 합. 같은 `segment` 안에서만 (4-3 예외) |
| 운동 시간 | `Running` 상태로 있었던 `elapsedRealtime` 구간의 합. 일시정지 · 자동 일시정지 시간 제외 |
| 경과 시간 | 종료 시각 − 시작 시각 (벽시계, 표시용) |
| 평균 페이스 | 운동 시간(초) ÷ 거리(km). 거리 < 50m면 표시하지 않음 |
| 현재 페이스 (`PaceWindow`) | 최근 200m 또는 최근 30초 중 **먼저 채워지는 창**의 평균. 창이 덜 찼으면 `null` (`–′––″`). 20′/km보다 느리면 `null` |
| 구간 (`SplitCalculator`) | 누적 거리가 1,000m 배수를 넘는 순간을 두 점 사이 **선형 보간**으로 찾아 그 시점의 운동 시간으로 자름. 마지막 남은 구간은 실제 거리 + 환산 페이스 (10m 미만이면 표에 넣지 않음) |
| 칼로리 (`CalorieEstimator`) | `체중kg × 거리km × 1.036`. 체중 없으면 `null` |

### 5-1. 자동 일시정지 (`AutoPauseDetector`)

| 전이 | 조건 (초기값) |
|---|---|
| Running → AutoPaused | 최근 6초 동안 이동 거리 < 3m (점의 `speed`가 아니라 위치 차이로 판단, 기기마다 speed 신뢰도가 달라서) |
| AutoPaused → Running | 연속 2구간 모두 이전 점 대비 속도 ≥ 1.2 m/s 이고, 2구간 전 점에서 이동한 거리 ≥ 4m |

- 자동 재개 시 멈춘 지점과 재개 지점 사이 거리는 넣습니다 (신호 대기 중 몇 걸음 이동).
- 사용자가 직접 누른 일시정지(`UserPaused`)는 자동 재개하지 않습니다.
- 자동 일시정지 · 재개가 10초 안에 반복되면(신호등 앞 서성임) 음성 안내를 생략합니다.

## 6. 저장 · 복구

### 6-1. 쓰기 주기 (`RunRecorder`)

| 시점 | 쓰는 것 |
|---|---|
| `start()` | `run` 행 생성 (`status = ACTIVE`, `started_at`, `owner_user_id`) |
| 5초마다 또는 점 10개마다 (먼저 오는 쪽) | 버퍼의 `run_point` 일괄 삽입 + `run` 행의 누적값(거리, 운동 시간, 마지막 점 시각) 갱신을 **한 트랜잭션**으로 |
| 일시정지 · 재개 | 버퍼 즉시 비우기 + `run.status` 변경 + `pause_count` |
| `finish(save = true)` | 버퍼 비우기 → 최종 계산(스플릿 · 칼로리 · 화면용 polyline) → `run_split` 삽입 → `status = FINISHED`, `sync_state = PENDING`(로그인 시) |
| `finish(save = false)` | `status = DISCARDED` (점은 정리 작업이 하루 뒤 삭제) |

- 쓰기는 `TrackingEngine`의 단일 코루틴에서 순서대로 합니다 (경합 없음).
- 1시간 러닝 ≈ 점 3,600개 ≈ 트랜잭션 720번. Room 쓰기 비용은 배터리에 영향이 작지만, 실기기 측정 후 10초 주기로 늘릴 수 있습니다.

### 6-2. 복구 (P8)

프로세스가 죽거나(시스템 · 사용자 강제 종료) 기기가 재부팅되면, 위치 권한이 "사용 중에만"이라 **서비스를 백그라운드에서 스스로 다시 띄울 수 없습니다.** 그래서 서비스는 `START_NOT_STICKY`로 두고, 사용자가 앱을 다시 열 때 복구합니다.

```
앱 시작 → RunTracker.recoverableRun(): status in (ACTIVE, PAUSED)인 run이 있나?
  ├─ 없음 → 평소대로
  └─ 있음 → app이 [HomeKey, RunKey(recoverRunId)]로 시작 → P8 다이얼로그
        ├─ 이어서 달리기 → 같은 runId, segment +1, 죽어 있던 시간은 일시정지로 처리 → P1-c → Recording
        ├─ 저장하고 마치기 → 마지막 저장된 점까지로 finish(save = true) → P6
        └─ 기록 버리기 → finish(save = false) → 홈
```

- 복구 시 운동 시간은 Room에 저장된 누적 운동 시간에서 이어갑니다 (`elapsedRealtime`은 재부팅 시 초기화되므로 저장값만 믿음).
- 진행 중 기록이 24시간 넘게 방치됐다면 다이얼로그에 "어제 시작한 기록이에요"를 붙이고 기본 버튼을 "저장하고 마치기"로 합니다.

## 7. 서비스 · 권한 · 알림

### 7-1. 매니페스트

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<!-- ACCESS_BACKGROUND_LOCATION, REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 는 선언하지 않는다 -->

<service
    android:name=".RunTrackingService"
    android:exported="false"
    android:foregroundServiceType="location" />
```

### 7-2. 시작 · 종료 규칙

- 서비스는 **앱이 화면에 보이는 상태에서 사용자 동작(카운트다운 끝 · "이어서 달리기")으로만** 시작합니다. 위젯 · 알림 버튼은 앱 화면을 열 뿐 서비스를 직접 시작하지 않습니다.
- `startForegroundService()` 직후 서비스는 즉시 `startForeground()`를 호출합니다 (알림을 먼저 띄우고 위치 요청은 그 다음).
- 시작 전 확인: 정밀 위치 권한, 기기 위치 켜짐. 없으면 서비스를 띄우지 않고 P1로.
- 종료: `finish()` → 버퍼 저장 → 위치 요청 해제 → `stopForeground(REMOVE)` → `stopSelf()`.
- 앱을 최근 앱 목록에서 밀어도 서비스는 계속됩니다 (포그라운드 서비스). 이것이 기대 동작이며, 알림의 "열기"로 돌아옵니다.

### 7-3. 배터리 최적화

- 일부 제조사는 포그라운드 서비스도 화면이 꺼지면 멈춥니다. P1-b에서 한 번 안내하고, **시스템 설정 목록 화면**(`ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`)을 엽니다.
- 앱을 직접 예외로 넣어달라는 요청 권한(`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)은 Play 정책상 허용 용도가 제한적이라 쓰지 않습니다.
- 이미 예외인지(`PowerManager.isIgnoringBatteryOptimizations`)는 확인해서, 예외면 P1-b를 건너뜁니다.

### 7-4. 진행 중 알림 (N1)

| 항목 | 값 |
|---|---|
| 채널 | `tracking` (중요도 낮음, 소리 · 진동 없음) |
| 시간 | `setUsesChronometer(true)` + `setWhen(시작 시점 보정값)` → 시스템이 1초마다 그림. 일시정지 시 크로노미터 끔 |
| 거리 · 페이스 | 5초마다 갱신 (`setOnlyAlertOnce(true)`) |
| 버튼 | 일시정지 / 재개, 열기 |
| 탭 | `MainActivity` + `AppIntents.EXTRA_OPEN_RUN` → P3 / P4 |

- 알림 권한이 없어도 포그라운드 서비스는 동작합니다 (알림만 안 보임). P1-a에서 알림 권한을 거부하면 "진행 상황을 알림에서 볼 수 없어요" 한 줄만 안내합니다.

### 7-5. Play Console 신고

- 포그라운드 서비스 유형(위치) 사용 신고: 용도 설명 + 사용 장면 영상(시작 → 화면 끔 → 알림 → 종료).
- 데이터 보안 섹션: 정밀 위치(수집, 기기 저장, 로그인 시 단순화한 경로를 백업 목적으로 전송), 이메일(계정), 건강 · 피트니스(운동 기록).
- 백그라운드 위치 권한을 쓰지 않으므로 해당 신고는 없습니다.

## 8. 테스트

### 8-1. GPX fixture (`core:domain/src/testFixtures/resources/gpx/`)

`core:domain`은 JVM 모듈이라 Android 라이브러리인 `core:testing`을 참조할 수 없으므로 fixture는 `core:domain`의 testFixtures에 둡니다. `python3 scripts/generate-gpx-fixtures.py`로 만들고, 정확도는 `<extensions><accuracy>`에 넣습니다.

| 파일 | 내용 | 확인하는 것 |
|---|---|---|
| `straight_5km.gpx` | 일정 속도 직선 5km, 1초 간격 | 거리 오차 ±1%, 스플릿 5개 · 각 5′00″ |
| `loop_with_spikes.gpx` | 3km 루프 + 정확도 80m 점 · 순간 점프 섞음 | 필터 후 거리 오차 ±2% |
| `standing_jitter.gpx` | 5분간 제자리, 정확도 5~15m 흔들림 | 거리 증가 < 10m, 자동 일시정지 진입 |
| `traffic_light.gpx` | 달리다 40초 정지 후 재개 × 3 | 자동 일시정지 3회, 운동 시간에서 정지 시간 제외 |
| `tunnel_gap.gpx` | 중간에 20초 · 60초 신호 공백 | 30초 이하는 직선 연결, 초과는 새 segment |
| `long_60min.gpx` | 60분 · 약 10km | 메모리 · 저장 횟수, polyline 500점 이하 |

- 기대값은 fixture를 만들 때 함께 계산해 `*.expected.json`으로 둡니다.
- 실제 러닝 GPX를 추가할 때는 **좌표를 원점 근처로 평행 이동**해서 개인 위치가 저장소에 남지 않게 합니다.

### 8-2. 단위 테스트

| 대상 | 방법 |
|---|---|
| `PointFilter`, `PaceWindow`, `SplitCalculator`, `AutoPauseDetector`, `CalorieEstimator` | 경계값 (정확도 25m · 25.1m, 1,000m 정확히 통과, 페이스 20′ 경계) |
| `RunMetricsAccumulator` | GPX fixture 재생 → expected.json 비교 |
| `TrackingEngine` | `FakeLocationSource` + `FakeElapsedClock` + Room in-memory: 시작 → 일시정지 → 재개 → 종료, 저장 주기, 복구 |
| `PolylineCodec`, `PolylineSimplifier`, `RouteProjector` | 왕복(인코딩 → 디코딩) 오차, 점 수 상한, 투영 비율 유지 |
| `VoiceAnnouncer` 시점 판단 | 1km · 0.5km · 5분 간격, 자동 일시정지 반복 시 생략 |

### 8-3. 실기기 확인 (추적 관련 PR마다 PR 본문에 기록)

- [ ] 화면 끈 상태 30분 기록 → 거리 · 경로가 끊기지 않음 (제조사 2곳 이상: 예 삼성 · 픽셀)
- [ ] 러닝 중 최근 앱에서 밀기 → 알림 유지 · 기록 계속
- [ ] 러닝 중 `adb shell am kill` / 설정에서 강제 종료 → 다시 열면 P8
- [ ] 위치 권한 거부 · 대략 위치만 허용 · 기기 위치 꺼짐 → P1 안내
- [ ] 음악 앱 재생 중 음성 안내 → 덕킹 후 복귀
- [ ] 디버그 빌드의 `GpxReplaySource`로 실내에서 전체 흐름 재생 (개발자 옵션 메뉴)

## 9. 음성 안내 (`VoiceAnnouncer`)

- 시점 판단은 `core:domain`의 `AnnouncementScheduler`(간격 설정, 누적 거리 · 운동 시간 → 안내할 내용 or null).
- 재생: `TextToSpeech`(한국어), 오디오 속성 `USAGE_ASSISTANCE_NAVIGATION_GUIDANCE`, 오디오 포커스 `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`, 재생이 끝나면 포커스 반환.
- 한국어 음성 데이터가 없으면 처음 한 번 "음성 안내를 쓰려면 한국어 음성 데이터가 필요해요" + 설치 화면 이동을 M3에 표시하고, 기록은 음성 없이 계속.
- 문구는 `strings.xml`의 plurals · 형식 문자열로 (`run_voice_split`: "%1$s킬로미터. %2$s. 평균 페이스 %3$s."). 숫자 읽기는 "5분 52초"처럼 단위를 붙여 TTS가 시각으로 읽지 않게 합니다.

## 10. 경로 그리기 · 서버용 경로

### 10-1. 투영 (`RouteProjector`, core:domain)

- 지도가 없으므로 정확한 지도 투영이 필요 없습니다. 기록 범위의 평균 위도 φ에서 `x = (lng − minLng) × cos φ`, `y = (maxLat − lat)`로 평면화하고, 가로세로 비율을 유지한 채 대상 영역(패딩 포함)에 맞춥니다.
- 결과는 0~1 정규화 좌표(`ProjectedRoute`: `NormalizedPoint` 목록, segment 정보 포함). 그리는 쪽이 픽셀로 곱합니다.
- 경로가 아주 짧으면(범위 < 30m) 중앙에 점 하나로 표시합니다.

### 10-2. 그리기 (`RouteCanvas`, core:designsystem)

- 배경 `routeCanvas`, 선 `routeLine` 3~4dp(썸네일은 비율로), 둥근 끝 · 둥근 이음, 시작점 `routeStart` 채운 원, 끝점 `routeLine` 테두리 원.
- 신호 공백 구간은 `routeLineMuted` 점선. 일시정지 중 이동한 구간도 같은 표현.
- 러닝 중(P3)은 새 점이 올 때마다 다시 그리되, 점 수가 많아지면 화면용 단순화(픽셀 기준 허용 오차 1px)를 적용합니다.
- 썸네일 · 공유 카드 · 결과 화면은 `run.route_polyline`(10-3의 단순화 경로)을 쓰고 `run_point`를 읽지 않습니다 (목록 스크롤 성능).

### 10-3. 단순화 · 인코딩 (서버 백업 · 썸네일 공용)

1. 채택된 점만, segment별로 Douglas-Peucker 단순화. 허용 오차 5m에서 시작.
2. 전체 점 수가 500을 넘으면 허용 오차를 1.5배씩 올려 500 이하가 될 때까지 반복.
3. Google Encoded Polyline(정밀도 1e-5)으로 인코딩. segment 구분은 별도 필드(`routeSegments`: 각 segment의 점 개수 배열).
4. 결과를 `run.route_polyline` · `run.route_segments`에 저장하고, 백업 시 그대로 올립니다.

- 500점 · 인코딩 기준 약 3~4KB로 Firestore 문서 1MiB 한도와 거리가 멀고, 보안 규칙의 20,000자 상한 안에 들어갑니다.
- 원시 점(`run_point`)은 서버로 보내지 않습니다. 다른 기기에서 복원한 기록은 단순화 경로만 있으므로, 상세 화면의 페이스 그래프는 저장된 스플릿으로 그립니다 (점 단위 그래프를 쓰지 않는 이유).
