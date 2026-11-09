package jpark.bro.pacenote.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import jpark.bro.pacenote.core.model.ThemeSetting
import kotlinx.coroutines.flow.Flow

/** 앱 상태: 테마 · 모드, 온보딩 완료. 서버에 올리지 않는다. */
class AppPrefsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeThemeSetting(): Flow<ThemeSetting> =
        dataStore.observe { prefs ->
            val default = ThemeSetting()
            ThemeSetting(
                type = prefs[Keys.THEME_TYPE].toEnum(default.type),
                mode = prefs[Keys.THEME_MODE].toEnum(default.mode),
            )
        }

    suspend fun setThemeSetting(setting: ThemeSetting) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_TYPE] = setting.type.name
            prefs[Keys.THEME_MODE] = setting.mode.name
        }
    }

    fun observeOnboardingCompleted(): Flow<Boolean> = dataStore.observe { it[Keys.ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted() {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    /** 키 이름은 저장된 값과 묶여 있으므로 바꾸지 않는다. */
    private object Keys {
        val THEME_TYPE = stringPreferencesKey("theme_type")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
