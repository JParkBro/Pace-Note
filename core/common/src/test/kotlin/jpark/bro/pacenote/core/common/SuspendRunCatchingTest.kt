package jpark.bro.pacenote.core.common

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class SuspendRunCatchingTest {
    @Test
    fun `성공하면 값을 담아 돌려줌`() =
        runTest {
            assertEquals(Result.success(1), suspendRunCatching { 1 })
        }

    @Test
    fun `일반 예외는 실패로 돌려줌`() =
        runTest {
            val error = IllegalStateException("실패")

            assertEquals(error, suspendRunCatching<Int> { throw error }.exceptionOrNull())
        }

    @Test
    fun `취소 예외는 삼키지 않고 다시 던짐`() =
        runTest {
            val error = CancellationException("취소")

            val caught =
                try {
                    suspendRunCatching<Int> { throw error }
                    null
                } catch (e: CancellationException) {
                    e
                }

            assertEquals(error, caught)
        }
}
