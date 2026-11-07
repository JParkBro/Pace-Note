# 테마 컬러 시스템 가이드 (런닝앱)

> 색상 값의 원본은 `assets/theme-tokens.json`입니다. `assets/theme-tokens.css`는 웹(정책 페이지, 미리보기)용입니다. 구조 · 사용 규칙 · Compose 적용 방식은 데이즈 테마 가이드에서 옮겨 온 [theme-foundation.md](theme-foundation.md)를 따르고, 이 문서는 **색 값과 런닝앱에서 달라진 부분**만 다룹니다.

4개 테마(볼트 라임(`volt`) · 선라이즈(`sunrise`) · 리버 블루(`river`) · 잉크 모노(`ink`))에 각각 라이트 · 다크를 지원해 **총 8개 팔레트**를 제공합니다. 모든 팔레트는 데이즈와 **같은 이름의 토큰 27개**에 경로 표시용 **route 토큰 4개**를 더한 31개를 사용합니다.

## 1. 테마 컨셉

| 키 | 이름 | 무드 | 라이트 primary | 다크 primary |
|---|---|---|---|---|
| `volt` | 볼트 라임 | 형광 라임 포인트의 에너지. 운동화 · 기록계 감성. 기본 테마 후보. | `#1E2810` | `#C8F53A` |
| `sunrise` | 선라이즈 | 새벽 러닝의 해 뜨는 주황. 따뜻한 동기부여. | `#C2410C` | `#FB8A4C` |
| `river` | 리버 블루 | 강변 새벽 공기 같은 청록. 시원하고 차분한 기록 앱. | `#0B6E86` | `#45C9DE` |
| `ink` | 잉크 모노 | 흑백만 쓰는 미니멀. 흰 경로선이 가장 돋보이는 테마. | `#18181B` | `#F4F4F5` |

**기본 테마는 `volt`를 추천합니다.** 라이트 모드에서는 "검은 버튼 + 라임 글자", 다크 모드에서는 "라임 버튼 + 검은 글자"로 뒤집히는 구조라 러닝 기록계 같은 인상이 강하고, 데이즈의 네 테마와도 겹치지 않습니다.

## 2. 데이즈와 달라진 점

| 항목 | 내용 |
|---|---|
| route 토큰 추가 | 지도 없이 경로를 흰 선으로 그리므로, 경로 영역 배경(`route-canvas`)은 **라이트 · 다크 모두 어두운 색**으로 고정합니다. 흰 선이 라이트 모드에서도 보이게 하기 위함입니다. |
| `volt` · `ink` 라이트의 primary | 거의 검정입니다. `on-primary`가 라임(`volt`) · 흰색(`ink`)이라 버튼은 "어두운 바탕 + 밝은 글자"가 됩니다. |
| 진행바 그라데이션 | 라이트 `volt`는 primary(검정)→accent(라임) 그라데이션이 어색하므로 **진행바 채움은 accent 단색**을 씁니다. 나머지 테마는 데이즈처럼 primary→accent. |
| 캘린더 토큰 | v2 달력 보기(R5)에서 사용하므로 그대로 둡니다. |
| 고정 색 | 카카오 색은 v1에서 카카오 로그인을 쓰지 않으므로 뺐습니다. |

### route 토큰

| 토큰 | 용도 |
|---|---|
| `route-canvas` | 경로 카드 · 결과 화면 경로 영역 · 공유 카드 기본 배경. 모드와 무관하게 어두움 |
| `route-line` | 경로 선 (흰색 고정) |
| `route-line-muted` | 일시정지 중 이동 구간, 러닝 중 화면의 지난 경로 잔상, 썸네일 격자 |
| `route-start` | 시작점 점, 러닝 중 현재 위치 점. 끝점은 `route-line` 테두리 원 |

## 3. 팔레트별 색상 값

### 볼트 라임 (`volt`)

