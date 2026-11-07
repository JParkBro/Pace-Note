# 런닝앱(pacenote) 프로젝트 문서

러닝 기록 앱 **pacenote**에만 해당하는 문서입니다. 코드 구조 · 규칙 · 테스트 같은 범용 내용은 [`../common/`](../common/README.md)을 따르고, 이 폴더에는 이 앱의 결정 사항만 둡니다.

## 문서 목록

| 문서 | 내용 | 주로 보는 시점 |
|---|---|---|
| [roadmap.md](roadmap.md) | 마일스톤(v1 · v2 · v3) 범위, v1 확정 결정, 기능 추가 절차, 기능 플래그, 데이터 호환 규칙, 출시 기준 | 기획 · 기능 추가 · 출시 |
| [theme-guide.md](theme-guide.md) | 4 테마 × 라이트/다크 = 8 팔레트 색 값, route 토큰, 런닝앱 전용 색 규칙, 명도 대비 | 디자인 · UI 개발 |
| [theme-foundation.md](theme-foundation.md) | 토큰 정의, 공통 사용 규칙, 컴포넌트 매핑, 타이포 · 모서리 · 간격, Compose 적용 (데이즈에서 옮겨 옴) | 디자인 시스템 구현 |
| `assets/theme-tokens.json` | 테마 색상 값 원본 (8 팔레트 × 31 토큰 + 고정 색) | `scripts/generate-theme-colors.py` 입력 |
| `assets/theme-tokens.css` | 같은 값의 CSS 변수 | 정책 페이지 · 웹 |
| `assets/theme-preview.html` | 8 팔레트 미리보기 | 디자인 확인 |
| `features/{feature}.md` | 기능 정의 1페이지 (roadmap 4장 템플릿) | 기능 추가 시 작성 |

## 핵심 결정 요약 (roadmap 3장)

- **지도 없음.** 경로는 `route-canvas` 위 Canvas 흰 선.
- **Google 로그인만, 로그인 없이도 사용 가능.** v1에는 Cloudflare Worker 없음.
- **Firebase Spark(결제 수단 없음).** Auth · Firestore · Remote Config · App Check · Crashlytics.
- **원시 GPS 점은 기기(Room)에만.** 서버에는 기록 요약 + 단순화한 경로(인코딩 polyline), 기록 1건 = Firestore 문서 1개.
- 정책 페이지는 Cloudflare Pages.
- 기본 테마 `volt`(볼트 라임).

## 공통 문서와 다르게 가는 부분

| 항목 | 공통 규칙 | pacenote | 이유 |
|---|---|---|---|
| versionCode 계산 | GIT_CONVENTIONS 6-2 | `MAJOR*1_000_000 + MINOR*10_000 + PATCH*100 + 빌드번호` (`gradle/libs.versions.toml`의 `projectVersionCode`) | roadmap 1장 |

## 아직 없는 문서

roadmap이 참조하지만 이 저장소에 없는 문서입니다. 만들면 위 목록으로 옮깁니다.

- KICKOFF (versionCode 규칙 원문 3-3)
- `wireframes.html` (화면 ID: O0, R5, H1, W2 …)
- 모듈 맵 (모듈 목록 · 내비게이션 키 · 기능 간 의존)
