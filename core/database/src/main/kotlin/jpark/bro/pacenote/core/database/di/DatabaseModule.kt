package jpark.bro.pacenote.core.database.di

import androidx.room.Room
import jpark.bro.pacenote.core.database.PacenoteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule =
    module {
        single {
            Room.databaseBuilder(androidContext(), PacenoteDatabase::class.java, "pacenote.db").build()
        }
        single { get<PacenoteDatabase>().runDao() }
        single { get<PacenoteDatabase>().runTrackingDao() }
        single { get<PacenoteDatabase>().runRecoveryDao() }
        single { get<PacenoteDatabase>().goalDao() }
        single { get<PacenoteDatabase>().syncCursorDao() }
    }
