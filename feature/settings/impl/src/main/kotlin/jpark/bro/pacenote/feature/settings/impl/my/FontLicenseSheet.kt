package jpark.bro.pacenote.feature.settings.impl.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunBottomSheet
import jpark.bro.pacenote.feature.settings.impl.R
import jpark.bro.pacenote.core.designsystem.R as DesignR

private val TEXT_MAX_HEIGHT = 480.dp

/** Pretendard 글꼴 라이선스(SIL OFL 1.1) 원문 시트 (theme-guide.md, product-spec 7장) */
@Composable
internal fun FontLicenseSheet(onAction: (MyAction) -> Unit) {
    val context = LocalContext.current
    val license =
        remember {
            context.resources
                .openRawResource(DesignR.raw.pretendard_ofl)
                .bufferedReader()
                .use { it.readText() }
        }
    RunBottomSheet(onDismissRequest = { onAction(MyAction.FontLicenseSheetDismissed) }) {
        Column(
            modifier = Modifier.fillMaxWidth().testTag("settings_font_license_sheet"),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Text(
                text = stringResource(R.string.settings_my_font_license),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
            )
            Text(
                text = license,
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
                modifier = Modifier.heightIn(max = TEXT_MAX_HEIGHT).verticalScroll(rememberScrollState()),
            )
        }
    }
}
