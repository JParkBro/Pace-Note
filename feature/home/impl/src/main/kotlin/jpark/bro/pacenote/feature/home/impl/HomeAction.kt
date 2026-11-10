package jpark.bro.pacenote.feature.home.impl

internal sealed interface HomeAction {
    data object StartRunClicked : HomeAction

    data class RunClicked(
        val runId: String,
    ) : HomeAction
}
