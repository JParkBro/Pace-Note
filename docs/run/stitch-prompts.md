# Google Stitch 프롬프트 (런닝앱 v1)

> `product-spec.md` 4-2의 화면 ID 순서대로 6개 묶음으로 나눴습니다.
> Stitch에는 **볼트 라임 다크(`volt` · dark)** 한 가지 팔레트만 넣습니다. 다른 테마는 토큰 값만 바꾸면 되므로 시안은 하나로 충분합니다.
> 받은 시안은 [design/README.md](design/README.md)에 화면 ID별로 정리되어 있습니다.

## 사용 순서

1. 새 Stitch 프로젝트를 열고 **0. 공통 컨텍스트**를 먼저 텍스트로 붙여넣습니다. (Stitch에 디자인 시스템 · DESIGN.md 입력란이 있으면 거기에, 없으면 첫 메시지로) `theme-guide.md` · HTML 파일은 보내지 않습니다 (8개 팔레트가 섞임).
2. 묶음 1부터 하나씩 보냅니다. 한 번에 한 묶음(3~5화면)만 보냅니다.
3. 결과마다 아래 **점검 목록**으로 확인하고, 어긋난 부분은 같은 대화에서 짧게 수정 요청합니다.
4. 스크린샷 · HTML · DESIGN.md를 받아 다음 세션(4단계: 결과 분석)에 첨부합니다.

## 결과 점검 목록 (매 묶음마다)

- [ ] primary가 `#C8F53A`에서 바뀌지 않았는가 (Material 톤으로 임의 변경하는 경향)
- [ ] 헤더 · 버튼 문구가 한국어인가 (영어로 바뀌는 경향)
- [ ] **지도 · 지도 타일 · 위성 사진이 들어가지 않았는가** (런닝앱이라 넣으려는 경향이 강함)
- [ ] 하위 화면에 프로필 아이콘 · 알림 벨이 붙지 않았는가
- [ ] 계획에 없는 기능(소셜 피드, 친구, 심박수, 랭킹, 배지 진열장, 광고 배너)이 없는가
- [ ] 샘플 데이터 숫자가 아래 시나리오와 같은가
- [ ] 하단 탭이 홈 · 기록 · 내 정보 3개뿐인가

---

## 0. 공통 컨텍스트 (최초 1회)

```
You are designing a Korean Android running app called "런닝" (working title).
Target users: beginner to intermediate runners who want to run consistently. Solo use only.

PLATFORM & STYLE
- Android phone, 360x800 dp frames. Clean, calm, modern. Generous spacing, rounded corners (12–16 dp cards, 14 dp buttons).
- Font: Pretendard for everything. Numbers use tabular figures. Big metrics are bold and very large.
- All UI text must be in Korean exactly as written in my prompts. Do not translate headers or buttons to English.
- Dark mode only for this project.

COLOR TOKENS (use exactly these hex values, do not generate a new palette, do not shift the primary color)
- primary #C8F53A (main buttons, active tab, toggle on, progress fill) — use on 1–2 elements per screen only
- on-primary #141A05 (text/icons on primary)
- primary-soft #252F10 / on-primary-soft #DDF98A (highlight badges)
- accent #7DD3A0 (charts and graphic accents only, never body text)
- bg #0D0F09 (screen background)
- surface #161911 (cards, list items, bottom tab bar)
- surface-raised #20241A (bottom sheets, dialogs)
- fill #23281C (secondary buttons, segmented control track, input background, progress track)
- border #262B1E, border-strong #3A4130
- text-primary #F1F4EA, text-secondary #A9B09D, text-tertiary #7B8270
- danger #F87171 (delete, destructive text), success #5BD69A
- route-canvas #1A2010 (background area behind the route line), route-line #FFFFFF, route-start #C8F53A

THE MOST IMPORTANT RULE: NO MAPS
- This app never shows a map. No map tiles, no streets, no satellite images, no map pins.
- A run route is shown ONLY as a single smooth white line (#FFFFFF, 3–4 dp, round caps) drawn on a dark rounded rectangle (route-canvas #1A2010).
- Start point: small filled dot in #C8F53A. End point: small white ring.
- In lists, the route appears as a small square thumbnail with the same white line.

NAVIGATION
- Bottom tab bar with exactly 3 tabs: 홈, 기록, 내 정보. Active tab uses primary; inactive uses text-tertiary.
- Sub-screens (anything opened from a tab) have a back arrow and a Korean title at the top, and NO bottom tab bar, NO profile icon, NO notification bell.
- Running screens (countdown, running, paused) are full-screen without tab bar or top bar.

DO NOT ADD
- Social feed, friends, followers, leaderboards, challenges, heart rate, music player, weather, ads/banners, achievement badge shelves, premium/upgrade buttons, chat, or any feature not described in my prompts.

SAMPLE DATA (use these exact values everywhere they apply). Today is 2026-10-29 (목).
- This week (10.26 – 11.1): 12.4 / 15 km, 3회, 1:12:40, 평균 5′52″
- Latest run: 10월 28일 (수) 오전 6:40, 5.21 km, 30:12, 평균 페이스 5′48″, 351 kcal
  Splits: 1 km 5′52″ / 2 km 5′44″ / 3 km 5′40″ / 4 km 5′50″ / 5 km 5′47″ / 0.21 km 6′16″
  Title: 수요일 아침 러닝, Memo: 한강 바람 시원했음
- Other runs this week: 10월 27일 (화) 3.21 km 18:23 5′44″ / 10월 26일 (월) 3.98 km 24:05 6′03″
- October total: 42.3 km · 8회. September: 51.0 km · 10회
- Lifetime: 312.5 km, 54회
- Live run (in progress): 3.42 km, 20:31, 현재 페이스 5′55″

Wait for my screen requests. Generate only the screens I ask for in each message.
```

