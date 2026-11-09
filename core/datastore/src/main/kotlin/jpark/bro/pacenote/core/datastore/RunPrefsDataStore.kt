package jpark.bro.pacenote.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import jpark.bro.pacenote.core.model.RunSettings
import jpark.bro.pacenote.core.model.VoiceItem
import kotlinx.coroutines.flow.Flow

/** 러닝 설정(M3) · 배터리 안내 표시 여부. 기기별 설정이라 서버에 올리지 않는다. */
class RunPrefsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeRunSettings(): Flow<RunSettings> = dataStore.observe(::readRunSettings)

    suspend fun setRunSettings(settings: RunSettings) {
        dataStore.edit { prefs ->
            prefs[Keys.VOICE_ENABLED] = settings.voiceEnabled
            prefs[Keys.VOICE_INTERVAL] = settings.voiceInterval.name
            prefs[Keys.VOICE_ITEMS] = settings.voiceItems.mapTo(mutableSetOf()) { it.name }
            prefs[Keys.AUTO_PAUSE_ENABLED] = settings.autoPauseEnabled
            prefs[Keys.COUNTDOWN_SECONDS] = settings.countdownSeconds
            prefs[Keys.KEEP_SCREEN_ON] = settings.keepScreenOn
        }
    }

    /** 첫 러닝 전 배터리 최적화 안내(P1-b)를 이미 보였는지 */
    fun observeBatteryGuideShown(): Flow<Boolean> = dataStore.observe { it[Keys.BATTERY_GUIDE_SHOWN] ?: false }

    suspend fun setBatteryGuideShown(shown: Boolean) {
        dataStore.edit { it[Keys.BATTERY_GUIDE_SHOWN] = shown }
    }

    private fun readRunSettings(prefs: Preferences): RunSettings {
        val default = RunSettings()
        return RunSettings(
            voiceEnabled = prefs[Keys.VOICE_ENABLED] ?: default.voiceEnabled,
            voiceInterval = prefs[Keys.VOICE_INTERVAL].toEnum(default.voiceInterval),
            voiceItems =
                prefs[Keys.VOICE_ITEMS]
                    ?.mapNotNullTo(mutableSetOf()) { name -> VoiceItem.entries.find { it.name == name } }
                    ?: default.voiceItems,
            autoPauseEnabled = prefs[Keys.AUTO_PAUSE_ENABLED] ?: default.autoPauseEnabled,
            countdownSeconds =
                prefs[Keys.COUNTDOWN_SECONDS]?.takeIf { it in RunSettings.COUNTDOWN_OPTIONS }
                    ?: default.countdownSeconds,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: default.keepScreenOn,
        )
    }

    /** 키 이름은 저장된 값과 묶여 있으므로 바꾸지 않는다. */
    private object Keys {
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val VOICE_INTERVAL = stringPreferencesKey("voice_interval")
        val VOICE_ITEMS = stringSetPreferencesKey("voice_items")
        val AUTO_PAUSE_ENABLED = booleanPreferencesKey("auto_pause_enabled")
        val COUNTDOWN_SECONDS = intPreferencesKey("countdown_seconds")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val BATTERY_GUIDE_SHOWN = booleanPreferencesKey("battery_guide_shown")
    }
}
