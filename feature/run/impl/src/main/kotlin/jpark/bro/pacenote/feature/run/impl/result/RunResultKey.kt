package jpark.bro.pacenote.feature.run.impl.result

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 결과(P6). 러닝 흐름 안에서만 연다 (module-map 4-2). */
@Serializable
internal data class RunResultKey(
    val runId: String,
) : NavKey
