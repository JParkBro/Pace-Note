package jpark.bro.pacenote.feature.record.impl.di

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.record.api.RecordKey
import jpark.bro.pacenote.feature.record.impl.list.RecordRoot
import jpark.bro.pacenote.feature.record.impl.list.RecordViewModel
import jpark.bro.pacenote.feature.run.api.RunKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val recordModule =
    module {
        viewModelOf(::RecordViewModel)

        navigation<RecordKey> {
            val navigator = get<Navigator>()
            RecordRoot(onStartRun = { navigator.goTo(RunKey()) }, viewModel = koinViewModel())
        }
    }
