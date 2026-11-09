package jpark.bro.pacenote.feature.home.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 홈(H1). 홈 화면(feature:home:impl)을 만들기 전까지는 app의 임시 홈이 이 키를 그린다. */
@Serializable
data object HomeKey : NavKey
