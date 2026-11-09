# 테마 컬러 시스템 가이드

> 색상 값의 원본은 `assets/theme-tokens.json`입니다. `assets/theme-tokens.css`는 웹(정책 페이지, 미리보기)용, `assets/theme-preview.html`은 8 팔레트 미리보기입니다. Android 적용 방법은 맨 아래 "Android(Compose) 적용"을 참고하세요.

런닝앱의 테마 컬러 정의서입니다. 4개 테마(볼트 라임, 선라이즈, 리버 블루, 잉크 모노)에 각각 라이트 · 다크 모드를 지원해 **총 8개 팔레트**를 제공합니다.

모든 팔레트는 **같은 이름의 토큰 31개**(데이즈와 같은 27개 + 경로 토큰 4개)를 사용합니다. 컴포넌트는 토큰 이름만 참조하고, 테마를 바꿀 때는 값만 교체합니다.

## 목차

1. 테마 전환 방식
2. 토큰 정의
3. 팔레트별 색상 값
4. 테마와 무관한 고정 색
5. 사용 규칙
6. 컴포넌트별 토큰 매핑
7. 명도 대비 검증
8. 데이즈 테마와 달라진 부분
9. 타이포그래피
10. 모서리 · 간격
11. 공통 컴포넌트

## 1. 테마 전환 방식

루트 요소에 `data-theme`과 `data-mode` 두 속성을 지정합니다 (웹 · 미리보기).

```html
<html data-theme="volt" data-mode="dark">
```

| 속성 | 값 |
|---|---|
| `data-theme` | `volt` · `sunrise` · `river` · `ink` |
| `data-mode` | `light` · `dark` |

앱에서 "시스템 설정 따르기"를 선택하면 기기 다크 모드 여부로 모드를 정합니다. **기본값은 `volt` + 시스템**입니다.

## 2. 토큰 정의

CSS 변수 이름은 `--color-{토큰}`, Compose는 camelCase(`primary-soft` → `primarySoft`)입니다.

### 브랜드

| 토큰 | 용도 |
|---|---|
| `primary` | 주 CTA(달리기 시작 · 저장), 활성 탭, 토글 ON, 러닝 중 시작 · 일시정지 원형 버튼 |
| `primary-pressed` | primary 버튼 눌림 상태 |
| `on-primary` | primary 위 글자 · 아이콘 |
| `primary-soft` | 강조 카드 배경, 강조 뱃지("3주 연속") 배경, 선택된 카드, 일시정지 상태 알약 |
| `on-primary-soft` | primary-soft 위 글자 |

### 보조

| 토큰 | 용도 |
|---|---|
| `accent` | 진행바 · 링 채움(그라데이션 끝), 통계 막대 · 페이스 그래프 선 (그래픽 전용) |
| `accent-soft` | 보조 뱃지 배경 |
| `on-accent-soft` | accent-soft 위 글자 |

### 표면

| 토큰 | 용도 |
|---|---|
| `bg` | 앱 전체 배경 |
| `surface` | 카드, 리스트 아이템, 탭바 |
| `surface-raised` | 바텀시트, 다이얼로그, 토스트 |
| `fill` | 세그먼트 트랙, 진행바 트랙, 비활성 칩, 보조 버튼, 입력창 배경, 스테퍼 원형 버튼 |
| `overlay` | 바텀시트 · 다이얼로그 뒤 딤 |

### 선

| 토큰 | 용도 |
|---|---|
| `border` | 카드 테두리, 리스트 구분선 |
| `border-strong` | 입력창 테두리, 토글 OFF, 아웃라인 버튼, 빈 체크박스 |

### 텍스트

| 토큰 | 용도 |
|---|---|
| `text-primary` | 제목, 본문, **러닝 수치(거리 · 시간 · 페이스)** |
| `text-secondary` | 라벨("거리", "평균 페이스"), 날짜, 설명 |
| `text-tertiary` | 플레이스홀더, 비활성 탭, `–′––″` 같은 빈 값 |

### 상태

| 토큰 | 용도 |
|---|---|
| `danger` | 삭제 · 탈퇴, 에러 |
| `danger-soft` | danger 안내 배경 |
| `success` | 목표 달성, 백업 완료, GPS 양호 |
| `success-soft` | success 뱃지 배경 |
| `warning` | GPS 약함, 배터리 최적화 안내 |
| `warning-soft` | warning 안내 박스 배경 |

