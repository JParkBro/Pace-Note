package jpark.bro.pacenote.feature.settings.impl.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.settings.api.GoalKey
import jpark.bro.pacenote.feature.settings.api.SettingsKey
import jpark.bro.pacenote.feature.settings.impl.goal.GoalRoot
import jpark.bro.pacenote.feature.settings.impl.goal.GoalViewModel
import jpark.bro.pacenote.feature.settings.impl.my.MyRoot
import jpark.bro.pacenote.feature.settings.impl.my.MyViewModel
import jpark.bro.pacenote.feature.settings.impl.profile.ProfileEditKey
import jpark.bro.pacenote.feature.settings.impl.profile.ProfileEditRoot
import jpark.bro.pacenote.feature.settings.impl.profile.ProfileEditViewModel
import jpark.bro.pacenote.feature.settings.impl.run.RunSettingsKey
import jpark.bro.pacenote.feature.settings.impl.run.RunSettingsRoot
import jpark.bro.pacenote.feature.settings.impl.run.RunSettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val settingsModule =
    module {
        viewModelOf(::MyViewModel)
        viewModelOf(::GoalViewModel)
        viewModelOf(::ProfileEditViewModel)
        viewModelOf(::RunSettingsViewModel)

        navigation<SettingsKey> {
            val navigator = get<Navigator>()
            MyRoot(
                onOpenGoal = { navigator.goTo(GoalKey) },
                onOpenProfile = { navigator.goTo(ProfileEditKey) },
                onOpenRunSettings = { navigator.goTo(RunSettingsKey) },
                viewModel = koinViewModel(),
            )
        }

        navigation<GoalKey> {
            GoalRoot(onClose = get<Navigator>()::goBack, viewModel = koinViewModel())
        }

        navigation<RunSettingsKey> {
            RunSettingsRoot(onBack = get<Navigator>()::goBack, viewModel = koinViewModel())
        }

        navigation<ProfileEditKey> {
            ProfileEditRoot(onClose = get<Navigator>()::goBack, viewModel = koinViewModel())
        }
    }
