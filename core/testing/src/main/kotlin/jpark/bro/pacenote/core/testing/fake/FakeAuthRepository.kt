package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** 기본은 게스트(null). [userId]를 바꾸면 로그인 · 로그아웃을 흉내 낸다. */
class FakeAuthRepository(
    userId: String? = null,
) : AuthRepository {
    val userId = MutableStateFlow(userId)

    override fun observeUserId(): Flow<String?> = userId
}
