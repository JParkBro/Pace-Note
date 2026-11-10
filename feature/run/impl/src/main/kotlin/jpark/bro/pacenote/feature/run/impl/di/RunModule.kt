package jpark.bro.pacenote.feature.run.impl.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.record.api.ShareCardKey
import jpark.bro.pacenote.feature.run.api.RunKey
import jpark.bro.pacenote.feature.run.api.RunResultHandler
import jpark.bro.pacenote.feature.run.impl.RunRoot
import jpark.bro.pacenote.feature.run.impl.RunViewModel
import jpark.bro.pacenote.feature.run.impl.result.RunResultKey
import jpark.bro.pacenote.feature.run.impl.result.RunResultRoot
import jpark.bro.pacenote.feature.run.impl.result.RunResultViewModel
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
        viewModel { (runId: String) -> RunResultViewModel(runId, get(), get(), get(), get()) }

        navigation<RunKey> { key ->
            val navigator = get<Navigator>()
            RunRoot(
                onClose = navigator::goBack,
                // 러닝 화면을 결과로 바꿔, 결과에서 뒤로 가도 러닝 화면으로 돌아가지 않게
                onOpenResult = { runId -> navigator.replaceTop(RunResultKey(runId)) },
                viewModel = koinViewModel { parametersOf(key.recoverRunId) },
            )
        }

        navigation<RunResultKey> { key ->
            val navigator = get<Navigator>()
            val resultHandler = get<RunResultHandler>()
            RunResultRoot(
                onFinished = resultHandler::handle,
                onOpenShareCard = { runId -> navigator.goTo(ShareCardKey(runId)) },
                viewModel = koinViewModel { parametersOf(key.runId) },
            )
        }
    }
