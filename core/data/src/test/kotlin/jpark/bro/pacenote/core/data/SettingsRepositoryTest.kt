package jpark.bro.pacenote.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import jpark.bro.pacenote.core.data.repository.DefaultOnboardingRepository
import jpark.bro.pacenote.core.data.repository.DefaultSettingsRepository
import jpark.bro.pacenote.core.datastore.AppPrefsDataStore
import jpark.bro.pacenote.core.datastore.RunPrefsDataStore
import jpark.bro.pacenote.core.datastore.UserPrefsDataStore
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.TermsConsent
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun TestScope.dataStore() =
        PreferenceDataStoreFactory.create(scope = backgroundScope) { File(tempFolder.root, "prefs.preferences_pb") }

    private fun TestScope.settings() =
        dataStore().let {
            DefaultSettingsRepository(
                UserPrefsDataStore(it),
                RunPrefsDataStore(it),
                AppPrefsDataStore(it),
            )
        }

    private val river = ThemeSetting(AppThemeType.RIVER, AppThemeMode.DARK)

    @Test
    fun `미리보기는 저장한 값보다 우선하고 취소하면 되돌아감`() =
        runTest {
            val repository = settings()

            repository.previewThemeSetting(river)
            assertEquals(river, repository.observeEffectiveThemeSetting().first())

            repository.previewThemeSetting(null)
            assertEquals(ThemeSetting(), repository.observeEffectiveThemeSetting().first())
        }

    @Test
    fun `적용하면 저장하고 미리보기를 끝냄`() =
        runTest {
            val repository = settings()

            repository.previewThemeSetting(river)
            repository.applyThemeSetting(river)
            repository.previewThemeSetting(null)

            assertEquals(river, repository.observeEffectiveThemeSetting().first())
        }

    @Test
    fun `선택지에 없는 카운트다운은 실패`() =
        runTest {
            val repository = settings()

            assertTrue(repository.setRunSettings(RunSettings(countdownSeconds = 7)).isFailure)
            assertEquals(RunSettings(), repository.observeRunSettings().first())
        }

    @Test
    fun `체중과 단위를 함께 저장하고 체중 0 이하는 실패`() =
        runTest {
            val repository = settings()

            repository.setProfile(UserProfile(65.0, DistanceUnit.MILE))
            assertEquals(UserProfile(65.0, DistanceUnit.MILE), repository.observeProfile().first())

            assertTrue(repository.setProfile(UserProfile(0.0, DistanceUnit.KM)).isFailure)
            assertEquals(UserProfile(65.0, DistanceUnit.MILE), repository.observeProfile().first())
        }

    @Test
    fun `만 14세 이상에 동의하지 않은 기록은 저장하지 않음`() =
        runTest {
            val store = dataStore()
            val repository = DefaultOnboardingRepository(UserPrefsDataStore(store), AppPrefsDataStore(store))
            val consent = TermsConsent("2026-11-01", "2026-11-01", "2026-11-01", false, false, 0L, 0L)

            assertTrue(repository.saveConsent(consent).isFailure)
            assertNull(repository.observeConsent().first())
        }
}