---

## 1. 첫 실행 (O0 · O1 · O2 · O3)

```
Generate 4 onboarding screens. No bottom tab bar on any of them.

[O0 시작]
- Center: app logo placeholder (rounded square in primary), title "매일 조금씩, 꾸준히", subtitle "기록은 기기에 저장되고, 로그인하면 백업돼요".
- Bottom: primary button "Google로 시작" with the standard Google "G" logo, below it an outline button "로그인 없이 시작".

[O1 약관 동의]
- Title "약관 동의". A bordered row "전체 동의" with a checkbox.
- Checkbox rows with a chevron to view each: "[필수] 만 14세 이상입니다", "[필수] 서비스 이용약관", "[필수] 개인정보 처리방침", "[필수] 위치정보 이용약관", "[선택] 마케팅 정보 수신" (unchecked).
- Bottom primary button "동의하고 계속".

[O2 기본 정보]
- Back arrow, title "기본 정보", top-right text button "건너뛰기".
- Label "체중" with input "65 kg" and helper text "칼로리 계산에만 사용해요".
- Label "거리 단위" with segmented control "km | mile" (km selected).
- Bottom primary button "다음".

[O3 첫 목표]
- Back arrow, title "이번 주 목표", top-right "건너뛰기".
- Segmented control "주간 거리 | 주간 횟수" (주간 거리 selected).
- Large stepper: round "−" button, big number "15" with small "km", round "+" button.
- Helper text "처음이라면 주 10~15km를 추천해요".
- Bottom primary button "시작하기".
```

## 2. 하단 탭 (H1 · R1 · R2 · M1)

```
Generate the 4 main tab screens. All show the bottom tab bar (홈 · 기록 · 내 정보).

[H1 홈] (tab 홈 active)
- Header "이번 주", right side small text "10.26 – 11.1".
- Large circular progress ring (track fill, progress primary) with "12.4" big and "/ 15 km" small inside.
- Row of 3 stats: "3회 러닝", "1:12:40 시간", "5′52″ 평균 페이스".
- Very prominent full-width primary button "달리기 시작". This is the hero of the screen.
- Section "최근 기록": one card with a white-line route thumbnail on route-canvas, "5.21 km", "어제 · 오전 6:40", "30:12 · 5′48″".

[R1 기록 · 목록] (tab 기록 active)
- Header "기록". Segmented control "목록 | 통계" (목록 selected).
- Month header row: "10월" left, "42.3 km · 8회" right.
- 3 list items, each with a square white-line route thumbnail, distance bold, date, time · pace: 10월 28일 (수) 5.21 km 30:12 · 5′48″ / 10월 27일 (화) 3.21 km 18:23 · 5′44″ / 10월 26일 (월) 3.98 km 24:05 · 6′03″.
- Next month header "9월" / "51.0 km · 10회".

[R2 기록 · 통계] (tab 기록 active)
- Header "기록". Segmented control "목록 | 통계" (통계 selected).
- Chips "주" (selected) and "월". Period navigator "‹ 10.26 – 11.1 ›".
- Bar chart Mon–Sun (월 화 수 목 금 토 일) with bars on 월 (3.98), 화 (3.21), 수 (5.21) in accent color; 목~일 empty (future days).
- 3 stats: "12.4km 거리", "3회 횟수", "5′52″ 평균".
- Helper text "지난주보다 2.1km 더 달렸어요".

[M1 내 정보] (tab 내 정보 active)
- Header "내 정보".
- Card with two stats side by side: "312.5km 누적 거리", "54회 누적 러닝".
- Card "로그인하면 기록이 백업돼요" with a small Google login button "Google로 로그인".
- Settings list rows with chevrons: "목표" (value "주 15km"), "러닝 설정", "내 정보 수정", "테마" (value "볼트 라임"), "데이터 · 백업", "약관 및 정책", "앱 정보" (value "1.0.0").
```

