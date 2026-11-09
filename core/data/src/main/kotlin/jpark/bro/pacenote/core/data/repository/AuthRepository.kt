package jpark.bro.pacenote.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** 현재 사용자. 게스트면 uid가 null이고, 기록 · 목표의 소유자(`owner_user_id`)로 쓴다. */
interface AuthRepository {
    fun observeUserId(): Flow<String?>
}

/** Google 로그인(module-map 9-3) 전까지 쓰는 게스트 전용 구현 */
internal class GuestAuthRepository : AuthRepository {
    override fun observeUserId(): Flow<String?> = flowOf(null)
}
