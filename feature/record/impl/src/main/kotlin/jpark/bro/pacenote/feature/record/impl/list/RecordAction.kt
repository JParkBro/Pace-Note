package jpark.bro.pacenote.feature.record.impl.list

import jpark.bro.pacenote.core.domain.stats.StatsPeriod

internal sealed interface RecordAction {
    /** 화면이 다시 보일 때마다 Root가 보낸다. 그 사이 날짜가 바뀌었으면 오늘 · 이번 기간을 다시 잡는다 */
    data object ScreenStarted : RecordAction

    data class TabSelected(
        val tab: RecordTab,
    ) : RecordAction

    /** 통계(R2)의 주 / 월 칩 */
    data class PeriodSelected(
        val period: StatsPeriod,
    ) : RecordAction

    data class RunClicked(
        val runId: String,
    ) : RecordAction

    data object PreviousPeriodClicked : RecordAction

    data object NextPeriodClicked : RecordAction

    /** 빈 상태의 "달리기 시작" */
    data object StartRunClicked : RecordAction
}
