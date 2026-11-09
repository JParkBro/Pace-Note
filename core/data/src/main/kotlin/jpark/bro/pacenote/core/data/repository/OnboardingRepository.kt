package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.datastore.AppPrefsDataStore
import jpark.bro.pacenote.core.datastore.UserPrefsDataStore
import jpark.bro.pacenote.core.model.TermsConsent
import kotlinx.coroutines.flow.Flow

/** 약관 동의(O1)와 온보딩 완료. 앱 시작 목적지 결정에도 쓴다 (module-map 4-3). */
interface OnboardingRepository {
    /** 동의하기 전이면 null */
    fun observeConsent(): Flow<TermsConsent?>

    /** 기기에 저장한다. 로그인하면 `core:sync`가 `users/{uid}`에 올린다. */
    suspend fun saveConsent(consent: TermsConsent): Result<Unit>

    fun observeOnboardingCompleted(): Flow<Boolean>

    suspend fun completeOnboarding(): Result<Unit>
}

internal class DefaultOnboardingRepository(
    private val userPrefs: UserPrefsDataStore,
    private val appPrefs: AppPrefsDataStore,
) : OnboardingRepository {
    override fun observeConsent(): Flow<TermsConsent?> = userPrefs.observeConsent()

    override suspend fun saveConsent(consent: TermsConsent): Result<Unit> =
        suspendRunCatching {
            require(consent.ageOver14) { "필수 항목(만 14세 이상)에 동의하지 않음" }
            userPrefs.setConsent(consent)
        }

    override fun observeOnboardingCompleted(): Flow<Boolean> = appPrefs.observeOnboardingCompleted()

    override suspend fun completeOnboarding(): Result<Unit> = suspendRunCatching { appPrefs.setOnboardingCompleted() }
}
