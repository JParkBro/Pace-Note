package jpark.bro.pacenote.update

import com.google.android.play.core.appupdate.AppUpdateInfo

/** Play 호출은 Activity가 필요해서 MainActivity가 받아 실행한다 */
sealed interface UpdateEvent {
    /** `startUpdateFlowForResult(FLEXIBLE)` */
    data class StartUpdateFlow(
        val info: AppUpdateInfo,
    ) : UpdateEvent

    /** `completeUpdate()`: 앱이 재시작되며 설치된다 */
    data object CompleteUpdate : UpdateEvent
}
