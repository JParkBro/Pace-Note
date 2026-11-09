package jpark.bro.pacenote

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import jpark.bro.pacenote.core.designsystem.AppTheme
import jpark.bro.pacenote.navigation.AppNavDisplay
import jpark.bro.pacenote.navigation.AppStartRouter
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val router: AppStartRouter by inject()

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
            AppTheme {
                AppNavDisplay()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        router.onIntent(intent)
    }
}
