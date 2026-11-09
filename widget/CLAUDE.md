# widget (Glance 홈 위젯)

기획 원문: `docs/run/product-spec.md` 8장, 모듈 상태: `docs/run/module-map.md` 10장, 색: `docs/run/theme-guide.md`, 시안: `docs/run/design/w1/`

## 역할

| 위젯 | 크기 | 표시 | 탭 | 버전 |
|---|---|---|---|---|
| 주간 목표 | 4×2 | "이번 주", `12.4 / 15 km`, 진행바, "달리기 시작" 버튼 | 버튼: 러닝 시작 흐름(P1) / 나머지: 홈 | v1 |
| 주간 목표 (작게) | 2×2 | 진행 링 + 거리 | 홈 | v1 |
| 최근 기록 | 4×2 | 최근 기록의 흰 선 경로 + 거리 · 날짜 | 기록 상세 | v2 |

러닝 중 상태는 위젯에 보여주지 않는다. 진행 중 기록은 포그라운드 서비스 알림이 담당한다.

## 규칙

- 위젯은 `core:data`의 Repository만 쓴다. `core:database`를 직접 읽지 않는다.
- 주간 합계 · 목표 진행률 계산은 `core:domain`(`WeeklyProgress`)을 그대로 쓴다. 위젯 안에서 따로 계산하지 않는다.
- 색은 앱과 같은 팔레트(`AppColors`)를 Glance `ColorProvider`로 옮겨 쓴다. 기기 다크 모드를 따른다. `Color(0xFF…)` 하드코딩 금지.
- 탭 동작은 `core:navigation`의 `AppIntents`(`EXTRA_START_RUN`, `EXTRA_OPEN_RUN_ID`)로 `MainActivity`를 열고, 백스택은 app이 만든다. 위젯이 직접 추적 서비스를 시작하지 않는다 (서비스는 앱 화면에서 권한 · GPS 확인 후에만 시작).
- 경로 그림(v2)은 Glance가 Canvas를 지원하지 않으므로 `RouteProjector`로 투영한 점을 `Bitmap`에 그려 `Image`로 넣는다. RemoteViews 크기 한도 때문에 비트맵은 짧은 변 360px 이하.
- Koin은 리시버 · Worker 같은 Android 진입점에서만 `KoinComponent`로 꺼낸다.
- 문자열은 `strings.xml` (`widget_{purpose}`), 숫자는 `RunFormat`으로 포맷한다.

## 갱신 시점

| 계기 | 방법 |
|---|---|
| 기록 저장 · 삭제 · 복원 완료 | `WidgetUpdater.updateAll()` (`core:data`의 `HomeWidgets` 인터페이스 구현) |
| 목표 · 테마 변경 | 같음 |
| 주가 바뀔 때 (월요일 00:00) | WorkManager 1회 작업을 매주 다시 예약 |
| 시각 · 시간대 변경 | `TIME_SET` · `TIMEZONE_CHANGED` 브로드캐스트 |

## 완료 전 확인

- 크기별 Glance Preview (라이트 · 다크, 목표 0% · 83% · 100% 초과)
- 주 경계 테스트: 일요일 23:59 → 월요일 00:00, 시간대 변경
- 실기기: 위젯 추가 → 러닝 저장 후 갱신되는지, 앱 프로세스가 죽은 뒤에도 탭이 동작하는지
