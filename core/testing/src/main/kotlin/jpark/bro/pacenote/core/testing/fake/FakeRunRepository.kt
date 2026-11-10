package jpark.bro.pacenote.core.testing.fake

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.model.Run
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeRunRepository : RunRepository {
    val runs = MutableStateFlow<List<Run>>(emptyList())

    /** 실패를 흉내 내려면 `Result.failure(…)`로 바꾼다. */
    var writeResult: Result<Unit> = Result.success(Unit)

    override fun observeRecentRuns(limit: Int): Flow<List<Run>> =
        runs.map { list -> list.filter { it.deletedAt == null }.sortedByDescending { it.startedAt }.take(limit) }

    override fun observeRunPages(): Flow<PagingData<Run>> =
        runs.map { list ->
            // 끝까지 다 읽은 상태로 알려야 asSnapshot이 더 읽기를 기다리지 않는다
            PagingData.from(
                list.filter { it.deletedAt == null }.sortedByDescending { it.startedAt },
                LoadStates(LoadState.NotLoading(true), LoadState.NotLoading(true), LoadState.NotLoading(true)),
            )
        }

    override fun observeRun(id: String): Flow<Run?> =
        runs.map { list -> list.find { it.id == id && it.deletedAt == null } }

    override suspend fun updateTitleAndMemo(
        id: String,
        title: String?,
        memo: String?,
    ): Result<Unit> =
        writeResult.onSuccess {
            runs.update { list -> list.map { if (it.id == id) it.copy(title = title, memo = memo) else it } }
        }

    override suspend fun deleteRun(id: String): Result<Unit> =
        writeResult.onSuccess {
            runs.update { list -> list.map { if (it.id == id) it.copy(deletedAt = it.updatedAt) else it } }
        }
}