### 캘린더 (v2 달력 보기)

| 토큰 | 용도 |
|---|---|
| `cal-sunday` | 일요일 숫자 |
| `cal-saturday` | 토요일 숫자 |

### 효과

| 토큰 | 용도 |
|---|---|
| `shadow` | 카드 · 원형 버튼 그림자 색 |

### 경로

| 토큰 | 용도 |
|---|---|
| `route-canvas` | 경로 영역 배경 (결과 · 상세 · 썸네일 · 러닝 중 · 공유 카드 기본). **라이트 · 다크 모두 어두운 색** |
| `route-line` | 경로 선 (흰색 고정) |
| `route-line-muted` | 신호 공백 · 일시정지 중 이동 구간(점선), 썸네일 보조선 |
| `route-start` | 시작점 점, 러닝 중 현재 위치 점 |

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

## 4. 테마와 무관한 고정 색

| 토큰 | 값 | 용도 |
|---|---|---|
| `--color-on-image` | `#FFFFFF` | 사진 · `route-canvas` 위 글자 (공유 카드) |
| `--color-image-scrim` | `rgba(0,0,0,0.35)` | 사진 배경 공유 카드의 가독성 막 |

Google 로그인 버튼은 Google 브랜드 가이드라인의 색 · 로고를 따릅니다 (토큰 아님).

## 5. 사용 규칙

### 5.1 primary는 화면당 한두 곳만

"달리기 시작", 저장, 활성 탭처럼 시선이 가야 할 곳에만 씁니다. 홈에서는 시작 버튼이 주인공이므로 진행 링 채움은 accent 쪽(그라데이션)으로 둡니다.

### 5.2 soft 배경에는 짝이 되는 on 색

`primary-soft` 위는 `on-primary-soft`, `accent-soft` 위는 `on-accent-soft`, `danger-soft` 위는 `danger`, `success-soft` 위는 `success`. 짝을 지켜야 다크 모드에서도 대비가 유지됩니다.

### 5.3 fill은 테마색이 섞이지 않은 회색 역할

보조 버튼, 세그먼트 트랙, 진행바 트랙, 비활성 칩, 입력창, 스테퍼 버튼은 모두 `fill`. 보조 요소에 `primary-soft`를 쓰면 강조가 사라집니다.

### 5.4 표면은 세 단계

`bg` → `surface` → `surface-raised`. 라이트는 surface · raised가 모두 흰색이고 그림자로 구분, 다크는 raised를 한 단계 밝게 합니다.

### 5.5 러닝 수치 색 규칙

거리 · 시간 · 페이스 같은 수치는 항상 `text-primary`, 라벨은 `text-secondary`, 값이 없으면(`–′––″`) `text-tertiary`. **수치에 primary를 쓰지 않습니다** — 달리면서 한눈에 읽어야 하므로 가장 높은 대비가 필요합니다. 숫자에는 tabular-nums를 적용합니다.

### 5.6 진행바 · 진행 링

트랙 `fill`, 채움 `linear-gradient(90deg, primary, accent)`. 단 **`volt` 라이트는 primary가 거의 검정이라 채움을 accent 단색**으로 합니다. 목표 100% 초과는 같은 색으로 가득 채우고 수치로만 표시합니다.

### 5.7 경로 표현

경로는 항상 `route-canvas` 위에 그립니다. 화면 배경(`bg`) 위에 흰 선을 직접 그리지 않습니다. 선 `route-line`, 시작점 `route-start`, 끝점은 `route-line` 테두리 원, 공백 구간은 `route-line-muted` 점선. 수치를 경로 영역 위에 얹을 때는 `on-image`.

### 5.8 상태 색은 네 테마 공통

`danger` · `success` · `warning`은 라이트끼리, 다크끼리 같은 값입니다. `sunrise`는 primary와 warning이, `volt` 다크는 primary와 success가 비슷해 보이므로 상태를 색만으로 구분하지 말고 아이콘 · 문구를 함께 씁니다. `ink`에서는 상태 색이 유일한 유채색이라 남발하지 않습니다.

### 5.9 그림자는 shadow 토큰으로

예: `box-shadow: 0 4px 16px var(--color-shadow)`. 러닝 중 원형 버튼에도 같은 토큰을 씁니다.

