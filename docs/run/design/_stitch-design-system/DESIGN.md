---
name: Obsidian Kinetic
colors:
  surface: '#12140e'
  surface-dim: '#12140e'
  surface-bright: '#383a32'
  surface-container-lowest: '#0d0f09'
  surface-container-low: '#1a1c16'
  surface-container: '#1e201a'
  surface-container-high: '#292b24'
  surface-container-highest: '#34362e'
  on-surface: '#e3e3d8'
  on-surface-variant: '#c4c9ae'
  inverse-surface: '#e3e3d8'
  inverse-on-surface: '#2f312a'
  outline: '#8e937b'
  outline-variant: '#444935'
  surface-tint: '#abd611'
  primary: '#ffffff'
  on-primary: '#283500'
  primary-container: '#c6f338'
  on-primary-container: '#556d00'
  inverse-primary: '#506600'
  secondary: '#82d8a5'
  on-secondary: '#003920'
  secondary-container: '#006a40'
  on-secondary-container: '#90e7b2'
  tertiary: '#ffffff'
  on-tertiary: '#2a3414'
  tertiary-container: '#dbe8ba'
  on-tertiary-container: '#5d6944'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c6f338'
  primary-fixed-dim: '#abd611'
  on-primary-fixed: '#161f00'
  on-primary-fixed-variant: '#3c4d00'
  secondary-fixed: '#9ef5c0'
  secondary-fixed-dim: '#82d8a5'
  on-secondary-fixed: '#002111'
  on-secondary-fixed-variant: '#005231'
  tertiary-fixed: '#dbe8ba'
  tertiary-fixed-dim: '#bfcca0'
  on-tertiary-fixed: '#151f03'
  on-tertiary-fixed-variant: '#404b29'
  background: '#12140e'
  on-background: '#e3e3d8'
  surface-variant: '#34362e'
typography:
  display-hero:
    fontFamily: Pretendard
    fontSize: 56px
    fontWeight: '700'
    lineHeight: 60px
    letterSpacing: -0.04em
  display-hero-mobile:
    fontFamily: Pretendard
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 52px
    letterSpacing: -0.03em
  metric-large:
    fontFamily: Pretendard
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  metric-medium:
    fontFamily: Pretendard
    fontSize: 26px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Pretendard
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Pretendard
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Pretendard
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: 0em
  body-lg:
    fontFamily: Pretendard
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
  body-md:
    fontFamily: Pretendard
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  body-sm:
    fontFamily: Pretendard
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-lg:
    fontFamily: Pretendard
    fontSize: 15px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0em
  label-md:
    fontFamily: Pretendard
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Pretendard
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  gutter-sm: 0.5rem
  margin: 1rem
  margin-sm: 0.75rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.25rem
  space-xl: 1.75rem
---

## Brand & Style

This design system delivers an introspective, distraction-free environment for solo runners. Built exclusively for dark mode on Android devices, it rejects the gamified noise, social pressure, and advertising bloat common in modern fitness platforms. The interface channels calm kinetic clarity: an athlete alone on dark tarmac at dawn, focused strictly on breath, pace, and distance.

The aesthetic fuses technical minimalism with performance utilitarianism. Surface hierarchy relies on nuanced tonal shifts of warm obsidian green, reserving high-visibility neon lime strictly for primary calls to action, active navigation states, and completed metrics. Typography leads with legibility and restraint, presenting vital telemetry through prominent tabular numerals alongside quiet, respectful Korean labels. Route displays forgo noisy third-party map tiles entirely, rendering GPS tracks as stark, luminous vector polyline paths against a dedicated canvas.

## Colors

The palette establishes an ultra-low-fatigue dark environment tailored for pre-dawn and night running conditions.

- **Primary (`#C8F53A`)**: High-vis neon lime. Highly restrained; maximum 1 to 2 elements per viewport. Used strictly for the primary action button (달리기 시작), the active bottom navigation tab icon/indicator, and active metric bar fills.
- **On-Primary (`#141A05`)**: Deep olive-black ensuring strict WCAG AAA contrast when placed over the neon lime primary fill.
- **Primary-Soft (`#252F10`) & On-Primary-Soft (`#DDF98A`)**: Subtle low-energy lime tint applied to selected chips, secondary status tags, or paused states.
- **Accent (`#7DD3A0`)**: Muted sage green reserved strictly for secondary data visualisations, cadence charts, and elevation trends.
- **Background (`#0D0F09`)**: Deep obsidian dark surface tinted with natural warm green undertones.
- **Surface (`#161911`)**: Primary card containers, persistent bottom navigation dock, and list items.
- **Surface-Raised (`#20241A`)**: Elevated bottom sheets, modal confirmation dialogs, and floating metric pills.
- **Fill (`#23281C`)**: Secondary interactive button surfaces, unfilled progress track backdrops, and input field backgrounds.
- **Borders**: Subdued structural separation via `border` (`#262B1E`) and focused/emphasized framing via `border-strong` (`#3A4130`).
- **Typography Colors**: `text-primary` (`#F1F4EA`) for hero data and active labels; `text-secondary` (`#A9B09D`) for units and section descriptors; `text-tertiary` (`#7B8270`) for timestamps, disabled states, and inactive tab icons.
- **Status Tokens**: `danger` (`#F87171`) for destructive actions (기록 삭제, 러닝 종료); `success` (`#5BD69A`) for personal records and target completions.
- **Minimalist Route Canvas**: `route-canvas` (`#1A2010`) provides the dedicated background layer for vector paths, `route-line` (`#FFFFFF`) renders the smooth 2.5dp anti-aliased path, and `route-start` (`#C8F53A`) anchors the initial waypoint.

## Typography

