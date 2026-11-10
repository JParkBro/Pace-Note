package jpark.bro.pacenote.feature.record.impl.edit

internal sealed interface RunEditEvent {
    /** 저장했거나 그냥 닫는다 → 상세로 */
    data object Close : RunEditEvent

    /** 삭제했다 → 상세까지 닫는다 */
    data object Deleted : RunEditEvent

    data object ShowError : RunEditEvent
}
