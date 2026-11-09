# 런닝앱(pacenote) 프로젝트 문서

입문~중급 러너를 위한 러닝 기록 앱에만 해당하는 문서입니다. 코드 구조 · 규칙 · 테스트 같은 범용 내용은 [`../common/`](../common/README.md)을 따르고, 이 폴더에는 이 앱의 결정 사항만 둡니다.

## 문서 목록

| 문서 | 내용 | 주로 보는 시점 |
|---|---|---|
| [product-spec.md](product-spec.md) | 기능 범위, 계산 규칙, 화면 목록 · 동작, 설정 구조, 위젯, 샘플 데이터 | 기획 · 디자인 · 개발 전반 |
| [tracking.md](tracking.md) | **위치 추적 설계**: 포그라운드 서비스, 권한, 점 필터 · 거리 · 페이스 · 자동 일시정지, 저장 · 복구, 음성 안내, 경로 그리기, 테스트 | 러닝 기능 개발 |
| [theme-guide.md](theme-guide.md) | 4 테마 × 라이트/다크 = 8 팔레트, 토큰 31개, 타이포 · 간격, 공통 컴포넌트, Compose 적용 | 디자인 · UI 개발 |
| [module-map.md](module-map.md) | 모듈 목록, 내비게이션 키, 기능 간 의존, 화면별 Event, 개발 순서 | 개발 시작 · 기능 추가 |
| [data-model-erd.md](data-model-erd.md) | Room · Firestore 배치, 백업 · 복원 흐름, Entity 예시, 용량 추정 | 데이터 작업 |
| [infra-and-billing-protection.md](infra-and-billing-protection.md) | Firebase Spark 구성, 무료 한도, 과금 방어, 출시 전 체크리스트 | 서버 작업 · 출시 전 |
| [ads-strategy.md](ads-strategy.md) | v1 광고 없음, 광고를 넣게 될 때의 원칙 | 수익화 검토 시 |
| [roadmap.md](roadmap.md) | v1 · v2 · v3 범위, 기능 추가 절차, 기능 플래그, 데이터 호환 규칙 | 기능 계획 |
| [stitch-prompts.md](stitch-prompts.md) | Google Stitch 공통 컨텍스트 + 화면 묶음 6개 프롬프트 | 시안 작업 |
| `assets/theme-tokens.json` | 테마 색상 값 원본 (8 팔레트 × 31 토큰) | 디자인 시스템 코드 생성 |
| `assets/theme-tokens.css` | 같은 값의 CSS 변수 (정책 페이지 · 웹 미리보기) | 웹 작업 |
| `assets/theme-preview.html` | 8 팔레트 미리보기 | 테마 검토 |
| [design/README.md](design/README.md) | 화면 ID별 Stitch 시안 경로. `design/{화면 ID}/`에 `screen.png` + `code.html`, `design/_stitch-design-system/DESIGN.md`. 색 · 글꼴 · 기능 범위는 이 폴더 문서가 기준이고 시안은 레이아웃 참고용 | 화면 구현 |

## AI 도구 지침 (Claude Code)

저장소 루트와 하위 폴더의 `CLAUDE.md`가 이 문서들을 기준으로 작업 지침을 요약합니다. 문서의 결정이 바뀌면 해당 `CLAUDE.md`도 함께 고칩니다.

| 파일 | 범위 |
|---|---|
| `CLAUDE.md` | 프로젝트 전체: 문서 지도, 절대 규칙, Git, 비밀 정보, 언어 + Android 앱(모듈 · MVI · 데이터 · UI 규칙, Gradle 명령, 테스트 요구 사항) |
| `firebase/CLAUDE.md` | 보안 규칙 원칙, 필수 테스트 시나리오 |
| `widget/CLAUDE.md` | Glance 위젯 표시 · 갱신 규칙 |

> 처음 읽을 때는 product-spec → tracking → theme-guide → module-map → data-model-erd 순서를 추천합니다.

## 핵심 결정 요약

**제품**
- 입문~중급 대상, 혼자 쓰는 앱. 소셜 · 피드 · 랭킹 없음. 기록 공유는 이미지 카드로만.
- **지도 없음.** 경로는 어두운 영역(`route-canvas`) 위 흰 선. 공유 카드에 장소 정보가 드러나지 않음.
- 로그인 없이 바로 시작. **Google 로그인**하면 기록 백업 · 복원 (카카오 로그인 없음).
- 하단 탭은 홈 / 기록 / 내 정보. 러닝 흐름은 탭 밖 전체 화면.

**기술**
- 원본 데이터는 Room. 원시 GPS 점은 기기에만, 서버에는 요약 + 단순화한 경로만.
- 추적은 사용자가 화면에서 시작한 **위치 타입 포그라운드 서비스**. 백그라운드 위치 권한 없음.
- 계산(거리 · 페이스 · 스플릿 · 칼로리 · 자동 일시정지)은 `core:domain`, GPX 재생 테스트로 검증.
- **서버는 Firebase Spark 한 곳, 결제 수단 없음.** Auth · Firestore · Remote Config · App Check · Crashlytics · Hosting. Cloud Functions · Storage · Cloudflare 없음.
- dev · prod 빌드가 Firebase 프로젝트 하나를 함께 쓰고, 디버그 빌드는 에뮬레이터 연결이 기본.
- 모든 ID는 UUID, 날짜는 `yyyy-MM-dd`, 시각은 epoch millis.

**디자인**
- 테마 4종(볼트 라임 기본, 선라이즈, 리버 블루, 잉크 모노) × 라이트/다크. 데이즈와 같은 27 토큰 + 경로 토큰 4개.
- 글꼴 Pretendard. 러닝 중 숫자는 가장 큰 크기 · `textPrimary`.

**수익화**
- v1은 광고 · 결제 없음 (`ads-strategy.md`).

## 공통 문서와 다르게 가는 부분

공통 규칙에서 벗어나는 결정을 이유와 함께 기록합니다.

| 항목 | 공통 규칙 | 런닝앱 | 이유 |
|---|---|---|---|
| Room 스키마 버전 | 스키마를 바꾸면 version을 올리고 Migration · 마이그레이션 테스트 추가 | **출시 전까지 version 1을 고쳐 쓰고** `schemas/…/1.json`을 덮어씀. 개발 기기에서 스키마가 안 맞으면 앱 데이터를 지우고 다시 설치 | 출시 전에는 보존할 사용자 데이터가 없어 Migration이 비용만 듦. **첫 출시 빌드부터는 공통 규칙을 따름** |
| `core:network` (Ktor) | 공통 스택에 포함 | v1에는 만들지 않음 (`pacenote.jvm.ktor` 플러그인 · Ktor 카탈로그 항목도 없음) | 외부 HTTP 호출이 없음. 필요해지면 추가 |
| PR 필수 검사 `screenshot` | `pr-title` · `lint` · `unit-test` · `screenshot` | Roborazzi를 도입할 때까지 `screenshot` 제외 | 아직 화면이 없고 Roborazzi를 구성하지 않음 |
| 자동 증가 ID 금지 | 모든 ID는 UUID | `run_point`는 (`run_id`, `seq`) 복합 키 | 기록 하나에 점이 수천 개라 UUID 비용이 크고, 기기 밖으로 나가지 않음 |

> 요금 · 정책 수치는 2026년 10~11월 기준으로 확인한 내용입니다. 출시 전 공식 문서에서 다시 확인하세요.
