package jpark.bro.pacenote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** 임시 테마. core:designsystem의 AppTheme(docs/run/theme-guide.md)이 생기면 교체한다. */
@Composable
fun PacenoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = Typography,
        content = content,
    )
}
