package jpark.bro.pacenote.feature.record.impl.share

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.pacenote.core.designsystem.AppSpacing
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.designsystem.component.RunFilterChip
import jpark.bro.pacenote.core.designsystem.component.RunIconButton
import jpark.bro.pacenote.core.designsystem.component.RunPrimaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSecondaryButton
import jpark.bro.pacenote.core.designsystem.component.RunSection
import jpark.bro.pacenote.core.designsystem.component.RunToastHost
import jpark.bro.pacenote.core.designsystem.component.RunTopBar
import jpark.bro.pacenote.core.designsystem.preview.ScreenPreviews
import jpark.bro.pacenote.core.model.AppThemeMode
import jpark.bro.pacenote.core.model.AppThemeType
import jpark.bro.pacenote.core.model.RoutePoint
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.feature.record.impl.R
import kotlin.math.PI
import kotlin.math.sin
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 공유 카드(P7)의 Root: 사진 선택기, 카드를 PNG로 만들어 저장 · 공유, 토스트 */
@Composable
internal fun ShareCardRoot(
    onBack: () -> Unit,
    viewModel: ShareCardViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.record_share_saved)
    val errorMessage = stringResource(R.string.record_error)
    val cardLayer = rememberGraphicsLayer()
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.onAction(ShareCardAction.PhotoPicked(uri?.toString()))
        }
    val photo by produceState<ImageBitmap?>(null, state.photoUri) {
        value = state.photoUri?.let { ShareImageExporter.loadPhoto(context, it)?.asImageBitmap() }
    }

    suspend fun export(toGallery: Boolean) {
        val fileName = "pacenote_${state.startedAt}"
        val result =
            runCatching {
                ShareImageExporter.toExportSize(
                    cardLayer.toImageBitmap().asAndroidBitmap(),
                    state.background,
                )
            }.fold(
                onSuccess = { bitmap ->
                    if (toGallery) {
                        ShareImageExporter.saveToGallery(context, bitmap, fileName)
                    } else {
                        ShareImageExporter.share(context, bitmap, fileName)
                    }
                },
                onFailure = { Result.failure(it) },
            )
        viewModel.onAction(ShareCardAction.ExportFinished(success = result.isSuccess, saved = toGallery))
    }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            ShareCardEvent.OpenPhotoPicker -> {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            ShareCardEvent.ExportToGallery -> {
                export(toGallery = true)
            }

            ShareCardEvent.ExportToShareSheet -> {
                export(toGallery = false)
            }

            ShareCardEvent.ShowSaved -> {
                snackbarHostState.showSnackbar(savedMessage)
            }

            ShareCardEvent.ShowError -> {
                snackbarHostState.showSnackbar(errorMessage)
            }

            ShareCardEvent.NavigateBack -> {
                onBack()
            }
        }
    }
    ShareCardScreen(state, photo, cardLayer, snackbarHostState, viewModel::onAction)
}

@Composable
private fun ShareCardScreen(
    state: ShareCardState,
    photo: ImageBitmap?,
    cardLayer: GraphicsLayer?,
    snackbarHostState: SnackbarHostState,
    onAction: (ShareCardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            RunTopBar(
                title = stringResource(R.string.record_share_title),
                navigationIcon = {
                    RunIconButton(
                        icon = DesignR.drawable.ic_arrow_back,
                        contentDescription = stringResource(R.string.record_back),
                        onClick = { onAction(ShareCardAction.BackClicked) },
                    )
                },
            )
        },
        snackbarHost = { RunToastHost(snackbarHostState) },
    ) { padding ->
        if (state.loading) return@Scaffold
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            val cardDescription = stringResource(R.string.record_share_card)
            ShareCard(
                state = state,
                photo = photo,
                modifier =
                    Modifier
                        .testTag("record_share_card")
                        .semantics { contentDescription = cardDescription }
                        .then(if (cardLayer != null) Modifier.recordInto(cardLayer) else Modifier),
            )
            BackgroundChips(state.background, onAction)
            FieldChips(state.fields, onAction)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.gutterCompact)) {
                RunSecondaryButton(
                    text = stringResource(R.string.record_share_save),
                    onClick = { onAction(ShareCardAction.SaveClicked) },
                    icon = DesignR.drawable.ic_download,
                    enabled = !state.working,
                    modifier = Modifier.weight(1f).testTag("record_share_save"),
                )
                RunPrimaryButton(
                    text = stringResource(R.string.record_share_share),
                    onClick = { onAction(ShareCardAction.ShareClicked) },
                    icon = DesignR.drawable.ic_share,
                    enabled = !state.working,
                    modifier = Modifier.weight(1f).testTag("record_share_share"),
                )
            }
        }
    }
}

