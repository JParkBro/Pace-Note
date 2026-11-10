package jpark.bro.pacenote.feature.record.impl.detail

internal sealed interface RecordDetailAction {
    data object EditClicked : RecordDetailAction

    data object ShareClicked : RecordDetailAction

    data object BackClicked : RecordDetailAction
}
