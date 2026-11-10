// 보안 규칙 테스트. firebase/CLAUDE.md "규칙을 바꿀 때 반드시 테스트할 시나리오"를 모두 포함한다.
// npm test가 demo-run 에뮬레이터를 띄운 뒤 실행한다 (실제 Firebase에 닿지 않음).
import { readFileSync } from 'node:fs';
import { afterAll, beforeAll, beforeEach, describe, test } from 'vitest';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import {
  Timestamp,
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  limit,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
} from 'firebase/firestore';

const ALICE = 'alice';
const BOB = 'bob';
const RUN_ID = '8f3c1a52-6b1e-4d7a-9c2f-0e4b7d9a1c33';
const GOAL_ID = '2b7e4f10-93c8-4a65-b1d2-7c5e8f0a6d44';

// product-spec 9장 시나리오: 2026.10.29(목) 아침 러닝 5.23km
const STARTED_AT = Date.UTC(2026, 9, 28, 21, 40); // 06:40 KST

function validRun(overrides = {}) {
  return {
    id: RUN_ID,
    schemaVersion: 1,
    status: 'FINISHED',
    startedAt: STARTED_AT,
    endedAt: STARTED_AT + 32 * 60_000,
    zoneId: 'Asia/Seoul',
    distanceM: 5230.4,
    movingMs: 30 * 60_000 + 12_000,
    elapsedMs: 32 * 60_000,
    calories: 312,
    splits: [
      { distanceM: 1000, movingMs: 348_000 },
      { distanceM: 1000, movingMs: 342_000 },
    ],
    routePolyline: '_p~iF~ps|U_ulLnnqC_mqNvxq`@',
    routeSegments: [3],
    title: '목요일 아침 러닝',
    memo: null,
    source: 'GPS',
    activityType: 'OUTDOOR_RUN',
    planId: null,
    createdAt: STARTED_AT + 32 * 60_000,
    updatedAt: STARTED_AT + 32 * 60_000,
    deletedAt: null,
    serverUpdatedAt: serverTimestamp(),
    ...overrides,
  };
}

function validGoal(overrides = {}) {
  return {
    id: GOAL_ID,
    type: 'DISTANCE',
    value: 20000,
    effectiveWeekStart: '2026-10-26',
    createdAt: STARTED_AT,
    updatedAt: STARTED_AT,
    deletedAt: null,
    serverUpdatedAt: serverTimestamp(),
    ...overrides,
  };
}

function validProfile(overrides = {}) {
  return {
    termsVersion: '2026-11-01',
    privacyVersion: '2026-11-01',
    locationTermsVersion: '2026-11-01',
    ageOver14: true,
    marketing: false,
    agreedAt: STARTED_AT,
    marketingUpdatedAt: null,
    weightKg: 65.5,
    distanceUnit: 'KM',
    profileUpdatedAt: STARTED_AT,
    updatedAt: STARTED_AT,
    serverUpdatedAt: serverTimestamp(),
    ...overrides,
  };
}

let env;

beforeAll(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-run',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});

afterAll(async () => {
  await env?.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
});

const db = (uid) => env.authenticatedContext(uid).firestore();
const guestDb = () => env.unauthenticatedContext().firestore();
const runRef = (firestore, uid = ALICE, id = RUN_ID) => doc(firestore, `users/${uid}/runs/${id}`);
const goalRef = (firestore, uid = ALICE, id = GOAL_ID) => doc(firestore, `users/${uid}/goals/${id}`);
const profileRef = (firestore, uid = ALICE) => doc(firestore, `users/${uid}`);

// 규칙을 거치지 않고 미리 넣어 둔다
async function seed(path, data) {
  await env.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), path), { ...data, serverUpdatedAt: Timestamp.now() });
  });
}

