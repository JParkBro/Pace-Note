package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface
import jpark.bro.pacenote.core.model.GpsStatus

/** GPS 상태. 색만으로 구분하지 않도록 아이콘과 문구를 함께 보여준다 (theme-guide.md 5.8). */
@Composable
fun RunGpsIndicator(
    status: GpsStatus,
    modifier: Modifier = Modifier,
) {
    val colors = AppThemeTokens.colors
    val (color, text) =
        when (status) {
            GpsStatus.GOOD -> colors.success to R.string.common_gps_good
            GpsStatus.WEAK -> colors.warning to R.string.common_gps_weak
            GpsStatus.LOST -> colors.textTertiary to R.string.common_gps_lost
        }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_gps_fixed),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Text(text = stringResource(text), style = AppThemeTokens.typography.labelMd, color = color)
    }
}

@ComponentPreviews
@Composable
private fun RunGpsIndicatorPreview() {
    RunPreviewSurface {
        GpsStatus.entries.forEach { RunGpsIndicator(status = it) }
    }
}
