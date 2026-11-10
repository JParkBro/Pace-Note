package jpark.bro.pacenote.feature.record.impl.list

import jpark.bro.pacenote.core.domain.stats.StatsPeriod

internal sealed interface RecordAction {
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
