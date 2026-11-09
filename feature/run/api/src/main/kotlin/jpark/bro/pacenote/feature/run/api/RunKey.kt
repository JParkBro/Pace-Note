package jpark.bro.pacenote.feature.run.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * 러닝 흐름(P1 ~ P5, P8). 단계는 화면 하나 안의 상태다 (module-map 4-2).
 *
 * @property recoverRunId null이면 새 러닝, 값이 있으면 그 기록의 복구 다이얼로그(P8)부터
 */
@Serializable
data class RunKey(
    val recoverRunId: String? = null,
) : NavKey
