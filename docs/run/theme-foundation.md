# 테마 기본 구조 (토큰 · 사용 규칙 · 타이포 · Compose)

> 데이즈 테마 가이드에서 앱과 무관한 부분(토큰 정의, 사용 규칙, 컴포넌트 매핑, 타이포그래피, 모서리 · 간격, Compose 적용)을 옮겨 온 문서입니다.
> 색 **값**과 런닝앱 전용 규칙은 [theme-guide.md](theme-guide.md), 값 원본은 `assets/theme-tokens.json`입니다.

## 목차

1. 테마 전환 방식 (웹)
2. 토큰 정의
3. 테마와 무관한 고정 색
4. 사용 규칙
5. 컴포넌트별 토큰 매핑
6. 타이포그래피
7. 모서리 · 간격
8. 공통 컴포넌트
9. Android(Compose) 적용

## 1. 테마 전환 방식 (웹)

정책 페이지 · 미리보기는 루트 요소에 `data-theme`과 `data-mode`를 지정합니다.

```html
<html data-theme="volt" data-mode="light">
```

| 속성 | 값 |
|---|---|
| `data-theme` | `volt` · `sunrise` · `river` · `ink` |
| `data-mode` | `light` · `dark` |

"시스템 설정 따르기"는 OS 다크 모드 여부(`prefers-color-scheme`)를 읽어 `data-mode`에 반영합니다. CSS 변수 이름은 `--color-{토큰}`입니다.

## 2. 토큰 정의

모든 팔레트는 같은 이름의 토큰 31개(공통 27 + route 4)를 씁니다. 컴포넌트는 토큰 이름만 참조하고, 테마를 바꾸면 값만 바뀝니다.

### 브랜드

| 토큰 | 용도 |
|---|---|
| `primary` | 시작 · 일시정지 등 주 CTA 버튼, 활성 탭, 토글 ON |
| `primary-pressed` | primary 버튼 눌림 · 호버 |
| `on-primary` | primary 위 글자 · 아이콘 |
| `primary-soft` | 강조 카드 배경, 선택된 카드 배경, 주 뱃지 배경 |
| `on-primary-soft` | primary-soft 위 글자 |

### 보조

| 토큰 | 용도 |
|---|---|
| `accent` | 진행바 채움, 차트 막대, 보조 강조 그래픽 (라이트에서 글자색 금지) |
| `accent-soft` | 보조 뱃지 배경 |
| `on-accent-soft` | accent-soft 위 글자 |

### 표면

| 토큰 | 용도 |
|---|---|
| `bg` | 앱 전체 배경 |
| `surface` | 카드, 리스트 아이템, 탭바 |
| `surface-raised` | 바텀시트, 모달, 팝오버, 토스트 |
| `fill` | 세그먼트 트랙, 진행바 트랙, 비활성 칩, 보조 버튼, 아이콘 배경, 입력창 배경 |
| `overlay` | 바텀시트 · 모달 뒤 딤 배경 |

### 선

| 토큰 | 용도 |
|---|---|
| `border` | 카드 테두리, 리스트 구분선 |
| `border-strong` | 입력창 테두리, 토글 OFF, 아웃라인 버튼, 빈 체크박스 |

### 텍스트

| 토큰 | 용도 |
|---|---|
| `text-primary` | 제목, 본문, 러닝 중 큰 숫자 |
| `text-secondary` | 날짜, 설명, 보조 정보 |
| `text-tertiary` | 플레이스홀더, 비활성 탭 · 텍스트 |

### 상태

| 토큰 | 용도 |
|---|---|
| `danger` | 삭제 · 탈퇴, 에러, GPS 끊김 |
| `danger-soft` | danger 뱃지 · 알림 배경 |
| `success` | 목표 달성, 저장 · 백업 완료 |
| `success-soft` | success 뱃지 배경 |
| `warning` | 주의 안내 (GPS 약함, 배터리 최적화 등) |
| `warning-soft` | warning 안내 박스 배경 |

### 캘린더 (v2 달력 보기)

| 토큰 | 용도 |
|---|---|
| `cal-sunday` | 일요일 · 공휴일 숫자 |
| `cal-saturday` | 토요일 숫자 |

### 효과

| 토큰 | 용도 |
|---|---|
| `shadow` | 카드 · FAB 그림자 색 |

### 경로 (route)

[theme-guide.md](theme-guide.md) 2장 "route 토큰" 참고.

## 3. 테마와 무관한 고정 색

| 토큰 | 값 | 용도 |
|---|---|---|
| `on-image` | `#FFFFFF` | 사진 · route-canvas 위 공유 카드 글자 · 아이콘 |
| `image-scrim` | `rgba(0,0,0,0.35)` | 사진 위 글자 가독성을 위한 어두운 막 |

Google 로그인 버튼은 Google 브랜드 가이드라인 색을 따릅니다.

## 4. 사용 규칙

