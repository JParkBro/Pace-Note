package jpark.bro.pacenote.feature.record.impl.edit

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 기록 편집(R4). 상세(R3)에서만 연다. */
@Serializable
internal data class RunEditKey(
    val runId: String,
) : NavKey