### 5.10 테마별 하드코딩 금지

컴포넌트에 `#C8F53A` 같은 값을 직접 쓰지 않습니다. 위젯도 같은 토큰 값을 Glance 쪽에 옮겨 씁니다.

### 5.11 공유 카드는 테마와 독립

공유 카드(P7)는 기본 배경 `route-canvas`(현재 테마의 값) 또는 사진 + `image-scrim`이고, 글자는 `on-image`입니다. 카드 안에 primary · accent를 쓰지 않아 어떤 테마에서 만들어도 같은 느낌을 줍니다.

## 6. 컴포넌트별 토큰 매핑

| 컴포넌트 | 배경 | 글자 · 아이콘 | 테두리 | 기타 |
|---|---|---|---|---|
| 화면 배경 | bg | – | – | – |
| 상단 헤더 | bg (스크롤 시 surface) | text-primary | border (스크롤 시) | – |
| 일반 카드 · 리스트 아이템 | surface | 제목 text-primary · 설명 text-secondary | border | – |
| 강조 카드 (목표 기여 등) | primary-soft | on-primary-soft | – | shadow |
| 수치 타일 (`RunStatTile`) | 투명 | 값 text-primary · 라벨 text-secondary | – | tabular-nums |
| 경로 카드 · 썸네일 | route-canvas | 선 route-line · 시작점 route-start · 수치 on-image | – | – |
| 주 버튼 | primary (눌림 primary-pressed) | on-primary | – | – |
| 보조 버튼 | fill | text-primary | – | – |
| 아웃라인 버튼 | 투명 | text-primary | border-strong | – |
| 위험 텍스트 버튼 (삭제 · 탈퇴) | 투명 | danger | – | – |
| 러닝 중 원형 버튼 (일시정지 · 재개) | primary | on-primary | – | shadow |
| 러닝 중 원형 버튼 (종료) | fill | text-primary | – | 길게 누르기 진행 링 danger |
| 잠금 버튼 | fill | text-secondary | – | – |
| 일시정지 알약 | primary-soft | on-primary-soft | – | – |
| 필터 칩 (활성) | text-primary | bg | – | – |
| 필터 칩 (비활성) | fill | text-secondary | – | – |
| 세그먼트 컨트롤 | 트랙 fill · 선택 surface | 선택 text-primary · 비선택 text-secondary | – | 선택 항목 shadow |
| 뱃지: 강조 (연속 기록) | primary-soft | on-primary-soft | – | – |
| 뱃지: 목표 달성 · 백업 완료 | success-soft | success | – | – |
| 뱃지: 보조 | accent-soft | on-accent-soft | – | – |
| GPS 표시 | – | 양호 success · 약함 warning · 없음 text-tertiary | – | 아이콘 + 문구 함께 |
| 토글 ON / OFF | primary / border-strong | 손잡이 on-primary / surface | – | – |
| 입력창 (기본 / 포커스) | surface | text-primary · 플레이스홀더 text-tertiary | border-strong / primary 1.5px + 바깥 링 primary-soft | – |
| 체크박스 (빈 / 선택) | surface / primary | 체크 on-primary | border-strong / – | – |
| 스테퍼 원형 버튼 | fill | text-primary | – | – |
| 진행바 · 진행 링 | 트랙 fill · 채움 primary→accent (volt 라이트는 accent) | – | – | – |
| 통계 막대 · 페이스 그래프 | – | 막대 · 선 accent, 축 · 눈금 text-tertiary, 기준선 border | – | 선택 막대 primary |
| 하단 탭바 | surface | 활성 primary · 비활성 text-tertiary | 상단 border | – |
| 바텀시트 · 다이얼로그 | surface-raised | text-primary | – | 뒤 배경 overlay, 핸들 border-strong |
| 안내 박스 (팁 / 주의) | fill / warning-soft | text-secondary / warning | – | – |
| 토스트 | text-primary | bg | – | shadow |
| 공유 카드 | route-canvas 또는 사진 + image-scrim | on-image | – | 5.11 |
| 구분선 | – | – | border | – |

## 7. 명도 대비 검증

WCAG 기준: 일반 텍스트 4.5:1 이상, 큰 텍스트(24px 이상 또는 18.7px 이상 굵게)와 UI 그래픽 3:1 이상. `theme-tokens.json` 값으로 계산했습니다.

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

