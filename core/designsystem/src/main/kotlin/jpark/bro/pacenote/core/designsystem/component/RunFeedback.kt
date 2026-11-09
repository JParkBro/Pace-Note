package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/** 안내 박스. 색만으로 구분하지 않도록 아이콘을 함께 보여준다 (theme-guide.md 5.8) */
@Composable
fun RunInfoBox(
    text: String,
    modifier: Modifier = Modifier,
    tone: RunInfoBoxTone = RunInfoBoxTone.TIP,
) {
    val colors = AppThemeTokens.colors
    val (background, content, icon) =
        when (tone) {
            RunInfoBoxTone.TIP -> Triple(colors.fill, colors.textSecondary, R.drawable.ic_info)
            RunInfoBoxTone.WARNING -> Triple(colors.warningSoft, colors.warning, R.drawable.ic_warning)
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.md)
                .background(background)
                .padding(AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = content,
            modifier =
                Modifier
                    .padding(top = 2.dp)
                    .size(16.dp),
        )
        Text(text = text, style = AppThemeTokens.typography.bodySm, color = content, modifier = Modifier.weight(1f))
    }
}

/** 토스트: text-primary 배경 + bg 글자 + shadow */
@Composable
fun RunToast(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    Box(
        modifier =
            modifier
                .padding(AppSpacing.marginMobile)
                .shadow(elevation = 8.dp, shape = AppShapes.md, ambientColor = colors.shadow, spotColor = colors.shadow)
                .clip(AppShapes.md)
                .background(colors.textPrimary)
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, style = AppThemeTokens.typography.bodyMd, color = colors.bg)
    }
}

/** 스낵바 자리에 [RunToast]를 띄운다. 화면 Root에서 SnackbarHost 대신 쓴다 */
@Composable
fun RunToastHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data -> RunToast(message = data.visuals.message) }
}

@ComponentPreviews
@Composable
private fun RunFeedbackPreview() {
    RunPreviewSurface {
        RunInfoBox(text = "위젯은 홈 화면을 길게 눌러 추가할 수 있어요.")
        RunInfoBox(
            text = "연결을 해제하면 '우리 둘이' 항목은 더 이상 함께 볼 수 없어요.",
            tone = RunInfoBoxTone.WARNING,
        )
        RunToast(message = "저장했어요")
    }
}
