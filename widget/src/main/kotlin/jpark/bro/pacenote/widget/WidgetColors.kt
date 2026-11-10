package jpark.bro.pacenote.widget

import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import jpark.bro.pacenote.core.designsystem.AppColors
import jpark.bro.pacenote.core.designsystem.palette
import jpark.bro.pacenote.core.model.AppThemeType

/** 앱 테마 팔레트를 Glance 색으로 옮긴다. 라이트 · 다크는 기기 설정을 따른다 (widget/CLAUDE.md). */
internal class WidgetColors(
    type: AppThemeType,
) {
    private val light = palette(type, dark = false)
    private val dark = palette(type, dark = true)

    private fun pick(token: AppColors.() -> androidx.compose.ui.graphics.Color): ColorProvider =
        ColorProvider(day = light.token(), night = dark.token())

    val surface = pick { surface }
    val textPrimary = pick { textPrimary }
    val textSecondary = pick { textSecondary }
    val primary = pick { primary }
    val onPrimary = pick { onPrimary }
    val track = pick { fill }
}
