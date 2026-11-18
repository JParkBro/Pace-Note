package jpark.bro.pacenote.feature.settings.impl.moreapps

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.AppThemeTokens
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunSecondaryButton
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.core.ui.RunWebView
import jpark.bro.pacenote.feature.settings.impl.BuildConfig
import jpark.bro.pacenote.feature.settings.impl.R
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 다른 앱 둘러보기(M11)의 Root: ViewModel 연결, 페이지 주소, Event 처리 */
@Composable
internal fun MoreAppsRoot(
    onBack: () -> Unit,
    viewModel: MoreAppsViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // 테마가 바뀌면 주소가 바뀌고, 웹뷰가 새 주소로 다시 불러온다
    val dark = AppThemeTokens.isDark
    val url = remember(dark) { moreAppsUrl(BuildConfig.RELEASE_APPLICATION_ID, dark) }
    BackHandler { viewModel.onAction(MoreAppsAction.BackClicked) }
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is MoreAppsEvent.OpenStore -> context.openStoreLink(event.url)
            MoreAppsEvent.NavigateBack -> onBack()
        }
    }
    MoreAppsScreen(
        state = state,
        onAction = viewModel::onAction,
        page = { modifier ->
            RunWebView(
                url = url,
                onPageLoaded = { viewModel.onAction(MoreAppsAction.PageLoaded) },
                onPageFailed = { viewModel.onAction(MoreAppsAction.PageFailed) },
                onStoreLink = { viewModel.onAction(MoreAppsAction.StoreLinkClicked(it)) },
                modifier = modifier.testTag("more_apps_page"),
                reloadKey = state.reloadCount,
            )
        },
    )
}

/** [page]: 웹뷰 자리. 로딩 · 실패 중에도 빼지 않고 투명하게 둔다 (다시 시도 때 같은 웹뷰로 다시 불러오기 위해) */
@Composable
private fun MoreAppsScreen(
    state: MoreAppsState,
    onAction: (MoreAppsAction) -> Unit,
    page: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = stringResource(R.string.settings_more_apps_title),
                navigationIcon = {
                    RunIconButton(
                        icon = DesignR.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.settings_back),
                        onClick = { onAction(MoreAppsAction.BackClicked) },
                    )
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val pageVisible = !state.isLoading && !state.isFailed
            page(Modifier.fillMaxSize().alpha(if (pageVisible) 1f else 0f))
            when {
                state.isFailed -> FailedContent(onRetry = { onAction(MoreAppsAction.RetryClicked) })
                state.isLoading -> CircularProgressIndicator(color = AppThemeTokens.colors.primary)
            }
        }
    }
}

@Composable
private fun FailedContent(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            text = stringResource(R.string.settings_more_apps_failed),
            style = AppThemeTokens.typography.bodyMd,
            color = AppThemeTokens.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        RunSecondaryButton(
            text = stringResource(R.string.settings_more_apps_retry),
            onClick = onRetry,
            modifier = Modifier.testTag("more_apps_retry"),
        )
    }
}

@Composable
private fun Preview(
    state: MoreAppsState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { MoreAppsScreen(state = state, onAction = {}, page = {}) }
}

@ScreenPreviews
@Composable
private fun MoreAppsLoadingPreview() = Preview(MoreAppsState())

@ScreenPreviews
@Composable
private fun MoreAppsFailedPreview() = Preview(MoreAppsState(isLoading = false, isFailed = true))
