package jpark.bro.pacenote.feature.record.impl.share

internal sealed interface ShareCardEvent {
    /** 시스템 사진 선택기 (권한 없음) */
    data object OpenPhotoPicker : ShareCardEvent

    /** 카드를 PNG로 만들어 사진 앱에 저장 */
    data object ExportToGallery : ShareCardEvent

    /** 카드를 PNG로 만들어 시스템 공유 시트로 */
    data object ExportToShareSheet : ShareCardEvent

    data object ShowSaved : ShareCardEvent

    data object ShowError : ShareCardEvent

    data object NavigateBack : ShareCardEvent
}