## 3. 러닝 준비 · 러닝 중 (P1-a · P1-c · P2 · P3 · P4)

```
Generate 5 screens for starting and during a run. No bottom tab bar.

[P1-a 권한 안내]
- Back arrow only. Center: simple location illustration (abstract, no map), title "달린 거리를 재려면 위치 권한이 필요해요", body "달리는 동안에만 사용하고, 화면이 꺼져도 기록할 수 있도록 알림을 함께 띄워요".
- Bottom primary button "권한 허용하기".

[P1-c GPS 대기]
- Center: GPS signal bars icon (3 of 4 bars lit), title "GPS 신호를 찾고 있어요", body "하늘이 트인 곳이 잘 잡혀요".
- Bottom: disabled primary button "시작" (low opacity), outline button "신호 없이 시작".

[P2 카운트다운]
- Full-screen bg. Huge centered number "3" (around 160 sp), small text below "화면을 탭하면 바로 시작".

[P3 러닝 중]
- Full screen, no top bar. Small lock icon at top-right.
- Label "거리 (km)", then a huge number "3.42" (largest element on screen, text-primary, not primary color).
- Two stats side by side, large: "20:31 시간", "5′55″ 현재 페이스".
- A route-canvas card showing the white route line still being drawn, with the current position as a filled primary dot at the end of the line.
- Bottom center: large circular primary pause button.

[P4 일시정지]
- Small pill badge at top "일시정지됨" (primary-soft background, on-primary-soft text).
- Huge "3.42", stats "20:31 시간", "5′58″ 평균 페이스".
- Route-canvas card with the white route line.
- Bottom: two large circular buttons side by side: resume (primary, play icon) and stop (fill color, square icon). Caption "종료는 길게 누르기".
```

## 4. 러닝 종료 · 결과 (P5 · P6 · P7 · P8)

```
Generate 4 screens for finishing a run. No bottom tab bar.

[P5 종료 확인]
- The paused running screen dimmed by an overlay, with a bottom sheet (surface-raised) on top.
- Sheet: drag handle, title "러닝을 마칠까요?", subtitle "3.42km · 20:31", primary button "저장하고 마치기", outline button "계속 달리기".

[P6 결과]
- Top bar: title "10월 28일 오전 6:40" (no back arrow), right text button "완료".
- Large route-canvas card with the full white route line, start dot and end ring.
- 2x2 stats grid: "5.21 km 거리", "30:12 시간", "5′48″ 평균 페이스", "351 kcal 칼로리".
- Card: "이번 주 목표 83% · 2.6km 남았어요" with a progress bar.
- Section "구간": simple table of km splits from the sample data.
- Input field placeholder "메모 남기기".
- Primary button "공유 카드 만들기".

[P7 공유 카드]
- Back arrow, title "공유 카드".
- Card preview (route-canvas background, rounded): white route line, below it "5.21 km" big, "30:12 · 5′48″/km", "2026.10.28". No logo watermark larger than a tiny wordmark.
- Label "배경" with chips "기본" (selected), "사진", "투명".
- Label "표시할 정보" with chips "거리", "시간", "페이스" (selected) and "날짜" (unselected).
- Bottom: outline button "이미지 저장" and primary button "공유" side by side.

[P8 기록 복구]
- Dimmed overlay with a centered dialog (surface-raised).
- Title "진행 중이던 러닝이 있어요", body "오전 6:40에 시작 · 3.42km".
- Primary button "이어서 달리기", outline button "저장하고 마치기", danger text button "기록 버리기".
```