모든 조합이 기준을 넘습니다. `text-tertiary`는 플레이스홀더 · 비활성 전용(3:1 수준)이라 읽어야 하는 정보에는 `text-secondary` 이상을 씁니다. `accent`는 라이트 모드에서 흰 배경 대비가 낮아 글자색으로 쓰지 않고 그래픽에만 씁니다.

## 8. 데이즈 테마와 달라진 부분

| 항목 | 데이즈 | 런닝앱 | 이유 |
|---|---|---|---|
| 테마 | 핑크 · 네이비 · 세이지 · 라벤더 | 볼트 라임 · 선라이즈 · 리버 블루 · 잉크 모노 | 운동 앱 성격 (에너지, 새벽, 강변, 미니멀) |
| 토큰 수 | 27 | 31 (`route-*` 4개 추가) | 지도 없이 흰 선으로 경로를 그리므로, 라이트 모드에서도 선이 보이는 어두운 영역이 필요 |
| `volt` · `ink` 라이트 primary | – | 거의 검정, on-primary가 라임 · 흰색 | 기록계 · 운동화 같은 인상. 버튼 대비 13~17:1 |
| 진행바 | primary → accent | 같음, 단 `volt` 라이트는 accent 단색 | 검정 → 라임 그라데이션이 탁해 보임 |
| 숫자 색 규칙 | 디데이 숫자: 대표 primary | 러닝 수치는 항상 text-primary | 달리면서 읽는 숫자는 대비가 최우선 |
| 고정 색 | 카카오 노랑 포함 | 카카오 없음 | v1은 Google 로그인만 |
| 캘린더 토큰 | 사용 | v2 달력 보기 때 사용 | 토큰 세트를 데이즈와 맞춰 둠 |

## 9. 타이포그래피

Stitch 디자인 시스템 원본: [design/_stitch-design-system/DESIGN.md](design/_stitch-design-system/DESIGN.md), 화면별 시안: [design/README.md](design/README.md).

글꼴은 **Pretendard**(SIL OFL 1.1) 한 가지입니다. 크기 · 굵기 · 줄 간격 · 자간은 Stitch `DESIGN.md`의 typography 값을 따릅니다. 예외는 `metricHuge` 하나로, 달리면서 한눈에 읽어야 하는 거리라 `DESIGN.md`의 `display-hero`(56sp)보다 크게 둡니다.

| 스타일 | 크기(sp) | 굵기 | 줄 간격(sp) | 자간 | 숫자 | 용도 |
|---|---|---|---|---|---|---|
| `metricHuge` | 72 | 800 | 76 | -0.03em | tnum | 러닝 중 거리 (P3 · P4), 카운트다운은 이 크기의 2배 (`DESIGN.md` 예외) |
| `metricXl` | 48 | 700 | 52 | -0.03em | tnum | 홈 진행 링 안 거리 |
| `metricLg` | 36 | 700 | 40 | -0.02em | tnum | 러닝 중 시간 · 페이스, 결과 · 상세의 거리 |
| `metricMd` | 26 | 600 | 32 | -0.02em | tnum | 수치 타일 (결과 · 상세 · 일시정지) |
| `headlineLg` | 22 | 700 | 28 | -0.01em |  | 화면 큰 제목 |
| `headlineMd` | 18 | 600 | 24 | -0.01em |  | 섹션 제목, 시트 제목, 리스트 거리(tnum) |
| `headlineSm` | 16 | 600 | 22 | 0em |  | 카드 제목 |
| `bodyLg` | 16 | 400 | 24 | 0em |  | 강조 본문 |
| `bodyMd` | 14 | 400 | 20 | 0em |  | 기본 본문 |
| `bodySm` | 12 | 400 | 16 | 0.01em |  | 보조 설명 · 날짜 · 수치 라벨 |
| `labelLg` | 15 | 600 | 20 | 0em |  | 버튼 |
| `labelMd` | 13 | 500 | 18 | 0.01em |  | 칩 · 탭 |
| `labelSm` | 11 | 500 | 14 | 0.02em |  | 뱃지 · 캡션 |

