package jpark.bro.pacenote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import jpark.bro.pacenote.core.data.repository.SettingsRepository
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.core.model.ThemeSetting
import jpark.bro.pacenote.navigation.AppNavDisplay
import jpark.bro.pacenote.navigation.AppStartRouter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val router: AppStartRouter by inject()
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                router.onLaunch()
                router.onIntent(intent)
            }
        }
        setContent {
            // 테마 시트(M5)의 미리보기도 이 값으로 바로 보인다
            val theme by settingsRepository.observeEffectiveThemeSetting().collectAsStateWithLifecycle(ThemeSetting())
            AppTheme(type = theme.type, mode = theme.mode) {
                AppNavDisplay()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        router.onIntent(intent)
    }
}
