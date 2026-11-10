package jpark.bro.pacenote.feature.record.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 공유 카드(P7). 결과(P6) · 상세(R3)에서 연다. */
@Serializable
data class ShareCardKey(
    val runId: String,
) : NavKey