## 5. 기록 상세 · 설정 하위 (R3 · R4 · M3 · M5 · M6)

```
Generate 5 sub-screens. Each has a back arrow and Korean title. No bottom tab bar, no profile icon.

[R3 기록 상세]
- Title "10월 28일 (수)", right "⋯" more icon.
- Route-canvas card with the white route line.
- 2x2 stats grid (same as the result screen).
- Section "페이스": simple line chart in accent color showing pace per km.
- Section "구간": splits table.
- Memo card "메모: 한강 바람 시원했음".
- Primary button "공유 카드 만들기".

[R4 기록 편집]
- Title "기록 편집", right text button "저장".
- Label "제목" input "수요일 아침 러닝". Label "메모" multiline input "한강 바람 시원했음".
- At the very bottom, danger text button "이 기록 삭제".

[M3 러닝 설정]
- Title "러닝 설정".
- Group "음성 안내": toggle row "음성 안내" (on), row "안내 간격" value "1km", row "안내 항목" value "거리 · 페이스".
- Group "기록": toggle row "자동 일시정지" (on) with helper "멈추면 기록도 멈춰요", row "카운트다운" value "3초", toggle row "러닝 중 화면 켜짐 유지" (off), row "배터리 최적화 안내 다시 보기".

[M5 테마]
- Title "테마".
- 2x2 grid of theme cards, each with a color circle and name: "볼트 라임" (#C8F53A, selected with primary border), "선라이즈" (#FB8A4C), "리버 블루" (#45C9DE), "잉크 모노" (#F4F4F5).
- Label "화면 모드" with segmented control "시스템 | 라이트 | 다크".
- Label "미리보기" with a small route-canvas card showing a white route line.

[M6 데이터 · 백업]
- Title "데이터 · 백업".
- Status card "백업 켜짐", "마지막 백업: 오늘 오전 7:12" with a small success check icon.
- Toggle row "러닝 후 자동 백업" (on), rows "지금 백업하기", "백업에서 복원하기".
- At the very bottom, danger text button "기기의 모든 기록 삭제".
```

## 6. 나머지 하위 · 시스템 (M2 · M7 · M10 · N1 · W1)

```
Generate 5 screens.

[M2 목표] Back arrow, title "목표". Segmented "주간 거리 | 주간 횟수", big stepper "− 15 km +", helper "최근 4주 평균 12.8km", bottom primary button "저장".

[M7 약관 및 정책] Back arrow, title "약관 및 정책". List rows with chevrons: "개인정보 처리방침" (bold, first), "서비스 이용약관", "위치정보 이용약관", "오픈소스 라이선스". Nothing else.

[M10 계정] Back arrow, title "계정". Card with "runner@gmail.com" and "Google 계정". Row "로그아웃". At the very bottom, danger text button "회원 탈퇴".

[N1 진행 중 알림] An Android notification shade mockup showing one expanded ongoing notification from this app: small app icon, "러닝 기록 중 · 20:31", bold "3.42 km · 5′55″/km", action buttons "일시정지" and "열기". Other notifications blurred placeholders.

[W1 홈 위젯] An Android home screen mockup with a 4x2 widget (surface background, rounded 24 dp): "이번 주", "12.4 / 15 km", progress bar in primary, small primary pill button "달리기 시작". Some blurred app icons below.
```

---

## 자주 쓰는 수정 요청 문구

| 상황 | 보낼 문구 |
|---|---|
| 지도가 들어감 | `Remove the map completely. Show the route only as a white line on the dark route-canvas card (#1A2010).` |
| primary가 바뀜 | `Restore primary to exactly #C8F53A and on-primary to #141A05. Do not adjust the palette.` |
| 영어 헤더 | `Use the exact Korean text from my prompt for all headers and buttons.` |
| 기능 추가됨 | `Remove {요소}. It is out of scope. Do not add features I did not describe.` |
| 숫자가 작음 | `Make the main metric much larger; it must be readable at arm's length while running.` |
