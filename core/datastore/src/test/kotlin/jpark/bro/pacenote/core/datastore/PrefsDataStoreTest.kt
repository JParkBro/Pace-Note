package jpark.bro.pacenote.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.TermsConsent
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.model.UserProfile
import jpark.bro.pacenote.core.model.VoiceInterval
import jpark.bro.pacenote.core.model.VoiceItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PrefsDataStoreTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val file by lazy { File(tempFolder.root, "user_prefs.preferences_pb") }

    private fun TestScope.dataStore() = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }

    private fun TestScope.prefs() = UserPrefsDataStore(dataStore())

    private class Stores(
        val user: UserPrefsDataStore,
        val run: RunPrefsDataStore,
        val app: AppPrefsDataStore,
    )

    private fun TestScope.stores() =
        dataStore().let {
            Stores(UserPrefsDataStore(it), RunPrefsDataStore(it), AppPrefsDataStore(it))
        }

    // 2026.10.29 (목) 오전 9:00 KST
    private val agreedAt = 1_793_232_000_000L

    @Test
    fun `저장한 적 없으면 기본값`() =
        runTest {
            val s = stores()

            assertEquals(UserProfile(weightKg = null, distanceUnit = DistanceUnit.KM), s.user.observeProfile().first())
            assertNull(s.user.observeConsent().first())
            assertEquals(RunSettings(), s.run.observeRunSettings().first())
            assertFalse(s.run.observeBatteryGuideShown().first())
            assertEquals(ThemeSetting(AppThemeType.VOLT, AppThemeMode.SYSTEM), s.app.observeThemeSetting().first())
            assertFalse(s.app.observeOnboardingCompleted().first())
        }

    @Test
    fun `여러 저장소가 한 파일을 써도 서로 값을 덮어쓰지 않음`() =
        runTest {
            val s = stores()

            s.user.setDistanceUnit(DistanceUnit.MILE)
            s.run.setRunSettings(RunSettings(keepScreenOn = true))
            s.app.setThemeSetting(ThemeSetting(AppThemeType.INK, AppThemeMode.LIGHT))

            assertEquals(
                DistanceUnit.MILE,
                s.user
                    .observeProfile()
                    .first()
                    .distanceUnit,
            )
            assertTrue(
                s.run
                    .observeRunSettings()
                    .first()
                    .keepScreenOn,
            )
            assertEquals(
                AppThemeType.INK,
                s.app
                    .observeThemeSetting()
                    .first()
                    .type,
            )
        }

    @Test
    fun `러닝 설정 기본값은 음성 켬 1km 거리 평균 페이스 자동 일시정지 켬 3초 화면 켜짐 끔`() {
        val settings = RunSettings()

        assertTrue(settings.voiceEnabled)
        assertEquals(VoiceInterval.KM_1, settings.voiceInterval)
        assertEquals(setOf(VoiceItem.DISTANCE, VoiceItem.AVERAGE_PACE), settings.voiceItems)
        assertTrue(settings.autoPauseEnabled)
        assertEquals(3, settings.countdownSeconds)
        assertFalse(settings.keepScreenOn)
    }

    @Test
    fun `체중과 단위를 저장하고 체중 null이면 지움`() =
        runTest {
            val prefs = prefs()

            prefs.setWeightKg(65.0)
            prefs.setDistanceUnit(DistanceUnit.MILE)
            assertEquals(UserProfile(65.0, DistanceUnit.MILE), prefs.observeProfile().first())

            prefs.setWeightKg(null)
            assertNull(prefs.observeProfile().first().weightKg)
        }

    @Test
    fun `테마와 모드를 저장`() =
        runTest {
            val prefs = AppPrefsDataStore(dataStore())

            prefs.setThemeSetting(ThemeSetting(AppThemeType.RIVER, AppThemeMode.DARK))

            assertEquals(ThemeSetting(AppThemeType.RIVER, AppThemeMode.DARK), prefs.observeThemeSetting().first())
        }

    @Test
    fun `러닝 설정 전체를 저장`() =
        runTest {
            val prefs = RunPrefsDataStore(dataStore())
            val settings =
                RunSettings(
                    voiceEnabled = false,
                    voiceInterval = VoiceInterval.MINUTES_5,
                    voiceItems = setOf(VoiceItem.TIME, VoiceItem.SPLIT_PACE),
                    autoPauseEnabled = false,
                    countdownSeconds = 10,
                    keepScreenOn = true,
                )

            prefs.setRunSettings(settings)

            assertEquals(settings, prefs.observeRunSettings().first())
        }

    @Test
    fun `음성 항목을 모두 끄면 빈 집합으로 저장`() =
        runTest {
            val prefs = RunPrefsDataStore(dataStore())

            prefs.setRunSettings(RunSettings(voiceItems = emptySet()))

            assertEquals(emptySet<VoiceItem>(), prefs.observeRunSettings().first().voiceItems)
        }

    @Test
    fun `모르는 enum 이름과 선택지에 없는 카운트다운은 기본값으로 읽음`() =
        runTest {
            val store = dataStore()
            store.edit {
                it[stringPreferencesKey("theme_type")] = "NEON"
                it[stringPreferencesKey("distance_unit")] = "YARD"
                it[stringPreferencesKey("voice_interval")] = "KM_3"
                it[stringSetPreferencesKey("voice_items")] = setOf("DISTANCE", "HEART_RATE")
                it[intPreferencesKey("countdown_seconds")] = 7
            }
            assertEquals(AppThemeType.VOLT, AppPrefsDataStore(store).observeThemeSetting().first().type)
            assertEquals(DistanceUnit.KM, UserPrefsDataStore(store).observeProfile().first().distanceUnit)
            val run = RunPrefsDataStore(store).observeRunSettings().first()
            assertEquals(VoiceInterval.KM_1, run.voiceInterval)
            assertEquals(setOf(VoiceItem.DISTANCE), run.voiceItems)
            assertEquals(3, run.countdownSeconds)
        }

    @Test
    fun `약관 동의를 저장`() =
        runTest {
            val prefs = prefs()
            val consent =
                TermsConsent(
                    termsVersion = "2026-11-01",
                    privacyVersion = "2026-11-01",
                    locationTermsVersion = "2026-11-01",
                    ageOver14 = true,
                    marketing = false,
                    agreedAt = agreedAt,
                    marketingUpdatedAt = agreedAt,
                )

            prefs.setConsent(consent)

            assertEquals(consent, prefs.observeConsent().first())
        }

    @Test
    fun `온보딩 완료와 배터리 안내 표시를 저장`() =
        runTest {
            val s = stores()

            s.app.setOnboardingCompleted()
            s.run.setBatteryGuideShown(true)

            assertTrue(s.app.observeOnboardingCompleted().first())
            assertTrue(s.run.observeBatteryGuideShown().first())
        }

    @Test
    fun `앱을 다시 열어도 저장한 값을 읽음`() {
        // 첫 runTest가 끝나면 backgroundScope와 함께 첫 DataStore도 닫힌다
        runTest { prefs().setDistanceUnit(DistanceUnit.MILE) }

        runTest { assertEquals(DistanceUnit.MILE, prefs().observeProfile().first().distanceUnit) }
    }
}