describe('기록 (users/{uid}/runs)', () => {
  test('본인은 기록을 만들고 고치고 soft delete할 수 있음', async () => {
    const ref = runRef(db(ALICE));
    await assertSucceeds(setDoc(ref, validRun()));
    await assertSucceeds(
      updateDoc(ref, { memo: '컨디션 좋음', updatedAt: STARTED_AT + 3_600_000, serverUpdatedAt: serverTimestamp() }),
    );
    await assertSucceeds(
      updateDoc(ref, { deletedAt: STARTED_AT + 7_200_000, updatedAt: STARTED_AT + 7_200_000, serverUpdatedAt: serverTimestamp() }),
    );
    await assertSucceeds(getDoc(ref));
  });

  test('다른 사용자는 읽기 · 쓰기 모두 거부됨', async () => {
    await seed(`users/${ALICE}/runs/${RUN_ID}`, validRun());
    const bob = db(BOB);
    await assertFails(getDoc(runRef(bob)));
    await assertFails(getDocs(query(collection(bob, `users/${ALICE}/runs`), limit(10))));
    await assertFails(setDoc(runRef(bob), validRun()));
    await assertFails(updateDoc(runRef(bob), { memo: 'x', serverUpdatedAt: serverTimestamp() }));
  });

  test('비로그인 사용자는 읽기 · 쓰기 모두 거부됨', async () => {
    await seed(`users/${ALICE}/runs/${RUN_ID}`, validRun());
    const guest = guestDb();
    await assertFails(getDoc(runRef(guest)));
    await assertFails(getDocs(query(collection(guest, `users/${ALICE}/runs`), limit(10))));
    await assertFails(setDoc(runRef(guest), validRun()));
  });

  test('serverUpdatedAt이 서버 시각이 아니면 거부됨', async () => {
    const ref = runRef(db(ALICE));
    await assertFails(setDoc(ref, validRun({ serverUpdatedAt: Timestamp.fromMillis(STARTED_AT) })));
    await assertFails(setDoc(ref, validRun({ serverUpdatedAt: STARTED_AT })));
    const { serverUpdatedAt, ...withoutServerTime } = validRun();
    await assertFails(setDoc(ref, withoutServerTime));
  });

  test('수정할 때도 serverUpdatedAt을 서버 시각으로 다시 써야 함', async () => {
    await seed(`users/${ALICE}/runs/${RUN_ID}`, validRun());
    await assertFails(updateDoc(runRef(db(ALICE)), { memo: '서버 시각 없음' }));
  });

  test('허용 목록에 없는 필드(points)가 있으면 거부됨', async () => {
    const points = [{ lat: 37.5665, lng: 126.978, recordedAt: STARTED_AT }];
    await assertFails(setDoc(runRef(db(ALICE)), validRun({ points })));
  });

  test('필수 필드가 빠지면 거부됨', async () => {
    const { routeSegments, ...withoutSegments } = validRun();
    await assertFails(setDoc(runRef(db(ALICE)), withoutSegments));
  });

  test('routePolyline은 20,000자까지 허용, 넘으면 거부됨', async () => {
    const ref = runRef(db(ALICE));
    await assertSucceeds(setDoc(ref, validRun({ routePolyline: 'a'.repeat(20_000) })));
    await assertFails(setDoc(ref, validRun({ routePolyline: 'a'.repeat(20_001) })));
    await assertFails(setDoc(ref, validRun({ routePolyline: ['a'] })));
  });

  test('splits는 200개까지 허용, 넘으면 거부됨', async () => {
    const ref = runRef(db(ALICE));
    const split = { distanceM: 1000, movingMs: 348_000 };
    await assertSucceeds(setDoc(ref, validRun({ splits: Array(200).fill(split) })));
    await assertFails(setDoc(ref, validRun({ splits: Array(201).fill(split) })));
  });

  test('memo 500자 · title 40자를 넘으면 거부됨', async () => {
    const ref = runRef(db(ALICE));
    await assertSucceeds(setDoc(ref, validRun({ memo: '가'.repeat(500), title: '가'.repeat(40) })));
    await assertFails(setDoc(ref, validRun({ memo: '가'.repeat(501) })));
    await assertFails(setDoc(ref, validRun({ title: '가'.repeat(41) })));
  });

  test('문서 id와 id 필드가 다르면 거부됨', async () => {
    await assertFails(setDoc(runRef(db(ALICE)), validRun({ id: 'other-id' })));
  });

  test('schemaVersion이 정수가 아니면 거부됨', async () => {
    const ref = runRef(db(ALICE));
    await assertFails(setDoc(ref, validRun({ schemaVersion: 1.5 })));
    await assertFails(setDoc(ref, validRun({ schemaVersion: '1' })));
  });

  test('끝나지 않은 기록(ACTIVE)은 올릴 수 없음', async () => {
    await assertFails(setDoc(runRef(db(ALICE)), validRun({ status: 'ACTIVE' })));
  });

  test('limit 없는 쿼리 · limit 51 이상 쿼리는 거부, 50은 허용', async () => {
    const runs = collection(db(ALICE), `users/${ALICE}/runs`);
    await assertFails(getDocs(runs));
    await assertFails(getDocs(query(runs, limit(51))));
    await assertSucceeds(getDocs(query(runs, limit(50))));
  });

  test('본인 문서 delete는 허용, 남의 문서 delete는 거부', async () => {
    await seed(`users/${ALICE}/runs/${RUN_ID}`, validRun());
    await assertFails(deleteDoc(runRef(db(BOB))));
    await assertFails(deleteDoc(runRef(guestDb())));
    await assertSucceeds(deleteDoc(runRef(db(ALICE))));
  });
});

