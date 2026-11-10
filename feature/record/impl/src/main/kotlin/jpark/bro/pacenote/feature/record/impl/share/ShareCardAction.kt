package jpark.bro.pacenote.feature.record.impl.share

internal sealed interface ShareCardAction {
    data class BackgroundSelected(
        val background: ShareBackground,
    ) : ShareCardAction

    /** 사진 선택기 결과. 고르지 않았으면 null */
    data class PhotoPicked(
        val uri: String?,
    ) : ShareCardAction

    data class FieldToggled(
        val field: ShareField,
    ) : ShareCardAction

    data object SaveClicked : ShareCardAction

    data object ShareClicked : ShareCardAction

    /** Root가 이미지를 저장 · 공유한 결과 */
    data class ExportFinished(
        val success: Boolean,
        val saved: Boolean,
    ) : ShareCardAction

    data object BackClicked : ShareCardAction
}
