package jpark.bro.pacenote

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/** 첫 화면(feature:home)이 생기기 전 자리표시자 */
@Composable
fun AppContent() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Text(text = stringResource(R.string.app_name), modifier = Modifier.padding(innerPadding))
    }
}
