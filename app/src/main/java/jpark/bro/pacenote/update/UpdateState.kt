package jpark.bro.pacenote.update

import com.google.android.play.core.appupdate.AppUpdateInfo

/** 인앱 업데이트 다이얼로그 상태 (docs/run/in-app-update.md 2장) */
data class UpdateState(
    val dialog: UpdateDialog = UpdateDialog.None,
)

/** 다이얼로그 두 개가 동시에 뜨지 않도록 하나의 sealed 타입으로 둔다 */
sealed interface UpdateDialog {
    data object None : UpdateDialog

    /** "업데이트 안내" */
    data class Available(
        val info: AppUpdateInfo,
    ) : UpdateDialog

    /** 다운로드 끝, "설치 준비 완료" */
    data object ReadyToInstall : UpdateDialog
}
