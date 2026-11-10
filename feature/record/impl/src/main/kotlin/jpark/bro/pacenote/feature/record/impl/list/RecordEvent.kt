package jpark.bro.pacenote.feature.record.impl.list

internal sealed interface RecordEvent {
    data object StartRun : RecordEvent
}
