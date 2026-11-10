package jpark.bro.pacenote.feature.run.impl.result

internal sealed interface RunResultEvent {
    /** 러닝 흐름을 끝낸다 (app이 홈으로) */
    data object Finish : RunResultEvent

    data object ShowError : RunResultEvent
}
