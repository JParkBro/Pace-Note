package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.data.repository.UpdatePromptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class FakeUpdatePromptRepository : UpdatePromptRepository {
    val snoozedDate = MutableStateFlow<LocalDate?>(null)
    var writeResult: Result<Unit> = Result.success(Unit)

    override fun observeSnoozedDate(): Flow<LocalDate?> = snoozedDate

    override suspend fun setSnoozedDate(date: LocalDate): Result<Unit> =
        writeResult.onSuccess {
            snoozedDate.value =
                date
        }
}
