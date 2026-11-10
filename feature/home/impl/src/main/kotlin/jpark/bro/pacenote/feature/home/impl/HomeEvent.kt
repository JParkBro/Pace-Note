package jpark.bro.pacenote.feature.home.impl

internal sealed interface HomeEvent {
    /** 러닝 준비(P1)부터 */
    data object StartRun : HomeEvent
}
