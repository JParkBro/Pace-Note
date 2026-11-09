package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class FakeSettingsRepository : SettingsRepository {
    val savedTheme = MutableStateFlow(ThemeSetting())
    val themePreview = MutableStateFlow<ThemeSetting?>(null)
    val runSettings = MutableStateFlow(RunSettings())
    val batteryGuideShown = MutableStateFlow(false)
    val profile = MutableStateFlow(UserProfile(weightKg = null, distanceUnit = DistanceUnit.KM))
    var writeResult: Result<Unit> = Result.success(Unit)

    override fun observeEffectiveThemeSetting(): Flow<ThemeSetting> =
        combine(themePreview, savedTheme) { preview, saved -> preview ?: saved }

    override fun previewThemeSetting(setting: ThemeSetting?) {
        themePreview.value = setting
    }

    override suspend fun applyThemeSetting(setting: ThemeSetting): Result<Unit> =
        writeResult.onSuccess {
            savedTheme.value = setting
            themePreview.value = null
        }

    override fun observeRunSettings(): Flow<RunSettings> = runSettings

    override suspend fun setRunSettings(settings: RunSettings): Result<Unit> =
        writeResult.onSuccess { runSettings.value = settings }

    override fun observeBatteryGuideShown(): Flow<Boolean> = batteryGuideShown

    override suspend fun setBatteryGuideShown(shown: Boolean): Result<Unit> =
        writeResult.onSuccess { batteryGuideShown.value = shown }

    override fun observeProfile(): Flow<UserProfile> = profile

    override suspend fun setProfile(profile: UserProfile): Result<Unit> =
        writeResult.onSuccess { this.profile.value = profile }
}
