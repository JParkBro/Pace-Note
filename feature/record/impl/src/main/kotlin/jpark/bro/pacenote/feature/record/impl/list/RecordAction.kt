package jpark.bro.pacenote.feature.record.impl.list

internal sealed interface RecordAction {
    /** 빈 상태의 "달리기 시작" */
    data object StartRunClicked : RecordAction
}
