package jpark.bro.pacenote.feature.record.impl.detail

internal sealed interface RecordDetailEvent {
    data class OpenEdit(
        val runId: String,
    ) : RecordDetailEvent

    data class OpenShareCard(
        val runId: String,
    ) : RecordDetailEvent

    data object NavigateBack : RecordDetailEvent
}