형광 라임 포인트의 에너지. 운동화 · 기록계 감성. 기본 테마 후보.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#1E2810` | `#C8F53A` |
| `primary-pressed` | `#323F1C` | `#D9FA74` |
| `on-primary` | `#D6FF3D` | `#141A05` |
| `primary-soft` | `#EEF8CF` | `#252F10` |
| `on-primary-soft` | `#3B5400` | `#DDF98A` |
| `accent` | `#9AD615` | `#7DD3A0` |
| `accent-soft` | `#F2F9DE` | `#15291E` |
| `on-accent-soft` | `#4A6608` | `#B5EBCB` |
| `bg` | `#F7F8F3` | `#0D0F09` |
| `surface` | `#FFFFFF` | `#161911` |
| `surface-raised` | `#FFFFFF` | `#20241A` |
| `fill` | `#EEF0E8` | `#23281C` |
| `overlay` | `rgba(14,18,8,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#E4E7DC` | `#262B1E` |
| `border-strong` | `#CBD0BE` | `#3A4130` |
| `text-primary` | `#171C10` | `#F1F4EA` |
| `text-secondary` | `#5C6352` | `#A9B09D` |
| `text-tertiary` | `#8A9080` | `#7B8270` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(30,40,16,0.12)` | `rgba(0,0,0,0.45)` |
| `route-canvas` | `#1E2810` | `#1A2010` |
| `route-line` | `#FFFFFF` | `#FFFFFF` |
| `route-line-muted` | `rgba(255,255,255,0.35)` | `rgba(255,255,255,0.35)` |
| `route-start` | `#D6FF3D` | `#C8F53A` |

### 선라이즈 (`sunrise`)

새벽 러닝의 해 뜨는 주황. 따뜻한 동기부여.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#C2410C` | `#FB8A4C` |
| `primary-pressed` | `#A3360A` | `#FCA572` |
| `on-primary` | `#FFFFFF` | `#2A1003` |
| `primary-soft` | `#FFEEE3` | `#3A1F12` |
| `on-primary-soft` | `#9A3412` | `#FFC9A8` |
| `accent` | `#F59E0B` | `#FBBF55` |
| `accent-soft` | `#FEF4DC` | `#3A2C12` |
| `on-accent-soft` | `#8A5300` | `#FDE0A6` |
| `bg` | `#FBF8F5` | `#14100D` |
| `surface` | `#FFFFFF` | `#1D1814` |
| `surface-raised` | `#FFFFFF` | `#28211C` |
| `fill` | `#F4EFEA` | `#2E2620` |
| `overlay` | `rgba(30,16,8,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#EEE6DF` | `#2F2721` |
| `border-strong` | `#D9CCC1` | `#463A31` |
| `text-primary` | `#221911` | `#F8F1EB` |
| `text-secondary` | `#6B5F55` | `#B8AA9E` |
| `text-tertiary` | `#958A80` | `#857A70` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(194,65,12,0.12)` | `rgba(0,0,0,0.45)` |
| `route-canvas` | `#2E1609` | `#2A160C` |
| `route-line` | `#FFFFFF` | `#FFFFFF` |
| `route-line-muted` | `rgba(255,255,255,0.35)` | `rgba(255,255,255,0.35)` |
| `route-start` | `#FDBA4A` | `#FBBF55` |

### 리버 블루 (`river`)

강변 새벽 공기 같은 청록. 시원하고 차분한 기록 앱.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#0B6E86` | `#45C9DE` |
| `primary-pressed` | `#095A6E` | `#76D9E8` |
| `on-primary` | `#FFFFFF` | `#032029` |
| `primary-soft` | `#E2F4F7` | `#0F2F38` |
| `on-primary-soft` | `#0A5C70` | `#A6E8F2` |
| `accent` | `#2BC4D8` | `#7DD3FC` |
| `accent-soft` | `#E3F7FA` | `#0C2A3D` |
| `on-accent-soft` | `#0B6373` | `#BAE6FD` |
| `bg` | `#F5F9FA` | `#091316` |
| `surface` | `#FFFFFF` | `#0F1C20` |
| `surface-raised` | `#FFFFFF` | `#16272C` |
| `fill` | `#EBF2F4` | `#1A2C31` |
| `overlay` | `rgba(6,22,28,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#DEE8EB` | `#1C3036` |
| `border-strong` | `#C3D3D8` | `#2B454D` |
| `text-primary` | `#0F1E23` | `#EDF6F8` |
| `text-secondary` | `#53656B` | `#98ADB3` |
| `text-tertiary` | `#839297` | `#6C8188` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(11,110,134,0.12)` | `rgba(0,0,0,0.45)` |
| `route-canvas` | `#0B2D37` | `#0B2830` |
| `route-line` | `#FFFFFF` | `#FFFFFF` |
| `route-line-muted` | `rgba(255,255,255,0.35)` | `rgba(255,255,255,0.35)` |
| `route-start` | `#5FE0EE` | `#45C9DE` |

