package jpark.bro.pacenote.feature.run.impl.result

internal sealed interface RunResultEvent {
    /** 러닝 흐름을 끝낸다 (app이 홈으로) */
    data object Finish : RunResultEvent

    data object ShowError : RunResultEvent

    /** 공유 카드(P7). 남은 메모를 저장한 뒤 연다 */
    data class OpenShareCard(
        val runId: String,
    ) : RunResultEvent
}
