package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.database.entity.SyncState

/** 게스트 기록은 올리지 않고(LOCAL), 로그인 사용자 기록은 올릴 차례(PENDING)로 둔다. */
internal fun syncStateFor(ownerUserId: String?): String =
    if (ownerUserId == null) SyncState.LOCAL.name else SyncState.PENDING.name
