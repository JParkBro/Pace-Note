package jpark.bro.pacenote.update

import android.app.Activity
import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import jpark.bro.pacenote.core.testing.fake.FakeClock
import jpark.bro.pacenote.core.testing.fake.FakeRunTracker
import jpark.bro.pacenote.core.testing.fake.FakeUpdatePromptRepository
import jpark.bro.pacenote.core.testing.rule.MainDispatcherRule
import jpark.bro.pacenote.core.tracking.TrackingState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration
import java.time.LocalDate

// FakeAppUpdateManager가 Context를 받아 Robolectric으로 돌린다. 앱 Application(Koin 시작)은 띄우지 않는다
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class UpdateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appUpdateManager = FakeAppUpdateManager(ApplicationProvider.getApplicationContext())
    private val tracker = FakeRunTracker()
    private val updatePrompt = FakeUpdatePromptRepository()
    private val clock = FakeClock() // 2026.10.29 (목) 09:00
    private val viewModel = newViewModel()

    // 앱을 새로 연 것처럼 ViewModel을 새로 만든다
    private fun newViewModel() = UpdateViewModel(appUpdateManager, tracker, updatePrompt, clock)

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun start(vm: UpdateViewModel = viewModel) {
        vm.onAction(UpdateAction.AppStarted)
        idle()
    }

    private fun downloadUpdate() {
        appUpdateManager.setUpdateAvailable(3, AppUpdateType.FLEXIBLE)
        start()
        viewModel.onAction(UpdateAction.UpdateConfirmed)
        // 실제로는 Activity가 startUpdateFlowForResult로 띄우는 Play 화면
        appUpdateManager.startUpdateFlow(
            appUpdateManager.appUpdateInfo.result,
            Activity(),
            AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
        )
        appUpdateManager.userAcceptsUpdate()
        appUpdateManager.downloadStarts()
        appUpdateManager.downloadCompletes()
        idle()
    }

    @Test
    fun `Play에 업데이트가 있으면 업데이트 안내`() {
        appUpdateManager.setUpdateAvailable(3, AppUpdateType.FLEXIBLE)

        start()

        assertTrue(viewModel.state.value.dialog is UpdateDialog.Available)
    }

    @Test
    fun `업데이트가 없으면 아무것도 띄우지 않음`() {
        appUpdateManager.setUpdateNotAvailable()

        start()

        assertEquals(UpdateDialog.None, viewModel.state.value.dialog)
    }

    @Test
    fun `업데이트를 누르면 다이얼로그를 닫고 FLEXIBLE 흐름 시작 Event`() =
        runTest {
            appUpdateManager.setUpdateAvailable(3, AppUpdateType.FLEXIBLE)
            start()

            viewModel.onAction(UpdateAction.UpdateConfirmed)

            assertEquals(UpdateDialog.None, viewModel.state.value.dialog)
            assertTrue(viewModel.event.first() is UpdateEvent.StartUpdateFlow)
        }

    @Test
    fun `나중에를 누르면 닫고 오늘 날짜를 저장`() {
        appUpdateManager.setUpdateAvailable(3, AppUpdateType.FLEXIBLE)
        start()

        viewModel.onAction(UpdateAction.UpdateDismissed)

        assertEquals(UpdateDialog.None, viewModel.state.value.dialog)
        assertEquals(LocalDate.of(2026, 10, 29), updatePrompt.snoozedDate.value)
    }

    @Test
    fun `나중에를 누른 날에는 앱을 새로 열어도 묻지 않고 다음 날 새로 열면 물음`() {
        appUpdateManager.setUpdateAvailable(3, AppUpdateType.FLEXIBLE)
        updatePrompt.snoozedDate.value = LocalDate.of(2026, 10, 29)

        val sameDay = newViewModel()
        start(sameDay)
        assertEquals(UpdateDialog.None, sameDay.state.value.dialog)

        clock.now = clock.now.plus(Duration.ofDays(1))
        val nextDay = newViewModel()
        start(nextDay)
        assertTrue(nextDay.state.value.dialog is UpdateDialog.Available)
    }

    @Test
    fun `설치 준비 완료에서 나중에를 눌러도 앱으로 돌아올 때마다 다시 물음`() {
        downloadUpdate()
        viewModel.onAction(UpdateAction.InstallDismissed)
        assertEquals(UpdateDialog.None, viewModel.state.value.dialog)

        viewModel.onAction(UpdateAction.AppResumed)
        idle()

        assertEquals(UpdateDialog.ReadyToInstall, viewModel.state.value.dialog)
    }

    @Test
    fun `업데이트 안내를 미룬 날이어도 다운로드가 끝나 있으면 설치 준비 완료를 물음`() {
        downloadUpdate()
        updatePrompt.snoozedDate.value = LocalDate.of(2026, 10, 29)

        val reopened = newViewModel()
        start(reopened)

        assertEquals(UpdateDialog.ReadyToInstall, reopened.state.value.dialog)
    }

    @Test
    fun `다운로드가 끝나면 설치 준비 완료, 재시작을 누르면 설치 Event`() =
        runTest {
            downloadUpdate()
            viewModel.event.first() // StartUpdateFlow

            assertEquals(UpdateDialog.ReadyToInstall, viewModel.state.value.dialog)

            viewModel.onAction(UpdateAction.InstallConfirmed)

            assertEquals(UpdateDialog.None, viewModel.state.value.dialog)
            assertEquals(UpdateEvent.CompleteUpdate, viewModel.event.first())
        }

    @Test
    fun `러닝 중에는 띄우지 않고 끝나면 띄움`() {
        tracker.state.value = FakeRunTracker.recording()
        appUpdateManager.setUpdateAvailable(3, AppUpdateType.FLEXIBLE)

        start()
        assertEquals(UpdateDialog.None, viewModel.state.value.dialog)

        tracker.state.value = TrackingState.Idle
        assertTrue(viewModel.state.value.dialog is UpdateDialog.Available)
    }
}
