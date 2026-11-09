package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.data.repository.OnboardingRepository
import jpark.bro.pacenote.core.model.TermsConsent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeOnboardingRepository : OnboardingRepository {
    val consent = MutableStateFlow<TermsConsent?>(null)
    val onboardingCompleted = MutableStateFlow(false)
    var writeResult: Result<Unit> = Result.success(Unit)

    override fun observeConsent(): Flow<TermsConsent?> = consent

    override suspend fun saveConsent(consent: TermsConsent): Result<Unit> =
        writeResult.onSuccess { this.consent.value = consent }

    override fun observeOnboardingCompleted(): Flow<Boolean> = onboardingCompleted

    override suspend fun completeOnboarding(): Result<Unit> = writeResult.onSuccess { onboardingCompleted.value = true }
}
