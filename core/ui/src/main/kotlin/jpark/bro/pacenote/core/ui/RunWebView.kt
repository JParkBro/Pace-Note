package jpark.bro.pacenote.core.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import jpark.bro.pacenote.core.designsystem.AppThemeTokens

/**
 * 웹 페이지 한 곳을 보여주는 웹뷰 (다른 앱 둘러보기, 정책 문서).
 * 링크는 [webLinkTarget]으로 고른다: 처음 연 사이트의 https만 안에서 열고, 스토어 링크는 [onStoreLink]로 넘기고, 나머지는 막는다.
 *
 * - [onPageLoaded]: 메인 프레임을 오류 없이 다 불러왔을 때. 오류 뒤에 끝난 것은 성공으로 보지 않는다.
 * - [onPageFailed]: 메인 프레임의 네트워크 · HTTP 오류. 하위 리소스(이미지 등) 오류는 무시한다.
 * - [url] 또는 [reloadKey]가 바뀌면 같은 웹뷰로 다시 불러온다. 실패 · 로딩 중에도 컴포지션에서 빼지 않는다.
 */
@Composable
fun RunWebView(
    url: String,
    onPageLoaded: () -> Unit,
    onPageFailed: () -> Unit,
    onStoreLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    reloadKey: Int = 0,
) {
    val currentOnPageLoaded by rememberUpdatedState(onPageLoaded)
    val currentOnPageFailed by rememberUpdatedState(onPageFailed)
    val currentOnStoreLink by rememberUpdatedState(onStoreLink)
    val currentPageHost by rememberUpdatedState(Uri.parse(url).host.orEmpty())
    // 흰 화면이 번쩍이지 않게 앱 배경색으로 칠한다
    val background = AppThemeTokens.colors.bg.toArgb()
    val loaded = remember { LoadedRequest() }
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                configureSecurely()
                webViewClient =
                    RunWebViewClient(
                        pageHost = { currentPageHost },
                        onPageLoaded = { currentOnPageLoaded() },
                        onPageFailed = { currentOnPageFailed() },
                        onStoreLink = { currentOnStoreLink(it) },
                    )
            }
        },
        update = { webView ->
            webView.setBackgroundColor(background)
            if (loaded.url != url || loaded.reloadKey != reloadKey) {
                loaded.url = url
                loaded.reloadKey = reloadKey
                webView.loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
        modifier = modifier,
    )
}

// 페이지가 목록을 fetch로 그려 JavaScript가 필요하다. 링크는 webLinkTarget으로 이 사이트만 열고
// addJavascriptInterface를 쓰지 않으며 파일 · 콘텐츠 접근을 막아, 페이지 스크립트가 앱에 닿을 길이 없다.
@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureSecurely() {
    settings.javaScriptEnabled = true
    settings.allowFileAccess = false
    settings.allowContentAccess = false
}

/** 마지막으로 불러온 요청. 같은 값이면 recomposition마다 다시 불러오지 않는다 */
private class LoadedRequest(
    var url: String? = null,
    var reloadKey: Int? = null,
)

private class RunWebViewClient(
    private val pageHost: () -> String,
    private val onPageLoaded: () -> Unit,
    private val onPageFailed: () -> Unit,
    private val onStoreLink: (String) -> Unit,
) : WebViewClient() {
    private var failed = false

    override fun onPageStarted(
        view: WebView?,
        url: String?,
        favicon: Bitmap?,
    ) {
        failed = false
    }

    override fun onPageFinished(
        view: WebView?,
        url: String?,
    ) {
        if (!failed) onPageLoaded()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        if (request?.isForMainFrame == true) fail()
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?,
    ) {
        if (request?.isForMainFrame == true) fail()
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        val link = request?.url ?: return true
        return when (webLinkTarget(pageHost(), link.scheme, link.host)) {
            WebLinkTarget.IN_PAGE -> {
                false
            }

            WebLinkTarget.PLAY_STORE -> {
                onStoreLink(link.toString())
                true
            }

            WebLinkTarget.BLOCKED -> {
                true
            }
        }
    }

    private fun fail() {
        if (failed) return
        failed = true
        onPageFailed()
    }
}
