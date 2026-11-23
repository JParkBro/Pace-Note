package jpark.bro.pacenote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.core.ui.ObserveEvents
import jpark.bro.pacenote.navigation.AppNavDisplay
import jpark.bro.pacenote.navigation.AppStartRouter
import jpark.bro.pacenote.update.UpdateAction
import jpark.bro.pacenote.update.UpdateDialogs
import jpark.bro.pacenote.update.UpdateEvent
import jpark.bro.pacenote.update.UpdateViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val router: AppStartRouter by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val appUpdateManager: AppUpdateManager by inject()
    private val updateViewModel: UpdateViewModel by viewModel()

    // 결과는 쓰지 않는다. Play 화면에서 취소하면 다음 실행 때 다시 묻는다. STARTED 전에 등록해야 해서 필드에 둔다
    private val updateFlowLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                router.onLaunch()
                router.onIntent(intent)
            }
            updateViewModel.onAction(UpdateAction.AppStarted)
        }
        setContent {
            // 테마 시트(M5)의 미리보기도 이 값으로 바로 보인다
            val theme by settingsRepository.observeEffectiveThemeSetting().collectAsStateWithLifecycle(ThemeSetting())
            val updateState by updateViewModel.state.collectAsStateWithLifecycle()
            ObserveEvents(updateViewModel.event) { event ->
                when (event) {
                    is UpdateEvent.StartUpdateFlow -> {
                        appUpdateManager.startUpdateFlowForResult(
                            event.info,
                            updateFlowLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                        )
                    }

                    UpdateEvent.CompleteUpdate -> {
                        appUpdateManager.completeUpdate()
                    }
                }
            }
            AppTheme(type = theme.type, mode = theme.mode) {
                Box(Modifier.fillMaxSize()) {
                    AppNavDisplay()
                    // NavDisplay 위에 그려지도록 마지막 자식으로 둔다
                    UpdateDialogs(updateState.dialog, updateViewModel::onAction)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateViewModel.onAction(UpdateAction.AppResumed)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        router.onIntent(intent)
    }
}