Typography prioritizes instant legibility at arms-length while moving. 

- **Font Family**: Standardized entirely across all scales on **Pretendard**, balancing contemporary Korean letterforms with modern geometric Latin and numerical typesetting.
- **Tabular Figures**: Every numeric display (distance, pace, duration, heart rate, cadence) mandates `font-variant-numeric: tabular-nums` (or OpenType `tnum`). This prevents layout jitter during rapid real-time updates.
- **Hierarchy Split**: Real-time telemetry utilizes `display-hero-mobile` and `metric-large` with tight negative letter-spacing, accompanied by `label-sm` or `label-md` units positioned directly underneath or adjacent in `text-secondary`.
- **Language Restraint**: Content labels stick strictly to concise, unadorned Korean terminology (e.g., 거리, 페이스, 시간, 케이던스, 평균 페이스, 최근 활동).

## Layout & Spacing

The layout is built for Android handheld usability, targeting a baseline 360 × 800 dp form factor. 

- **Grid Model**: A 4-column fluid mobile grid with 16dp (`margin`) screen edges and 12dp (`gutter`) column divisions. Metrics are grouped in clean 1-column, 2-column (split cards), or full-width 4-column hero modules.
- **Touch Targets**: During active runs, critical touch targets enforce a minimum height of 56dp to accommodate sweaty or gloved hand interactions while in motion.
- **Vertical Rhythm**: Layout pacing is strictly vertical and non-nested. Sections flow with 20dp to 28dp margins, preventing cramped data presentation.
- **Android Safe Zones**: Bottom navigation leaves 24dp padding clearance above the Android system navigation bar / gesture pill. Top metric displays maintain 48dp offset below the hardware camera cutout.

## Elevation & Depth

This system intentionally rejects blurry ambient drop shadows and neon outer glows. Elevation is conveyed strictly through tonal stepping and subtle structural hairline borders.

- **Level 0 (Canvas Base)**: `#0D0F09`. The root canvas across all three primary tabs.
- **Level 1 (Embedded Surface)**: `#161911` with a 1dp border of `#262B1E`. Used for stat cards, summary blocks, and the fixed 3-tab bottom bar.
- **Level 2 (Active Interactive & Floating Sheets)**: `#20241A` with a 1dp border of `#3A4130`. Used for bottom modal sheets, run-pause menus, and metric settings sheets.
- **Depth Boundaries**: Separation between card boundaries and parent frames relies on the contrast between `#161911` against `#0D0F09`, framed by the `#262B1E` stroke. No box-shadows are applied anywhere in the UI.

## Shapes

The interface embraces a functional, smooth corner geometry tailored to Android hardware edges:

- **Cards & Route Panels**: 16dp corner radius (`rounded-lg`), giving charts, vector maps, and metric summaries a solid container feel.
- **Action Buttons & Inputs**: 14dp corner radius on primary and secondary buttons, ensuring distinct tactile cohesion.
- **Chips & Badges**: 8dp corner radius (`rounded-md`) for metric pills, time range selectors, and filter toggles.
- **Floating Controls**: 24dp to full pill radius on small toggle buttons (e.g., GPS status indicators, lock buttons).

## Components

### Buttons
- **Primary CTA (달리기 시작 / 재개)**: 56dp height, full-width or large centered floating pill. Background `#C8F53A`, text/icon `#141A05`, 14dp radius, bold 16px label. Limit to 1 per screen.
- **Secondary Action (일시정지 / 구간 기록)**: 56dp height. Background `#23281C`, border 1dp `#262B1E`, text `#F1F4EA`, 14dp radius.
- **Destructive Action (러닝 종료 / 삭제)**: Long-press trigger (3-second circular progress stroke). Background `#20241A`, border 1dp `#F87171` at 40% opacity, text `#F87171`.

### Minimalist Route Canvas
- **Map View Replacement**: A dedicated canvas container (`#1A2010`) with 16dp rounded corners and a 1dp border (`#262B1E`). Zero street names, zero satellite imagery, zero road lines.
- **GPS Path**: Anti-aliased 2.5dp line rendered in pure `#FFFFFF`.
- **Start / End Markers**: Start point represented by a 6dp solid circle in `#C8F53A`. Current position/end point indicated by an 8dp `#FFFFFF` dot surrounded by a 2dp pulsing halo.

### Metric Cards & Telemetry Blocks
- **Hero Running Block**: Single massive card (`#161911`) housing primary metric (e.g., 거리 `5.24` km in 48px bold tabular numbers).
- **Secondary 2-Up Metrics**: Split cards displaying 페이스 (`5'32"`) and 시간 (`28:14`) side by side, utilizing 26px bold numbers and 12px `text-secondary` labels above each value.

### Navigation Dock (하단 탭)
- **Structure**: Fixed bottom navigation bar with 3 tabs: **홈**, **기록**, **내 정보**.
- **Container**: `#161911` with a top 1dp hairline border in `#262B1E`. 64dp height plus Android system bar inset.
- **Active State**: Active tab icon and label set to `#C8F53A` with a micro 4dp neon indicator dot beneath the label.
- **Inactive State**: Inactive tabs rendered in `#7B8270`.

### Chips & Filters
- **Period Filter (주간 / 월간 / 전체)**: Height 32dp, radius 8dp. Inactive: `#161911` with `#7B8270` text. Active: `#252F10` with `#DDF98A` text and 1dp border in `#3A4130`.

### Form & Settings Controls
- **Inputs**: Background `#23281C`, border 1dp `#262B1E`, text `#F1F4EA`, radius 14dp. Focus state shifts border to `#C8F53A`.
- **Toggles**: Track fill `#23281C`, active thumb `#C8F53A`, active track `#252F10`.