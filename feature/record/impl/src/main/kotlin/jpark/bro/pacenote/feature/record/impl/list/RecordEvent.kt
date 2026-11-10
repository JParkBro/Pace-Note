package jpark.bro.pacenote.feature.record.impl.list

internal sealed interface RecordEvent {
    data object StartRun : RecordEvent

    /** 기록 상세(R3) */
    data class OpenRun(
        val runId: String,
    ) : RecordEvent
}
