package jpark.bro.pacenote.feature.run.impl.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.run.api.RunKey
import jpark.bro.pacenote.feature.run.api.RunResultHandler
import jpark.bro.pacenote.feature.run.impl.RunRoot
import jpark.bro.pacenote.feature.run.impl.RunViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val runModule =
    module {
        viewModel { (recoverRunId: String?) -> RunViewModel(recoverRunId, get(), get(), get()) }

        navigation<RunKey> { key ->
            val navigator = get<Navigator>()
            val resultHandler = get<RunResultHandler>()
            RunRoot(
                onClose = navigator::goBack,
                onFinished = resultHandler::handle,
                viewModel = koinViewModel { parametersOf(key.recoverRunId) },
            )
        }
    }
