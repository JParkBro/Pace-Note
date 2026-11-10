package jpark.bro.pacenote.feature.settings.impl.my

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.pacenote.feature.settings.impl.R

/** 문의 메일 (product-spec 7장): 받는 주소 · 제목에 앱 버전 · 기기 모델 · Android 버전을 붙인다. 위치 · 기록은 넣지 않는다 */
@Composable
internal fun contactIntent(versionName: String): Intent {
    val email = stringResource(R.string.settings_contact_email)
    val subject = stringResource(R.string.settings_contact_subject)
    val body =
        stringResource(
            R.string.settings_contact_body,
            versionName,
            "${Build.MANUFACTURER} ${Build.MODEL}",
            Build.VERSION.RELEASE,
        )
    return Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
}
