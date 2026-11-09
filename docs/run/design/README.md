# 디자인 시안 (Stitch)

화면을 구현할 때 레이아웃 참고용으로 봅니다. **색 · 글꼴 · 문구 · 기능 범위는 이 시안이 아니라 문서가 기준입니다.** 색은 [theme-guide.md](../theme-guide.md), 기능은 [product-spec.md](../product-spec.md)를 따르고, 시안과 문서가 다른 부분은 product-spec 11장 표에 있습니다.

- 시안은 볼트 라임 다크(`volt` · dark) 한 팔레트로만 만들었습니다. 다른 테마는 토큰 값만 바뀝니다.
- 폴더 이름은 화면 ID의 소문자입니다 (`product-spec.md` 4-2). 각 폴더에 `screen.png`(이미지)와 `code.html`(Stitch가 만든 HTML)이 있습니다.
- `code.html`은 구조를 읽는 용도입니다. 클래스 · 색 값을 코드로 옮기지 않고, 외부 이미지 · CDN을 앱에 넣지 않습니다.
- `_stitch-design-system/DESIGN.md`: Stitch가 만든 디자인 시스템 요약.
- 시안이 없는 화면(P1-b · P3-L · M4 · M8 · U1)은 product-spec 5 · 7장 동작과 같은 묶음의 시안을 기준으로 구성합니다.

## 화면별 경로

| ID | 화면 | 기능 모듈 | 시안 |
|---|---|---|---|
| O0 | 시작 | onboarding | [o0/](o0/screen.png) |
| O1 | 약관 동의 | onboarding | [o1/](o1/screen.png) |
| O2 | 기본 정보 | onboarding | [o2/](o2/screen.png) |
| O3 | 첫 목표 | onboarding | [o3/](o3/screen.png) |
| H1 | 홈 | home | [h1/](h1/screen.png) |
| P1-a | 준비: 권한 안내 | run | [p1-a/](p1-a/screen.png) |
| P1-b | 준비: 배터리 안내 | run | 없음 |
| P1-c | 준비: GPS 대기 | run | [p1-c/](p1-c/screen.png) |
| P2 | 카운트다운 | run | [p2/](p2/screen.png) |
| P3 | 러닝 중 | run | [p3/](p3/screen.png) |
| P3-L | 러닝 중 · 잠금 | run | 없음 |
| P4 | 일시정지 | run | [p4/](p4/screen.png) |
| P5 | 종료 확인 | run | [p5/](p5/screen.png) |
| P6 | 결과 | run | [p6/](p6/screen.png) |
| P7 | 공유 카드 | record | [p7/](p7/screen.png) |
| P8 | 기록 복구 | run | [p8/](p8/screen.png) |
| R1 | 기록 목록 | record | [r1/](r1/screen.png) |
| R2 | 기록 통계 | record | [r2/](r2/screen.png) |
| R3 | 기록 상세 | record | [r3/](r3/screen.png) |
| R4 | 기록 편집 | record | [r4/](r4/screen.png) |
| M1 | 내 정보 | settings | [m1/](m1/screen.png) |
| M2 | 목표 | settings | [m2/](m2/screen.png) |
| M3 | 러닝 설정 | settings | [m3/](m3/screen.png) |
| M4 | 내 정보 수정 | settings | 없음 |
| M5 | 테마 | settings | [m5/](m5/screen.png) |
| M6 | 데이터 · 백업 | settings | [m6/](m6/screen.png) |
| M7 | 약관 및 정책 | settings | [m7/](m7/screen.png) |
| M8 | 앱 정보 | settings | 없음 |
| M10 | 계정 · 회원 탈퇴 | settings | [m10/](m10/screen.png) |
| N1 | 진행 중 알림 | core:tracking | [n1/](n1/screen.png) |
| W1 | 홈 위젯 | widget | [w1/](w1/screen.png) |
| U1 | 강제 업데이트 | app | 없음 |
