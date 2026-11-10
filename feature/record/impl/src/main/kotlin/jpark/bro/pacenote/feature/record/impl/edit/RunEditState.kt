package jpark.bro.pacenote.feature.record.impl.edit

import jpark.bro.pacenote.core.domain.stats.DefaultRunTitle

/**
 * @property title 입력 중인 제목. 비워 두면 [defaultTitle]을 보여주고 저장하지 않는다
 * @property working 저장 · 삭제 중 (버튼을 막는다)
 */
internal data class RunEditState(
    val loading: Boolean = true,
    val title: String = "",
    val memo: String = "",
    val defaultTitle: DefaultRunTitle? = null,
    val showDeleteDialog: Boolean = false,
    val working: Boolean = false,
) {
    companion object {
        const val TITLE_MAX_LENGTH = 40
        const val MEMO_MAX_LENGTH = 500
    }
}
