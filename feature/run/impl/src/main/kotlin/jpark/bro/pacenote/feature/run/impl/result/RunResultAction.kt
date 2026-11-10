package jpark.bro.pacenote.feature.run.impl.result

internal sealed interface RunResultAction {
    data class MemoChanged(
        val memo: String,
    ) : RunResultAction

    /** "완료" 또는 뒤로 가기 */
    data object DoneClicked : RunResultAction

    data object ShareClicked : RunResultAction
}
