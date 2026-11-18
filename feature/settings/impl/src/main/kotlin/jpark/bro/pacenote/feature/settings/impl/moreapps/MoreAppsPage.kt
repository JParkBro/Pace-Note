package jpark.bro.pacenote.feature.settings.impl.moreapps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

private const val MORE_APPS_URL = "https://jparkbro-apps.jparkbro.workers.dev/"
private const val PLAY_STORE_PACKAGE = "com.android.vending"

/**
 * 소개 페이지 주소. 앱 데이터는 보내지 않고 아래 두 값만 붙인다.
 * - `from`: 이 앱의 출시 패키지 이름. 페이지가 목록에서 이 앱을 빼고 유입 경로로 쓴다.
 * - `theme`: 지금 앱 테마의 `light` / `dark`.
 */
internal fun moreAppsUrl(
    releasePackageName: String,
    dark: Boolean,
): String = "$MORE_APPS_URL?from=$releasePackageName&theme=${if (dark) "dark" else "light"}"

/** Play 스토어 앱으로 연다. 스토어 앱이 없으면 패키지 없이 다시 열어 브라우저로 넘긴다 */
internal fun Context.openStoreLink(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    val opened = runCatching { startActivity(Intent(intent).setPackage(PLAY_STORE_PACKAGE)) }
    if (opened.exceptionOrNull() is ActivityNotFoundException) {
        // market: 링크는 스토어 앱 없이 열 곳이 없을 수 있다. 그때는 아무것도 하지 않는다
        runCatching { startActivity(intent) }
    }
}
