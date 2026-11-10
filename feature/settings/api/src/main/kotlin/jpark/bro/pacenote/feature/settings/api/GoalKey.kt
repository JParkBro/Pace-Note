package jpark.bro.pacenote.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 목표 (M2). 내 정보와 홈의 "목표 정하기"에서 연다. */
@Serializable
data object GoalKey : NavKey