### 잉크 모노 (`ink`)

흑백만 쓰는 미니멀. 흰 경로선이 가장 돋보이는 테마.

| 토큰 | 라이트 | 다크 |
|---|---|---|
| `primary` | `#18181B` | `#F4F4F5` |
| `primary-pressed` | `#3F3F46` | `#D4D4D8` |
| `on-primary` | `#FFFFFF` | `#111113` |
| `primary-soft` | `#EFEFF1` | `#27272A` |
| `on-primary-soft` | `#27272A` | `#E4E4E7` |
| `accent` | `#8E8E96` | `#A1A1AA` |
| `accent-soft` | `#F2F2F4` | `#232326` |
| `on-accent-soft` | `#3F3F46` | `#D4D4D8` |
| `bg` | `#F7F7F8` | `#0C0C0D` |
| `surface` | `#FFFFFF` | `#161618` |
| `surface-raised` | `#FFFFFF` | `#202023` |
| `fill` | `#EEEEF0` | `#252528` |
| `overlay` | `rgba(10,10,12,0.45)` | `rgba(0,0,0,0.6)` |
| `border` | `#E4E4E7` | `#26262A` |
| `border-strong` | `#CDCDD3` | `#3A3A40` |
| `text-primary` | `#18181B` | `#F4F4F5` |
| `text-secondary` | `#5E5E66` | `#A8A8B0` |
| `text-tertiary` | `#8E8E96` | `#76767E` |
| `danger` | `#C62828` | `#F87171` |
| `danger-soft` | `#FDECEC` | `#3A1C1F` |
| `success` | `#127235` | `#5BD69A` |
| `success-soft` | `#E7F5EC` | `#163024` |
| `warning` | `#B45309` | `#FBBF55` |
| `warning-soft` | `#FDF3E1` | `#3A2C12` |
| `cal-sunday` | `#C62828` | `#F87171` |
| `cal-saturday` | `#2563EB` | `#60A5FA` |
| `shadow` | `rgba(0,0,0,0.10)` | `rgba(0,0,0,0.45)` |
| `route-canvas` | `#18181B` | `#1C1C1F` |
| `route-line` | `#FFFFFF` | `#FFFFFF` |
| `route-line-muted` | `rgba(255,255,255,0.35)` | `rgba(255,255,255,0.35)` |
| `route-start` | `#FFFFFF` | `#FFFFFF` |

## 4. 사용 규칙 (데이즈 규칙에 추가)

[theme-foundation.md](theme-foundation.md) 4장의 규칙(primary는 화면당 한두 곳, soft 배경엔 짝이 되는 on 색, fill은 회색 역할, 표면 세 단계, 하드코딩 금지)을 그대로 따릅니다. 런닝앱에서 추가하는 규칙은 다음과 같습니다.

