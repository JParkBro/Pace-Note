package jpark.bro.pacenote.navigation

import jpark.bro.pacenote.core.navigation.Navigator
import jpark.bro.pacenote.feature.home.api.HomeKey
import jpark.bro.pacenote.feature.run.api.RunResult
import jpark.bro.pacenote.feature.run.api.RunResultHandler

/** 러닝 흐름이 끝난 뒤 목적지 (module-map 4-3). */
class AppRunResultHandler(
    private val navigator: Navigator,
) : RunResultHandler {
    override fun handle(result: RunResult) =
        when (result) {
            RunResult.Finished -> navigator.replaceAll(HomeKey)
        }
}