1. **primary는 화면당 한두 곳만.** 주 버튼, 활성 탭처럼 시선이 가야 할 곳에만 씁니다.
2. **soft 배경에는 짝이 되는 on 색.** `primary-soft` ↔ `on-primary-soft`, `accent-soft` ↔ `on-accent-soft`, `danger-soft` ↔ `danger`. 짝을 지켜야 다크 모드에서도 대비가 유지됩니다.
3. **fill은 테마색이 섞이지 않은 회색 역할.** 보조 버튼 · 트랙 · 비활성 칩 · 아이콘 배경 · 입력창 배경은 `fill`. 보조 요소에 `primary-soft`를 쓰면 강조가 사라집니다.
4. **표면은 세 단계.** `bg` → `surface` → `surface-raised`. 라이트는 `surface`와 `surface-raised`가 같은 흰색이고 `shadow`로 구분, 다크는 `surface-raised`를 한 단계 밝게 해서 구분합니다.
5. **진행바.** 트랙 `fill`, 채움 `primary → accent` 그라데이션. 단, 라이트 `volt`는 accent 단색 (theme-guide 2장).
6. **상태 색은 네 테마 공통.** 색만으로 상태를 구분하지 말고 문구 · 아이콘을 함께 씁니다.
7. **그림자는 `shadow` 토큰으로.** 예: `box-shadow: 0 4px 16px var(--color-shadow);`
8. **하드코딩 금지.** 컴포넌트 코드에 `Color(0xFF…)` · `#1E2810` 같은 값을 직접 쓰지 않습니다. 홈 위젯도 같은 팔레트를 씁니다.

런닝앱 전용 규칙(큰 숫자 색, route-canvas, 공유 카드)은 [theme-guide.md](theme-guide.md) 4장.

## 5. 컴포넌트별 토큰 매핑

| 컴포넌트 | 배경 | 글자 · 아이콘 | 테두리 | 기타 |
|---|---|---|---|---|
| 화면 배경 | bg | – | – | – |
| 상단 헤더 | bg (스크롤 시 surface) | text-primary | border (스크롤 시) | – |
| 강조 카드 (이번 주 목표 등) | primary-soft | 라벨 on-primary-soft · 숫자 text-primary | – | shadow |
| 일반 카드 · 리스트 아이템 | surface | 제목 text-primary · 설명 text-secondary | border | – |
| 경로 카드 · 썸네일 | route-canvas | 선 route-line · 시작점 route-start · 수치 on-image | – | – |
| 주 버튼 | primary (눌림 primary-pressed) | on-primary | – | – |
| 보조 버튼 | fill | text-primary | – | – |
| 아웃라인 버튼 | 투명 | text-primary | border-strong | – |
| 위험 버튼 (삭제 · 탈퇴) | danger-soft 또는 danger | danger 또는 on-primary | – | – |
| FAB | primary | on-primary | – | shadow |
| 필터 칩 (활성 / 비활성) | text-primary / fill | bg / text-secondary | – | – |
| 세그먼트 컨트롤 | 트랙 fill · 선택 surface | 선택 text-primary · 비선택 text-secondary | – | 선택 항목 shadow |
| 뱃지: 기본 | primary-soft | on-primary-soft | – | – |
| 뱃지: 보조 | accent-soft | on-accent-soft | – | – |
| 뱃지: 완료 · 달성 | success-soft | success | – | – |
| 뱃지: 경고 | danger-soft | danger | – | – |
| 토글 ON / OFF | primary / border-strong | 손잡이 on-primary / surface | – | – |
| 입력창 (기본) | surface | text-primary · 플레이스홀더 text-tertiary | border-strong | – |
| 입력창 (포커스) | surface | text-primary | primary 1.5px + 바깥 링 primary-soft 3px | – |
| 체크박스 (빈 / 선택) | surface / primary | 체크 on-primary | border-strong / – | – |
| 진행바 | 트랙 fill · 채움 4-5 | – | – | – |
| 차트 막대 | accent (선택 막대 primary) | 축 text-tertiary | – | – |
| 하단 탭바 | surface | 활성 primary · 비활성 text-tertiary | 상단 border | – |
| 바텀시트 · 모달 | surface-raised | text-primary | – | 뒤 배경 overlay |
| 시트 드래그 핸들 | border-strong | – | – | – |
| 안내 박스 (팁 / 주의) | fill / warning-soft | text-secondary / warning | – | – |
| 토스트 | text-primary | bg | – | shadow |
| 구분선 | – | – | border | – |

## 6. 타이포그래피

글꼴은 **Pretendard**(SIL OFL 1.1) 한 가지입니다.