1. **러닝 중 큰 숫자는 `text-primary`**: 시간 · 거리 · 페이스는 가장 높은 대비가 필요하므로 primary가 아니라 `text-primary`로 씁니다. primary는 일시정지 · 시작 버튼에만 씁니다.
2. **경로는 항상 `route-canvas` 위에**: 화면 배경(`bg`) 위에 흰 선을 직접 그리지 않습니다.
3. **공유 카드는 테마와 독립**: 공유 카드 배경은 `route-canvas` 또는 사용자 사진(+ `image-scrim`)이며, 글자는 `on-image`입니다. 앱 테마를 바꿔도 이미 만든 카드 모양이 흔들리지 않게 하기 위함입니다.
4. **`sunrise`의 primary와 warning이 비슷합니다**: 경고는 색만으로 구분하지 말고 아이콘 · 문구를 함께 씁니다.
5. **`ink`에서는 상태 색이 유일한 유채색입니다**: 목표 달성 등 success를 남발하지 않습니다.

## 5. 명도 대비 검증

WCAG 기준: 일반 텍스트 4.5:1, 큰 텍스트 · UI 그래픽 3:1. 아래 값은 `theme-tokens.json`에서 계산했습니다.

| 조합 | 용도 | 기준 | 볼트 라임 라이트 | 볼트 라임 다크 | 선라이즈 라이트 | 선라이즈 다크 | 리버 블루 라이트 | 리버 블루 다크 | 잉크 모노 라이트 | 잉크 모노 다크 |
|---|---|---|---|---|---|---|---|---|---|---|
| `on-primary` / `primary` | 버튼 글자 | 4.5 | 13.34 | 14.05 | 5.18 | 7.53 | 5.85 | 8.58 | 17.72 | 17.16 |
| `on-primary-soft` / `primary-soft` | 뱃지 · 히어로 라벨 | 4.5 | 7.73 | 12.05 | 6.47 | 10.24 | 6.67 | 10.41 | 12.97 | 11.74 |
| `on-accent-soft` / `accent-soft` | 보조 뱃지 | 4.5 | 6.07 | 11.49 | 5.78 | 10.58 | 6.22 | 11.19 | 9.34 | 10.6 |
| `text-primary` / `bg` | 본문 | 4.5 | 16.26 | 17.32 | 16.33 | 16.92 | 16.1 | 17.14 | 16.55 | 17.79 |
| `text-secondary` / `surface` | 보조 텍스트 | 4.5 | 6.25 | 7.94 | 6.19 | 7.78 | 6.1 | 7.42 | 6.42 | 7.65 |
| `text-tertiary` / `surface` | 비활성 텍스트 | 3.0 | 3.29 | 4.46 | 3.37 | 4.2 | 3.22 | 4.25 | 3.25 | 4.01 |
| `primary` / `surface` | 활성 탭 · 아이콘 | 3.0 | 15.37 | 14.03 | 5.18 | 7.41 | 5.85 | 8.83 | 17.72 | 16.44 |
| `danger` / `danger-soft` | 경고 뱃지 | 4.5 | 4.92 | 5.56 | 4.92 | 5.56 | 4.92 | 5.56 | 4.92 | 5.56 |
| `route-line` / `route-canvas` | 경로 흰 선 | 3.0 | 15.37 | 16.69 | 17.01 | 17.23 | 14.54 | 15.44 | 17.72 | 17.0 |
| `route-start` / `route-canvas` | 시작점 표시 | 3.0 | 13.34 | 13.18 | 9.96 | 10.41 | 9.26 | 7.84 | 17.72 | 17.0 |

모든 조합이 기준을 넘습니다. `accent`는 데이즈와 같이 라이트 모드에서 글자색으로 쓰지 않고 그래픽(진행바, 차트 막대, 마커)에만 사용합니다.

## 6. Android(Compose) 적용

데이즈 가이드의 `AppColors` 구조에 아래 4개 필드를 추가하고, 테마 enum만 바꿉니다. 전체 코드는 [theme-foundation.md](theme-foundation.md) 9장.

```kotlin
// AppColors에 추가
val routeCanvas: Color,
val routeLine: Color,
val routeLineMuted: Color,
val routeStart: Color,

enum class AppThemeType { VOLT, SUNRISE, RIVER, INK }
```

- 홈 위젯(Glance)도 같은 팔레트를 씁니다. 위젯의 경로 썸네일(v2 W2)도 `route-canvas` 위에 그립니다.
