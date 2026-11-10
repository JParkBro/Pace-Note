package jpark.bro.pacenote.feature.home.impl

internal sealed interface HomeEvent {
    /** 러닝 준비(P1)부터 */
    data object StartRun : HomeEvent

    /** 기록 상세(R3) */
    data class OpenRun(
        val runId: String,
    ) : HomeEvent
}
