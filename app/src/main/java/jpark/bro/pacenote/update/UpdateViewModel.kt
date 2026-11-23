package jpark.bro.pacenote.update

import androidx.lifecycle.viewModelScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.requestAppUpdateInfo
import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.core.data.repository.UpdatePromptRepository
import jpark.bro.pacenote.core.tracking.RunTracker
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.core.ui.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

/**
 * Play In-App Updates (FLEXIBLE만). 컨펌 없이 업데이트 · 재시작하지 않는다 (docs/run/in-app-update.md).
 * 러닝 중(추적 상태가 Idle이 아님)에는 다이얼로그를 띄우지 않고, 끝나면 띄운다.
 * "업데이트 안내"에서 "나중에"를 누르면 그날은 다시 묻지 않고, 다음 날 새로 시작할 때 묻는다.
 * "설치 준비 완료"는 날짜와 상관없이 앱으로 돌아올 때마다 묻는다.
 */
class UpdateViewModel(
    private val appUpdateManager: AppUpdateManager,
    runTracker: RunTracker,
    private val updatePromptRepository: UpdatePromptRepository,
    private val clock: Clock,
) : MviViewModel<UpdateState, UpdateAction, UpdateEvent>(UpdateState()) {
    /** 띄울 다이얼로그. 러닝 중이면 [state]에는 None으로 나간다 */
    private val pending = MutableStateFlow<UpdateDialog>(UpdateDialog.None)

    // 백그라운드 다운로드 완료 감지. Activity가 아니라 ViewModel 생명주기에 건다
    private val installStateListener =
        InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADED) pending.value = UpdateDialog.ReadyToInstall
        }

    init {
        appUpdateManager.registerListener(installStateListener)
        combine(pending, runTracker.state) { dialog, tracking ->
            if (tracking is TrackingState.Idle) dialog else UpdateDialog.None
        }.onEach { dialog -> setState { copy(dialog = dialog) } }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        appUpdateManager.unregisterListener(installStateListener)
    }

    override fun onAction(action: UpdateAction) {
        when (action) {
            UpdateAction.AppStarted -> {
                checkUpdate(offerUpdate = true)
            }

            UpdateAction.AppResumed -> {
                checkUpdate(offerUpdate = false)
            }

            UpdateAction.UpdateConfirmed -> {
                (pending.value as? UpdateDialog.Available)?.let { sendEvent(UpdateEvent.StartUpdateFlow(it.info)) }
                pending.value = UpdateDialog.None
            }

            UpdateAction.InstallConfirmed -> {
                sendEvent(UpdateEvent.CompleteUpdate)
                pending.value = UpdateDialog.None
            }

            UpdateAction.UpdateDismissed -> {
                pending.value = UpdateDialog.None
                // 저장에 실패해도 닫는다. 그러면 다음 실행 때 다시 물을 뿐이다
                viewModelScope.launch { updatePromptRepository.setSnoozedDate(LocalDate.now(clock)) }
            }

            UpdateAction.InstallDismissed -> {
                pending.value = UpdateDialog.None
            }
        }
    }

    /**
     * 확인 실패(Play에서 설치하지 않은 빌드 등)는 조용히 넘긴다. 업데이트 확인이 앱 사용을 막으면 안 된다.
     * @param offerUpdate 새 버전이 있으면 "업데이트 안내"까지 띄울지. false면 다운로드 완료만 본다 (onResume)
     */
    private fun checkUpdate(offerUpdate: Boolean) {
        viewModelScope.launch {
            suspendRunCatching { appUpdateManager.requestAppUpdateInfo() }.onSuccess { info ->
                val canUpdate =
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && info.isFlexibleUpdateAllowed
                when {
                    info.installStatus() == InstallStatus.DOWNLOADED -> pending.value = UpdateDialog.ReadyToInstall
                    offerUpdate && canUpdate && !snoozedToday() -> pending.value = UpdateDialog.Available(info)
                }
            }
        }
    }

    private suspend fun snoozedToday(): Boolean =
        updatePromptRepository.observeSnoozedDate().first() == LocalDate.now(clock)
}
