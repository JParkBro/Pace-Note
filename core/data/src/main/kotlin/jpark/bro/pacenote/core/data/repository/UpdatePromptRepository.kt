package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.datastore.AppPrefsDataStore
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** 인앱 업데이트 안내를 미룬 날 (docs/run/in-app-update.md). 기기 단위 값이라 서버에 올리지 않는다. */
interface UpdatePromptRepository {
    /** "업데이트 안내"에서 마지막으로 "나중에"를 누른 날. 그날은 다시 묻지 않는다. 없으면 null */
    fun observeSnoozedDate(): Flow<LocalDate?>

    suspend fun setSnoozedDate(date: LocalDate): Result<Unit>
}

internal class DefaultUpdatePromptRepository(
    private val appPrefs: AppPrefsDataStore,
) : UpdatePromptRepository {
    override fun observeSnoozedDate(): Flow<LocalDate?> = appPrefs.observeUpdateSnoozedDate()

    override suspend fun setSnoozedDate(date: LocalDate): Result<Unit> =
        suspendRunCatching { appPrefs.setUpdateSnoozedDate(date) }
}