- 포함한 굵기: Regular(400) · Medium(500) · SemiBold(600) · Bold(700) · ExtraBold(800).
- 수치 스타일은 모두 tabular-nums. 페이스의 `′ ″`는 같은 글꼴의 프라임 기호(U+2032 · U+2033)를 씁니다.
- 글꼴 크기 설정 2배에서 `metricHuge`가 화면을 넘으면 `autoSize` 대신 한 단계 작은 스타일로 내려갑니다 (P3 레이아웃이 깨지지 않게).
- 라이선스 원문은 `core/designsystem/src/main/res/raw/pretendard_ofl.txt`, 설정 > 약관 및 정책에서 따로 고지합니다.

## 10. 모서리 · 간격

코드에서는 `AppShapes` · `AppSpacing`으로만 참조합니다 (CODING_CONVENTIONS 6-5).

| 모서리 | 값 | | 간격 | 값 |
|---|---|---|---|---|
| `sm` | 4dp | | `xs` | 4dp |
| `default` | 8dp | | `sm` | 8dp |
| `md` | 12dp (경로 썸네일) | | `md` | 14dp |
| `lg` | 16dp (카드 · 경로 카드) | | `lg` | 20dp |
| `xl` | 24dp (위젯) | | `xl` | 28dp |
| `full` | 원 · 알약 (러닝 중 원형 버튼 72dp) | | `xxl` | 40dp |
| | | | `marginMobile` · `margin` | 16dp · 20dp (화면 좌우) |
| | | | `gutter` · `gutterCompact` | 16dp · 12dp (카드 사이) |

아이콘은 Material Symbols Outlined(Apache 2.0)를 VectorDrawable로 `core:designsystem`의 `res/drawable/ic_*`에 둡니다.

러닝 중 화면(P3 · P4)의 터치 영역은 최소 72dp(원형 버튼), 잠금 버튼 48dp 이상으로 둡니다 (뛰면서 누름).

## 11. 공통 컴포넌트

6장 매핑을 구현한 컴포넌트입니다. 경로는 `core/designsystem/.../component/`. 기능 모듈에서 같은 역할의 컴포넌트를 새로 만들지 않습니다.

| 파일 | 컴포넌트 | 6장 항목 |
|---|---|---|
| `RunButton.kt` · `RunButtonSize.kt` | `RunPrimaryButton` · `RunSecondaryButton` · `RunOutlineButton` · `RunDangerTextButton`, 크기 `LARGE`(56dp) · `SMALL`(36dp) | 주 · 보조 · 아웃라인 · 위험 버튼 |
| `RunGoogleButton.kt` | `RunGoogleSignInButton` | Google 가이드라인 색 · 로고 |
| `RunRoundButton.kt` | `RunRoundButton(kind = PRIMARY · NEUTRAL · LOCK, longPress)`, 길게 누르기 진행 링 | 러닝 중 원형 버튼 · 잠금 |
| `RunIconButton.kt` | `RunIconButton`(fill 원형) · `RunStepper`(− 값 +) | 스테퍼 |
| `RunChip.kt` | `RunFilterChip` · `RunSegmentedControl` | 필터 칩 · 세그먼트 |
| `RunBadge.kt` | `RunBadge(tone = HIGHLIGHT · SUCCESS · ACCENT · NEUTRAL)` · `RunPausedPill` | 뱃지 · 일시정지 알약 |
| `RunStatTile.kt` | `RunStatTile(value, label, size = HUGE · LARGE · MEDIUM)` · `RunStatGrid` | 수치 타일 (5.5) |
| `RouteCanvas.kt` | `RouteCanvas(route: ProjectedRoute, style = CARD · THUMBNAIL · LIVE · SHARE)` | 경로 카드 · 썸네일 (5.7, tracking.md 10-2) |
| `RunCard.kt` | `RunCard` · `RunSection` · `RunListItem` · `RunDivider` | 카드 · 리스트 · 구분선 |
| `RunTextField.kt` | `RunTextField`(포커스 링, 글자 수 표시) | 입력창 |
| `RunSelectionControls.kt` | `RunSwitch` · `RunCheckbox` | 토글 · 체크박스 |
| `RunProgress.kt` | `RunProgressBar` · `RunProgressRing` | 진행바 · 진행 링 (5.6) |
| `RunCharts.kt` | `RunBarChart`(요일 · 주차 막대) · `RunPaceChart`(km별 꺾은선) | 통계 막대 · 페이스 그래프 |
| `RunGpsIndicator.kt` | `RunGpsIndicator(status)` | GPS 표시 |
| `RunBars.kt` | `RunTopBar(isScrolled)` · `RunBottomBar` | 상단 헤더 · 하단 탭바 |
| `RunBottomSheet.kt` | `RunBottomSheet` · `RunSheetHandle` | 바텀시트 |
| `RunFeedback.kt` | `RunInfoBox(TIP · WARNING)` · `RunToast` · `RunToastHost` | 안내 박스 · 토스트 |

