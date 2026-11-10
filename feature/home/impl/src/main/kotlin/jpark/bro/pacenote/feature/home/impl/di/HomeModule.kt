package jpark.bro.pacenote.feature.home.impl.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.home.impl.HomeRoot
import jpark.bro.pacenote.feature.home.impl.HomeViewModel
import jpark.bro.pacenote.feature.record.api.RunDetailKey
import jpark.bro.pacenote.feature.run.api.RunKey
import jpark.bro.pacenote.feature.settings.api.GoalKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val homeModule =
    module {
        viewModelOf(::HomeViewModel)

        navigation<HomeKey> {
            val navigator = get<Navigator>()
            HomeRoot(
                onStartRun = { navigator.goTo(RunKey()) },
                onOpenRun = { runId -> navigator.goTo(RunDetailKey(runId)) },
                onOpenGoal = { navigator.goTo(GoalKey) },
                viewModel = koinViewModel(),
                debugMenu = getOrNull(),
            )
        }
    }
