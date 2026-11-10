package jpark.bro.pacenote.feature.record.impl.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.record.api.RecordKey
import jpark.bro.pacenote.feature.record.api.RunDetailKey
import jpark.bro.pacenote.feature.record.impl.detail.RecordDetailRoot
import jpark.bro.pacenote.feature.record.impl.detail.RecordDetailViewModel
import jpark.bro.pacenote.feature.record.impl.edit.RunEditKey
import jpark.bro.pacenote.feature.record.impl.edit.RunEditRoot
import jpark.bro.pacenote.feature.record.impl.edit.RunEditViewModel
import jpark.bro.pacenote.feature.record.impl.list.RecordRoot
import jpark.bro.pacenote.feature.record.impl.list.RecordViewModel
import jpark.bro.pacenote.feature.run.api.RunKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val recordModule =
    module {
        viewModelOf(::RecordViewModel)
        viewModel { (runId: String) -> RecordDetailViewModel(runId, get(), get()) }
        viewModel { (runId: String) -> RunEditViewModel(runId, get()) }

        navigation<RecordKey> {
            val navigator = get<Navigator>()
            RecordRoot(
                onStartRun = { navigator.goTo(RunKey()) },
                onOpenRun = { runId -> navigator.goTo(RunDetailKey(runId)) },
                viewModel = koinViewModel(),
            )
        }

        navigation<RunDetailKey> { key ->
            val navigator = get<Navigator>()
            RecordDetailRoot(
                onBack = navigator::goBack,
                onEdit = { runId -> navigator.goTo(RunEditKey(runId)) },
                viewModel = koinViewModel { parametersOf(key.runId) },
            )
        }

        navigation<RunEditKey> { key ->
            val navigator = get<Navigator>()
            RunEditRoot(
                onClose = navigator::goBack,
                // 편집과 상세를 함께 닫아 상세를 연 화면(목록 · 홈)으로 돌아간다
                onDeleted = {
                    navigator.goBack()
                    navigator.goBack()
                },
                viewModel = koinViewModel { parametersOf(key.runId) },
            )
        }
    }
