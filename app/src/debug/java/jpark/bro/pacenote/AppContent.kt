package jpark.bro.pacenote

import androidx.compose.runtime.Composable
import jpark.bro.pacenote.debug.TrackingDebugScreen

/** debug 빌드: 첫 화면(feature:home)이 생기기 전까지 추적 테스트 화면을 띄운다 (tracking.md 8-3). */
@Composable
fun AppContent() {
    TrackingDebugScreen()
}