describe('목표 (users/{uid}/goals)', () => {
  test('본인은 목표를 만들고 고치고 지울 수 있음', async () => {
    const ref = goalRef(db(ALICE));
    await assertSucceeds(setDoc(ref, validGoal()));
    await assertSucceeds(updateDoc(ref, { value: 25000, updatedAt: STARTED_AT + 1, serverUpdatedAt: serverTimestamp() }));
    await assertSucceeds(deleteDoc(ref));
  });

  test('다른 사용자 · 비로그인 사용자는 거부됨', async () => {
    await seed(`users/${ALICE}/goals/${GOAL_ID}`, validGoal());
    await assertFails(getDoc(goalRef(db(BOB))));
    await assertFails(setDoc(goalRef(db(BOB)), validGoal()));
    await assertFails(getDoc(goalRef(guestDb())));
    await assertFails(deleteDoc(goalRef(db(BOB))));
  });

  test('type · value · effectiveWeekStart 형식이 틀리면 거부됨', async () => {
    const ref = goalRef(db(ALICE));
    await assertFails(setDoc(ref, validGoal({ type: 'PACE' })));
    await assertFails(setDoc(ref, validGoal({ value: 0 })));
    await assertFails(setDoc(ref, validGoal({ effectiveWeekStart: '2026.10.26' })));
    await assertFails(setDoc(ref, validGoal({ extra: true })));
    await assertFails(setDoc(ref, validGoal({ serverUpdatedAt: Timestamp.now() })));
  });

  test('limit 없는 쿼리 · limit 51 이상 쿼리는 거부', async () => {
    const goals = collection(db(ALICE), `users/${ALICE}/goals`);
    await assertFails(getDocs(goals));
    await assertFails(getDocs(query(goals, limit(51))));
    await assertSucceeds(getDocs(query(goals, limit(50))));
  });
});

describe('프로필 (users/{uid})', () => {
  test('본인은 프로필을 만들고 일부 필드만 고칠 수 있음', async () => {
    const ref = profileRef(db(ALICE));
    await assertSucceeds(setDoc(ref, validProfile()));
    await assertSucceeds(
      setDoc(ref, { weightKg: 64, updatedAt: STARTED_AT + 1, serverUpdatedAt: serverTimestamp() }, { merge: true }),
    );
    await assertSucceeds(getDoc(ref));
  });

  test('동의 필드 없이 체중 · 단위만으로 만들 수 있음', async () => {
    await assertSucceeds(
      setDoc(profileRef(db(ALICE)), {
        weightKg: null,
        distanceUnit: 'MILE',
        updatedAt: STARTED_AT,
        serverUpdatedAt: serverTimestamp(),
      }),
    );
  });

  test('다른 사용자 · 비로그인 사용자는 거부됨', async () => {
    await seed(`users/${ALICE}`, validProfile());
    await assertFails(getDoc(profileRef(db(BOB))));
    await assertFails(setDoc(profileRef(db(BOB)), validProfile()));
    await assertFails(getDoc(profileRef(guestDb())));
    await assertFails(deleteDoc(profileRef(db(BOB))));
  });

  test('허용 목록 밖 필드 · 잘못된 값 · 서버 시각 아님은 거부됨', async () => {
    const ref = profileRef(db(ALICE));
    await assertFails(setDoc(ref, validProfile({ email: 'runner@example.com' })));
    await assertFails(setDoc(ref, validProfile({ distanceUnit: 'METER' })));
    await assertFails(setDoc(ref, validProfile({ ageOver14: 'yes' })));
    await assertFails(setDoc(ref, validProfile({ serverUpdatedAt: Timestamp.now() })));
  });

  test('본인 프로필 delete는 허용', async () => {
    await seed(`users/${ALICE}`, validProfile());
    await assertSucceeds(deleteDoc(profileRef(db(ALICE))));
  });
});

describe('정해지지 않은 경로', () => {
  test('본인이어도 다른 하위 컬렉션 · 최상위 컬렉션은 거부됨', async () => {
    const alice = db(ALICE);
    await assertFails(setDoc(doc(alice, `users/${ALICE}/points/p1`), { lat: 37.5 }));
    await assertFails(getDoc(doc(alice, `users/${ALICE}/points/p1`)));
    await assertFails(setDoc(doc(alice, 'runs/r1'), validRun()));
    await assertFails(getDocs(query(collection(alice, 'users'), limit(10))));
  });
});
