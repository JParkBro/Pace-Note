package jpark.bro.pacenote.core.data.di

import jpark.bro.pacenote.core.common.DeviceClock
import jpark.bro.pacenote.core.common.IdGenerator
import jpark.bro.pacenote.core.common.UuidIdGenerator
import jpark.bro.pacenote.core.data.repository.AuthRepository
import jpark.bro.pacenote.core.data.repository.DefaultGoalRepository
import jpark.bro.pacenote.core.data.repository.DefaultOnboardingRepository
import jpark.bro.pacenote.core.data.repository.DefaultRunRepository
import jpark.bro.pacenote.core.data.repository.DefaultSettingsRepository
import jpark.bro.pacenote.core.data.repository.DefaultStatsRepository
import jpark.bro.pacenote.core.data.repository.GoalRepository
import jpark.bro.pacenote.core.data.repository.GuestAuthRepository
import jpark.bro.pacenote.core.data.repository.OnboardingRepository
import jpark.bro.pacenote.core.data.repository.RunRepository
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.data.repository.StatsRepository
import jpark.bro.pacenote.core.database.di.databaseModule
import jpark.bro.pacenote.core.datastore.di.datastoreModule
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.time.Clock

val dataModule =
    module {
        includes(databaseModule, datastoreModule)

        single<Clock> { DeviceClock }
        single<IdGenerator> { UuidIdGenerator }

        singleOf(::GuestAuthRepository) { bind<AuthRepository>() }
        singleOf(::DefaultRunRepository) { bind<RunRepository>() }
        singleOf(::DefaultStatsRepository) { bind<StatsRepository>() }
        singleOf(::DefaultGoalRepository) { bind<GoalRepository>() }
        singleOf(::DefaultSettingsRepository) { bind<SettingsRepository>() }
        singleOf(::DefaultOnboardingRepository) { bind<OnboardingRepository>() }
    }
