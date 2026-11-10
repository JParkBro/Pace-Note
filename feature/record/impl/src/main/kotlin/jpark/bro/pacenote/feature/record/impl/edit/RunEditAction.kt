package jpark.bro.pacenote.feature.record.impl.edit

internal sealed interface RunEditAction {
    data class TitleChanged(
        val title: String,
    ) : RunEditAction

    data class MemoChanged(
        val memo: String,
    ) : RunEditAction

    data object SaveClicked : RunEditAction

    data object DeleteClicked : RunEditAction

    data object DeleteConfirmed : RunEditAction

    data object DeleteDismissed : RunEditAction

    data object BackClicked : RunEditAction
}
