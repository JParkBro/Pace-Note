package jpark.bro.pacenote.core.datastore.di

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import jpark.bro.pacenote.core.datastore.AppPrefsDataStore
import jpark.bro.pacenote.core.datastore.RunPrefsDataStore
import jpark.bro.pacenote.core.datastore.UserPrefsDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val datastoreModule =
    module {
        // 파일이 깨지면 설정을 기본값으로 되돌린다. 기록 원본(Room)과 무관한 설정만 들어 있다.
        single {
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                produceFile = { androidContext().preferencesDataStoreFile("user_prefs") },
            )
        }
        singleOf(::UserPrefsDataStore)
        singleOf(::RunPrefsDataStore)
        singleOf(::AppPrefsDataStore)
    }