| 스타일 | 크기(sp) | 굵기 | 줄 간격(sp) | 자간 | 숫자 | 용도 (권장) |
|---|---|---|---|---|---|---|
| `displayHero` | 44 | 800 | 52 | -0.03em | tnum | 러닝 중 메인 숫자 (큰 화면) |
| `displayHeroMobile` | 36 | 800 | 44 | -0.02em | tnum | 러닝 중 메인 숫자 · 결과 거리 |
| `headlineLg` | 28 | 700 | 36 | -0.02em |  | 화면 큰 제목 |
| `headlineMd` | 22 | 700 | 30 | -0.015em |  | 섹션 제목 |
| `headlineSm` | 18 | 600 | 24 | -0.01em |  | 카드 제목 |
| `bodyLg` | 17 | 400 | 24 | -0.01em |  | 강조 본문 |
| `bodyMd` | 15 | 400 | 22 | -0.005em |  | 기본 본문 |
| `bodySm` | 13 | 400 | 18 | 0em |  | 보조 설명 · 날짜 |
| `labelLg` | 15 | 600 | 20 | -0.005em |  | 버튼 |
| `labelMd` | 13 | 600 | 18 | 0em |  | 칩 · 탭 |
| `labelSm` | 11 | 600 | 14 | 0.02em |  | 뱃지 · 캡션 |
| `metricTabular` | 24 | 700 | 28 | -0.02em | tnum | 보조 수치 (페이스 · 시간 · 스플릿) |

- 포함 굵기: Regular(400) · Medium(500) · SemiBold(600) · Bold(700) · ExtraBold(800).
- 거리 · 시간 · 페이스처럼 값이 계속 바뀌는 숫자는 `tnum`(tabular-nums)을 써서 폭이 흔들리지 않게 합니다.
- Pretendard는 oss-licenses 플러그인 목록에 자동으로 들어가지 않으므로 라이선스 원문(`res/raw/pretendard_ofl.txt`)을 약관 및 정책 화면에서 따로 고지합니다.

## 7. 모서리 · 간격

코드에서는 `AppShapes` · `AppSpacing`으로만 참조하고 값을 직접 쓰지 않습니다 (CODING_CONVENTIONS 6-5).

| 모서리 | 값 | | 간격 | 값 |
|---|---|---|---|---|
| `sm` | 4dp | | `xs` | 4dp |
| `default` | 8dp | | `sm` | 8dp |
| `md` | 12dp | | `md` | 14dp |
| `lg` | 16dp (카드) | | `lg` | 20dp |
| `xl` | 24dp | | `xl` | 28dp |
| `full` | 원 · 알약 | | `xxl` | 40dp |
| | | | `marginMobile` · `margin` | 16dp · 20dp (화면 좌우) |
| | | | `gutter` · `gutterCompact` | 16dp · 12dp (카드 사이) |

아이콘은 Material Symbols Outlined(Apache 2.0)를 VectorDrawable로 `core:designsystem`의 `res/drawable/ic_*`에 둡니다.

## 8. 공통 컴포넌트

5장 매핑을 구현한 컴포넌트는 `core/designsystem/.../component/`에 둡니다. 기능 모듈에서 같은 역할의 컴포넌트를 새로 만들지 않습니다. 컴포넌트를 추가하면 이 장에 파일 · 이름 · 5장 항목을 표로 기록합니다.

**Preview** (`core/designsystem/.../preview/`)
- `@ComponentPreviews`: 라이트 · 다크 · 글꼴 2배.
- `@ScreenPreviews`: 핸드폰(411×891dp) 라이트 · 다크 · 글꼴 2배 · 가로. 태블릿은 대상이 아닙니다.

## 9. Android(Compose) 적용

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
    val dark = when (mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = palette(type, dark)   // theme-tokens.json으로 생성한 8개 팔레트 중 선택
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(content = content)  // colorScheme 매핑은 아래 참고
    }
}

object AppThemeTokens {
    val colors: AppColors @Composable get() = LocalAppColors.current
}
```

- 토큰 이름은 JSON의 kebab-case를 camelCase로 바꿉니다 (`route-line-muted` → `routeLineMuted`).
- 컴포넌트에서는 `AppThemeTokens.colors.primary`처럼 토큰만 참조합니다.
- 8개 팔레트(`ThemeTokens.kt`)는 **생성 파일**입니다. 값은 `theme-tokens.json`만 고치고 `python3 scripts/generate-theme-colors.py`로 다시 만듭니다. JSON과 생성 코드가 다르면 실패하는 `ThemeTokensSyncTest`를 둡니다.
- 고정 색(3장)은 `AppFixedColors`(`onImage`, `imageScrim`)로 씁니다.
- Material 컴포넌트용 `colorScheme` 매핑: `primary` · `onPrimary`, `primaryContainer`=`primary-soft`, `secondary`=`accent`, `background`=`bg`, `surface` · `surfaceContainer`=`surface`, `surfaceContainerHigh(est)`=`surface-raised`, `surfaceVariant`=`fill`, `onSurface`=`text-primary`, `onSurfaceVariant`=`text-secondary`, `outline`=`border-strong`, `outlineVariant`=`border`, `error`=`danger`, `scrim`=`overlay`.
- 글자 스타일(6장)은 `AppThemeTokens.typography.bodyMd`처럼 참조하고, Material `Typography`도 같은 값으로 매핑합니다.
- 선택한 테마 · 모드는 `core:datastore`에 저장하고, `MainActivity`가 이 값으로 `AppTheme`을 그리며 시스템 바 아이콘 색도 맞춥니다.
