package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.data.mapper.toModel
import jpark.bro.pacenote.core.database.dao.RunDao
import jpark.bro.pacenote.core.model.Run
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Clock

/** 끝난 기록의 조회 · 편집 (tracking.md 2장). 러닝 중 기록 쓰기는 `core:tracking`이 맡는다. */
interface RunRepository {
    /** 현재 사용자의 최근 기록, 최신순. 목록용이라 [Run.splits]는 비어 있다. */
    fun observeRecentRuns(limit: Int): Flow<List<Run>>

    /** 스플릿을 포함한 기록 하나. 없거나 지웠으면 null */
    fun observeRun(id: String): Flow<Run?>

    /** 제목 · 메모 수정 (R4). 빈 값은 null로 저장한다. */
    suspend fun updateTitleAndMemo(
        id: String,
        title: String?,
        memo: String?,
    ): Result<Unit>

    /** soft delete. 로그인 사용자면 삭제도 백업 대상이 된다. */
    suspend fun deleteRun(id: String): Result<Unit>
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultRunRepository(
    private val runDao: RunDao,
    private val authRepository: AuthRepository,
    private val clock: Clock,
) : RunRepository {
    override fun observeRecentRuns(limit: Int): Flow<List<Run>> =
        authRepository.observeUserId().flatMapLatest { owner ->
            runDao.observeRecent(owner, limit).map { runs -> runs.map { it.toModel() } }
        }

    override fun observeRun(id: String): Flow<Run?> = runDao.observeRun(id).map { it?.toModel() }

    override suspend fun updateTitleAndMemo(
        id: String,
        title: String?,
        memo: String?,
    ): Result<Unit> =
        suspendRunCatching {
            val owner = authRepository.observeUserId().first()
            runDao.updateTitleAndMemo(
                id = id,
                title = title?.trim()?.ifEmpty { null },
                memo = memo?.trim()?.ifEmpty { null },
                updatedAt = clock.millis(),
                syncState = syncStateFor(owner),
            )
        }

    override suspend fun deleteRun(id: String): Result<Unit> =
        suspendRunCatching {
            val owner = authRepository.observeUserId().first()
            runDao.softDelete(id = id, deletedAt = clock.millis(), syncState = syncStateFor(owner))
        }
}