화면 전용 모양(홈 진행 링 카드, 공유 카드 레이아웃)은 각 feature에 둡니다. 거리 · 시간 · 페이스 문자열은 `core:ui`의 `RunFormat`으로만 만듭니다.

**Preview** (`core/designsystem/.../preview/RunPreviews.kt`)
- `@ComponentPreviews`: 라이트 · 다크 · 글꼴 2배. 견본은 `RunPreviewSurface { }` 안에.
- `@ScreenPreviews`: 핸드폰(411×891dp) 라이트 · 다크 · 글꼴 2배 · 가로.
- 러닝 중 화면은 `volt` 외에 `ink` 다크 Preview를 하나 더 둡니다 (가장 대비가 다른 팔레트).

## Android(Compose) 적용

`core:designsystem` 모듈에서 아래 구조로 사용합니다.

```kotlin
@Immutable
data class AppColors(
    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val onPrimarySoft: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccentSoft: Color,
    val bg: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val fill: Color,
    val overlay: Color,
    val border: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val danger: Color,
    val dangerSoft: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val calSunday: Color,
    val calSaturday: Color,
    val shadow: Color,
    val routeCanvas: Color,
    val routeLine: Color,
    val routeLineMuted: Color,
    val routeStart: Color,
)

enum class AppThemeType { VOLT, SUNRISE, RIVER, INK }
enum class AppThemeMode { LIGHT, DARK, SYSTEM }

val LocalAppColors = staticCompositionLocalOf<AppColors> { error("AppTheme 밖에서 사용") }

@Composable
fun AppTheme(
    type: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(mode)
    val colors = palette(type, dark)   // ThemeTokens.kt (생성 파일)
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = colors.toColorScheme(dark), content = content)
    }
}

object AppThemeTokens {
    val colors: AppColors @Composable get() = LocalAppColors.current
}
```

- 컴포넌트에서는 `AppThemeTokens.colors.routeLine`처럼 토큰만 참조합니다. `Color(0xFF…)` 하드코딩은 금지입니다.
- 8개 팔레트(`ThemeTokens.kt`)는 **생성 파일**입니다. 값은 `theme-tokens.json`만 고치고 `python3 scripts/generate-theme-colors.py`로 다시 만듭니다 (데이즈 스크립트를 31 토큰에 맞게 수정). `ThemeTokensSyncTest`가 JSON과 생성 코드가 다르면 실패합니다.
- 글자 스타일(9장)은 `AppThemeTokens.typography.metricHuge`처럼 참조합니다. Material `Typography` 매핑은 데이즈와 같습니다 (`displayLarge`=`metricHuge`, `headline*`, `titleLarge`=`headlineSm`, `titleMedium`=`labelLg`, `body*`, `label*`).
- 고정 색(4장)은 `AppFixedColors`(`onImage`, `imageScrim`).
- Material `colorScheme` 매핑: `primary`·`onPrimary`, `primaryContainer`=`primary-soft`, `secondary`=`accent`, `background`=`bg`, `surface`·`surfaceContainer`=`surface`, `surfaceContainerHigh(est)`=`surface-raised`, `surfaceVariant`=`fill`, `onSurface`=`text-primary`, `onSurfaceVariant`=`text-secondary`, `outline`=`border-strong`, `outlineVariant`=`border`, `error`=`danger`, `scrim`=`overlay`.
- 선택한 테마 · 모드는 `core:datastore`에 저장하고 `SettingsRepository.observeEffectiveThemeSetting()`(미리보기 값 ?: 저장 값)으로 그립니다. 테마 시트의 미리보기 · 적용 · 취소 동작은 데이즈와 같습니다.
- 위젯도 같은 팔레트를 Glance `ColorProvider`로 옮겨 씁니다.
