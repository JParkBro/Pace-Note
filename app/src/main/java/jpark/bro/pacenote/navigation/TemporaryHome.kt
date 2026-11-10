package jpark.bro.pacenote.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.R
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews

/**
 * 홈(H1, feature:home:impl)을 만들기 전까지 [jpark.bro.pacenote.feature.home.api.HomeKey]를 그리는 자리표시자.
 * [extras]에 빌드 유형별 항목(debug: 추적 테스트 화면 이동)이 들어간다.
 */
@Composable
fun TemporaryHome(extras: @Composable ColumnScope.() -> Unit = {}) {
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.app_home_placeholder),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
            )
            extras()
        }
    }
}

@ScreenPreviews
@Composable
private fun TemporaryHomePreview() {
    AppTheme { TemporaryHome() }
}
