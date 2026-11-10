package jpark.bro.pacenote.feature.record.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 기록 상세(R3). 기록 목록 · 홈의 최근 기록에서 연다. */
@Serializable
data class RunDetailKey(
    val runId: String,
) : NavKey
