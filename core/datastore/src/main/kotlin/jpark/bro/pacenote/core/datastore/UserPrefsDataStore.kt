package jpark.bro.pacenote.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.TermsConsent
import jpark.bro.pacenote.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** 로그인하면 `users/{uid}`에 올리는 값: 체중 · 거리 단위, 약관 동의 (data-model-erd.md 3장). */
class UserPrefsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    fun observeProfile(): Flow<UserProfile> =
        dataStore.observe { prefs ->
            UserProfile(
                weightKg = prefs[Keys.WEIGHT_KG],
                distanceUnit = prefs[Keys.DISTANCE_UNIT].toEnum(DistanceUnit.KM),
            )
        }

    /** [weightKg]가 null이면 체중을 지운다 (칼로리 표시 안 함). */
    suspend fun setWeightKg(weightKg: Double?) {
        dataStore.edit { prefs ->
            if (weightKg == null) prefs.remove(Keys.WEIGHT_KG) else prefs[Keys.WEIGHT_KG] = weightKg
        }
    }

    suspend fun setDistanceUnit(unit: DistanceUnit) {
        dataStore.edit { it[Keys.DISTANCE_UNIT] = unit.name }
    }

    /** 동의하기 전이면 null */
    fun observeConsent(): Flow<TermsConsent?> = dataStore.observe(::readConsent)

    suspend fun setConsent(consent: TermsConsent) {
        dataStore.edit { prefs ->
            prefs[Keys.CONSENT_TERMS_VERSION] = consent.termsVersion
            prefs[Keys.CONSENT_PRIVACY_VERSION] = consent.privacyVersion
            prefs[Keys.CONSENT_LOCATION_TERMS_VERSION] = consent.locationTermsVersion
            prefs[Keys.CONSENT_AGE_OVER_14] = consent.ageOver14
            prefs[Keys.CONSENT_MARKETING] = consent.marketing
            prefs[Keys.CONSENT_AGREED_AT] = consent.agreedAt
            prefs[Keys.CONSENT_MARKETING_UPDATED_AT] = consent.marketingUpdatedAt
        }
    }

    /** [setConsent]가 모든 키를 한 번에 쓰므로 동의 시각이 있으면 동의한 것으로 본다. */
    private fun readConsent(prefs: Preferences): TermsConsent? {
        val agreedAt = prefs[Keys.CONSENT_AGREED_AT] ?: return null
        return TermsConsent(
            termsVersion = prefs[Keys.CONSENT_TERMS_VERSION].orEmpty(),
            privacyVersion = prefs[Keys.CONSENT_PRIVACY_VERSION].orEmpty(),
            locationTermsVersion = prefs[Keys.CONSENT_LOCATION_TERMS_VERSION].orEmpty(),
            ageOver14 = prefs[Keys.CONSENT_AGE_OVER_14] ?: false,
            marketing = prefs[Keys.CONSENT_MARKETING] ?: false,
            agreedAt = agreedAt,
            marketingUpdatedAt = prefs[Keys.CONSENT_MARKETING_UPDATED_AT] ?: agreedAt,
        )
    }

    /** 키 이름은 저장된 값과 묶여 있으므로 바꾸지 않는다. */
    private object Keys {
        val WEIGHT_KG = doublePreferencesKey("weight_kg")
        val DISTANCE_UNIT = stringPreferencesKey("distance_unit")

        val CONSENT_TERMS_VERSION = stringPreferencesKey("consent_terms_version")
        val CONSENT_PRIVACY_VERSION = stringPreferencesKey("consent_privacy_version")
        val CONSENT_LOCATION_TERMS_VERSION = stringPreferencesKey("consent_location_terms_version")
        val CONSENT_AGE_OVER_14 = booleanPreferencesKey("consent_age_over_14")
        val CONSENT_MARKETING = booleanPreferencesKey("consent_marketing")
        val CONSENT_AGREED_AT = longPreferencesKey("consent_agreed_at")
        val CONSENT_MARKETING_UPDATED_AT = longPreferencesKey("consent_marketing_updated_at")
    }
}
