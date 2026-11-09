# Firebase 설정

설계 원문: `docs/run/data-model-erd.md` (Firestore 구조 · 백업), `docs/run/infra-and-billing-protection.md` (Spark · 과금 방어)

## 파일

| 파일 | 내용 |
|---|---|
| `firestore.rules` | 보안 규칙 |
| `firestore.indexes.json` | 복합 인덱스 |
| `firebase.json` | 에뮬레이터 설정, Hosting(`../policy-site/public`, `cleanUrls`) |
| `tests/rules.test.js` | 보안 규칙 테스트 (vitest + `@firebase/rules-unit-testing`) |
| `package.json` · `.npmrc` | 테스트 도구 (버전 고정) |

`storage.rules` · `functions/`는 없다. Spark 요금제라 Cloud Storage · Cloud Functions를 쓰지 않는다.

## 규칙

- **Spark(무료)이고 결제 수단이 없다.** 한도를 넘으면 청구되지 않고 요청이 실패한다. 그래도 한도 소진 = 모든 사용자의 백업 중단이므로, 읽기 · 쓰기를 늘리는 규칙 · 넓게 열린 규칙은 이유와 최악의 경우를 설명한다.
- 사용자 데이터는 `users/{uid}/**` 아래에만 있고 **본인(`request.auth.uid == uid`)만** 읽고 쓴다. 다른 사용자 경로를 읽는 규칙을 만들지 않는다 (v1에 공유 기능 없음).
- App Check 강제는 콘솔에서 Firestore에 건다 (규칙 파일과 별개).
- 기록 문서(`users/{uid}/runs/{runId}`) 쓰기 조건:
  - `serverUpdatedAt == request.time` (변경분만 받기 위한 커서 전제)
  - `id == runId`, `schemaVersion`은 정수
  - `routePolyline`은 문자열 · 길이 20,000자 이하, `splits`는 리스트 · 200개 이하, `memo` 500자 이하, `title` 40자 이하
  - 원시 GPS 점 배열 같은 큰 필드를 받지 않도록 **허용 필드 목록(`keys().hasOnly`)** 을 둔다
- 일반 삭제는 soft delete(`deletedAt`)다. 실제 `delete`는 본인 문서에 한해 허용하되 앱은 **회원 탈퇴에서만** 호출한다.
- 목록 쿼리는 `request.query.limit <= 50`만 허용한다 (복원 시 페이지 단위로 읽음).
- 규칙 안의 `get()` · `exists()`도 읽기로 집계되므로 추가할 때 이유를 설명한다. 현재 규칙에는 없다.

## 명령어

| 목적 | 명령 |
|---|---|
| 설치 | `npm install` (`firebase/` 안에서) |
| 에뮬레이터 실행 (앱 개발, dev 빌드가 기본으로 연결) | `npx firebase emulators:start --only auth,firestore --project {firebase-project-id}` |
| 에뮬레이터 실행 (규칙만 볼 때) | `npx firebase emulators:start --project demo-run` |
| 규칙 테스트 | `npm test` (`demo-run` 가짜 프로젝트라 실제 Firebase에 닿지 않음. Java 21+ 필요) |
| Hosting 로컬 확인 | `npx firebase emulators:start --only hosting --project demo-run` → `http://127.0.0.1:5002/privacy` |
| 배포 | 직접 실행하지 않는다. 사용자에게 확인 후 진행 (`npx firebase deploy --only firestore:rules,firestore:indexes,hosting --project {firebase-project-id}`) |

## 규칙을 바꿀 때 반드시 테스트할 시나리오

- 본인은 자기 기록을 만들고 · 고치고 · soft delete할 수 있다
- 다른 사용자 · 비로그인 사용자는 읽기 · 쓰기 모두 거부된다
- `serverUpdatedAt`이 서버 시각이 아니면 거부된다
- 허용 목록에 없는 필드(예: `points`)가 있으면 거부된다
- `routePolyline` 20,000자 초과, splits 200개 초과, memo 500자 초과는 거부된다
- `limit` 없는 쿼리, `limit` 51 이상 쿼리는 거부된다
- 본인 문서 `delete`는 허용, 남의 문서 `delete`는 거부
