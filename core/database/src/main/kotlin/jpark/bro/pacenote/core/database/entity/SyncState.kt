package jpark.bro.pacenote.core.database.entity

/** 백업 상태. `sync_state` 컬럼에 이름 그대로 저장한다 (data-model-erd.md 3장). */
enum class SyncState {
    /** 게스트 기록: 올리지 않음 */
    LOCAL,

    /** 올릴 차례 */
    PENDING,

    /** 서버와 같음 */
    SYNCED,

    /** 규칙 거부: 재시도하지 않음 */
    FAILED,
}
