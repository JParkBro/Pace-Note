package jpark.bro.pacenote.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppColors
import jpark.bro.pacenote.core.designsystem.AppShapes
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.preview.ComponentPreviews
import jpark.bro.pacenote.core.designsystem.preview.RunPreviewSurface

/** 뱃지 종류 (theme-guide.md 6장). 색만으로 구분하지 않도록 항상 문구와 함께 쓴다 (5.8) */
enum class RunBadgeTone {
    /** 강조: primary-soft */
    HIGHLIGHT,

    /** 목표 달성 · 백업 완료: success-soft */
    SUCCESS,

    /** 보조: accent-soft */
    ACCENT,

    /** 일반: fill */
    NEUTRAL,
}

enum class RunBadgeSize {
    /** 리스트 · 카드 안 (labelSm) */
    SMALL,

    /** 설정 행 끝 상태 표시처럼 조금 큰 것 (labelMd) */
    MEDIUM,
}

/** 알약 모양 뱃지. [showDot]이면 앞에 상태 점을 붙인다 */
@Composable
fun RunBadge(
    text: String,
    tone: RunBadgeTone,
    modifier: Modifier = Modifier,
    size: RunBadgeSize = RunBadgeSize.SMALL,
    showDot: Boolean = false,
) {
    val colors = AppThemeTokens.colors.badgeColors(tone)
    val typography = AppThemeTokens.typography
    Row(
        modifier =
            modifier
                .clip(AppShapes.full)
                .background(colors.background)
                .then(
                    when (size) {
                        RunBadgeSize.SMALL -> Modifier.padding(horizontal = AppSpacing.sm, vertical = 2.dp)
                        RunBadgeSize.MEDIUM -> Modifier.padding(horizontal = 12.dp, vertical = AppSpacing.xs)
                    },
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (showDot) {
            Box(
                modifier =
                    Modifier
                        .size(if (size == RunBadgeSize.SMALL) 6.dp else 8.dp)
                        .clip(AppShapes.full)
                        .background(colors.dot),
            )
        }
        Text(
            text = text,
            style = if (size == RunBadgeSize.SMALL) typography.labelSm else typography.labelMd,
            color = colors.content,
        )
    }
}

private data class BadgeColors(
    val background: Color,
    val content: Color,
    val dot: Color,
)

private fun AppColors.badgeColors(tone: RunBadgeTone): BadgeColors =
    when (tone) {
        RunBadgeTone.HIGHLIGHT -> BadgeColors(primarySoft, onPrimarySoft, primary)
        RunBadgeTone.SUCCESS -> BadgeColors(successSoft, success, success)
        RunBadgeTone.ACCENT -> BadgeColors(accentSoft, onAccentSoft, accent)
        RunBadgeTone.NEUTRAL -> BadgeColors(fill, textSecondary, textTertiary)
    }

/** P4 상단의 "일시정지됨" 알약 */
@Composable
fun RunPausedPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    RunBadge(text = text, tone = RunBadgeTone.HIGHLIGHT, modifier = modifier, size = RunBadgeSize.MEDIUM)
}

@ComponentPreviews
@Composable
private fun RunBadgePreview() {
    RunPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            RunBadge(text = "이번 주 목표 달성!", tone = RunBadgeTone.SUCCESS)
            RunBadge(text = "강조", tone = RunBadgeTone.HIGHLIGHT)
            RunBadge(text = "보조", tone = RunBadgeTone.ACCENT)
            RunBadge(text = "일반", tone = RunBadgeTone.NEUTRAL)
        }
        RunBadge(text = "백업 켜짐", tone = RunBadgeTone.SUCCESS, size = RunBadgeSize.MEDIUM, showDot = true)
        RunPausedPill(text = "일시정지됨")
    }
}
