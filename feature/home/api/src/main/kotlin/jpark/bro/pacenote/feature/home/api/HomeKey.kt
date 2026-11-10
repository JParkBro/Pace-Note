package jpark.bro.pacenote.feature.home.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 홈(H1). 앱의 시작 화면이자 러닝 결과 "완료" 뒤의 목적지 */
@Serializable
data object HomeKey : NavKey
