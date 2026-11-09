package jpark.bro.pacenote.core.data.repository

import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.datastore.AppPrefsDataStore
import jpark.bro.pacenote.core.datastore.RunPrefsDataStore
import jpark.bro.pacenote.core.datastore.UserPrefsDataStore
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/** 내 정보 · 러닝 설정 · 테마 (M3 · M4 · M5). */
interface SettingsRepository {
    /** 미리보기 값이 있으면 그것, 없으면 저장한 값 (theme-guide.md). 앱 전체 테마가 이것을 구독한다. */
    fun observeEffectiveThemeSetting(): Flow<ThemeSetting>

    /** 테마 시트(M5)에서 고르는 즉시 미리보기. null이면 미리보기를 끝내고 저장한 값으로 되돌린다. */
    fun previewThemeSetting(setting: ThemeSetting?)

    /** [테마 적용하기]: 저장하고 미리보기를 끝낸다. */
    suspend fun applyThemeSetting(setting: ThemeSetting): Result<Unit>

    fun observeRunSettings(): Flow<RunSettings>

    suspend fun setRunSettings(settings: RunSettings): Result<Unit>

    /** 배터리 최적화 안내(P1-b)를 이미 보였는지. M3 "다시 보기"는 false로 되돌린다. */
    fun observeBatteryGuideShown(): Flow<Boolean>

    suspend fun setBatteryGuideShown(shown: Boolean): Result<Unit>

    fun observeProfile(): Flow<UserProfile>

    /** 체중 · 거리 단위 (O2 · M4). 체중 null이면 지운다. */
    suspend fun setProfile(profile: UserProfile): Result<Unit>
}

internal class DefaultSettingsRepository(
    private val userPrefs: UserPrefsDataStore,
    private val runPrefs: RunPrefsDataStore,
    private val appPrefs: AppPrefsDataStore,
) : SettingsRepository {
    // Repository가 single이라 화면을 옮겨 다녀도 미리보기가 유지되고, 앱을 다시 열면 사라진다
    private val themePreview = MutableStateFlow<ThemeSetting?>(null)

    override fun observeEffectiveThemeSetting(): Flow<ThemeSetting> =
        combine(themePreview, appPrefs.observeThemeSetting()) { preview, saved -> preview ?: saved }

    override fun previewThemeSetting(setting: ThemeSetting?) {
        themePreview.value = setting
    }

    override suspend fun applyThemeSetting(setting: ThemeSetting): Result<Unit> =
        suspendRunCatching { appPrefs.setThemeSetting(setting) }
            .onSuccess { themePreview.value = null }

    override fun observeRunSettings(): Flow<RunSettings> = runPrefs.observeRunSettings()

    override suspend fun setRunSettings(settings: RunSettings): Result<Unit> =
        suspendRunCatching {
            require(settings.countdownSeconds in RunSettings.COUNTDOWN_OPTIONS) { "카운트다운 선택지 밖의 값" }
            runPrefs.setRunSettings(settings)
        }

    override fun observeBatteryGuideShown(): Flow<Boolean> = runPrefs.observeBatteryGuideShown()

    override suspend fun setBatteryGuideShown(shown: Boolean): Result<Unit> =
        suspendRunCatching { runPrefs.setBatteryGuideShown(shown) }

    override fun observeProfile(): Flow<UserProfile> = userPrefs.observeProfile()

    override suspend fun setProfile(profile: UserProfile): Result<Unit> =
        suspendRunCatching {
            require(profile.weightKg.let { it == null || it > 0 }) { "체중은 0보다 커야 한다" }
            userPrefs.setWeightKg(profile.weightKg)
            userPrefs.setDistanceUnit(profile.distanceUnit)
        }
}
