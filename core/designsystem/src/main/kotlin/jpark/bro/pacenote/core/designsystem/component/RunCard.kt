package jpark.bro.pacenote.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.R
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/** 일반 카드: surface + border, 모서리 lg. [onClick]이 있으면 카드 전체를 누를 수 있다 */
@Composable
fun RunCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AppThemeTokens.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.lg)
                .background(colors.surface)
                .border(1.dp, colors.border, AppShapes.lg)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content,
    )
}

/** 섹션 제목 + 카드 묶음 (설정 메뉴 그룹 등). 카드 안 행 사이에는 [RunDivider] */
@Composable
fun RunSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = title,
            style = AppThemeTokens.typography.labelMd,
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.padding(horizontal = AppSpacing.xs),
        )
        RunCard(content = content)
    }
}

/**
 * 카드 · 리스트 안의 한 행: [leading] · 제목 · 설명 · [trailing] · 화살표.
 * [onClick]이 있으면 기본으로 오른쪽 화살표를 보여준다. [emphasized]면 제목을 굵게 (예: 개인정보 처리방침).
 */
@Composable
fun RunListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    emphasized: Boolean = false,
) {
    val colors = AppThemeTokens.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style =
                    AppThemeTokens.typography.labelLg.let {
                        if (emphasized) it.copy(fontWeight = FontWeight.Bold) else it
                    },
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(text = subtitle, style = AppThemeTokens.typography.bodySm, color = colors.textSecondary)
            }
        }
        trailing?.invoke(this)
        if (showChevron) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** 구분선 (border). 카드 안 행 사이는 [inset]으로 좌우를 띄운다 */
@Composable
fun RunDivider(
    modifier: Modifier = Modifier,
    inset: Dp = 0.dp,
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = inset),
        color = AppThemeTokens.colors.border,
    )
}

@ComponentPreviews
@Composable
private fun RunCardPreview() {
    RunPreviewSurface {
        RunCard(onClick = {}) {
            RunListItem(title = "5.21 km", subtitle = "10월 28일 (수) · 30:12 · 5′48″", onClick = {})
            RunDivider(inset = AppSpacing.marginMobile)
            RunListItem(title = "3.21 km", subtitle = "10월 27일 (화) · 18:23 · 5′44″", onClick = {})
        }
        RunSection(title = "정보") {
            RunListItem(title = "개인정보 처리방침", onClick = {}, emphasized = true)
            RunDivider(inset = AppSpacing.marginMobile)
            RunListItem(
                title = "목표",
                trailing = { RunBadge(text = "주 15km", tone = RunBadgeTone.NEUTRAL) },
            )
        }
    }
}
