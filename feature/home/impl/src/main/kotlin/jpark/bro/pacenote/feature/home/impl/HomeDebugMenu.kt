package jpark.bro.pacenote.feature.home.impl

import androidx.compose.runtime.Composable

/**
 * 빌드 유형별로 홈 맨 아래에 붙는 항목 (debug: 추적 테스트 화면 이동).
 * app의 debug Koin 모듈만 등록하고, release에는 없다.
 */
fun interface HomeDebugMenu {
    @Composable
    fun Content()
}