/** 화면에 그리는 카드를 [layer]에도 기록해 두고, 저장할 때 그 레이어를 비트맵으로 뽑는다 */
private fun Modifier.recordInto(layer: GraphicsLayer): Modifier =
    drawWithContent {
        layer.record { this@drawWithContent.drawContent() }
        drawLayer(layer)
    }

@Composable
private fun BackgroundChips(
    selected: ShareBackground,
    onAction: (ShareCardAction) -> Unit,
) {
    RunSection(title = stringResource(R.string.record_share_background)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth()) {
            ShareBackground.entries.forEach { background ->
                RunFilterChip(
                    text =
                        stringResource(
                            when (background) {
                                ShareBackground.DEFAULT -> R.string.record_share_background_default
                                ShareBackground.PHOTO -> R.string.record_share_background_photo
                                ShareBackground.TRANSPARENT -> R.string.record_share_background_transparent
                            },
                        ),
                    selected = background == selected,
                    onClick = { onAction(ShareCardAction.BackgroundSelected(background)) },
                    modifier = Modifier.testTag("record_share_background_${background.name.lowercase()}"),
                )
            }
        }
    }
}

@Composable
private fun FieldChips(
    selected: Set<ShareField>,
    onAction: (ShareCardAction) -> Unit,
) {
    RunSection(title = stringResource(R.string.record_share_fields)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth()) {
            ShareField.entries.forEach { field ->
                RunFilterChip(
                    text =
                        stringResource(
                            when (field) {
                                ShareField.DISTANCE -> R.string.record_share_field_distance
                                ShareField.TIME -> R.string.record_share_field_time
                                ShareField.PACE -> R.string.record_share_field_pace
                                ShareField.DATE -> R.string.record_share_field_date
                            },
                        ),
                    selected = field in selected,
                    onClick = { onAction(ShareCardAction.FieldToggled(field)) },
                    role = androidx.compose.ui.semantics.Role.Checkbox,
                    modifier = Modifier.testTag("record_share_field_${field.name.lowercase()}"),
                )
            }
        }
    }
}

// 미리보기: 샘플 시나리오(product-spec 9장) 10.28 (수) 오전 6:40, 5.21km
private const val PREVIEW_ROUTE_POINTS = 40

@Suppress("MagicNumber")
private val PREVIEW_STATE =
    ShareCardState(
        loading = false,
        startedAt = 1_793_137_200_000L,
        zoneId = "Asia/Seoul",
        distanceM = 5_210.0,
        movingMs = 1_812_000L,
        averagePaceSecPerKm = 347.8,
        route =
            List(PREVIEW_ROUTE_POINTS) { i ->
                val t = i / (PREVIEW_ROUTE_POINTS - 1.0)
                RoutePoint(37.52 + 0.004 * sin(t * 2 * PI), 126.93 + 0.02 * t, 0)
            },
    )

@Composable
private fun Preview(
    state: ShareCardState,
    themeType: AppThemeType = AppThemeType.VOLT,
    mode: AppThemeMode = AppThemeMode.SYSTEM,
) {
    AppTheme(type = themeType, mode = mode) { ShareCardScreen(state, null, null, remember { SnackbarHostState() }, {}) }
}

@ScreenPreviews
@Composable
private fun ShareCardPreview() = Preview(PREVIEW_STATE)

@ScreenPreviews
@Composable
private fun ShareCardInkDarkPreview() = Preview(PREVIEW_STATE, AppThemeType.INK, AppThemeMode.DARK)

@ScreenPreviews
@Composable
private fun ShareCardTransparentPreview() =
    Preview(PREVIEW_STATE.copy(background = ShareBackground.TRANSPARENT, fields = ShareField.entries.toSet()))
