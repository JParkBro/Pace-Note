package jpark.bro.pacenote.core.ui

/** [RunWebView] 안에서 누른 링크를 어디로 보낼지 */
enum class WebLinkTarget {
    /** 처음 연 사이트의 https 링크: 웹뷰 안에서 연다 */
    IN_PAGE,

    /** Play 스토어 링크: 화면이 스토어 앱으로 넘긴다 */
    PLAY_STORE,

    /** 그 밖의 모든 링크(http, 다른 사이트, `intent:`, `javascript:` 등): 열지 않는다 */
    BLOCKED,
}

/**
 * 링크 정책. [pageHost]는 웹뷰가 처음 연 URL의 host.
 * 웹뷰는 이 사이트만 보여주고, 스토어 링크만 밖으로 넘기며, 나머지는 막는다.
 */
fun webLinkTarget(
    pageHost: String,
    linkScheme: String?,
    linkHost: String?,
): WebLinkTarget =
    when {
        linkScheme == "https" && linkHost == pageHost -> WebLinkTarget.IN_PAGE
        linkScheme == "https" && linkHost == PLAY_STORE_HOST -> WebLinkTarget.PLAY_STORE
        linkScheme == "market" -> WebLinkTarget.PLAY_STORE
        else -> WebLinkTarget.BLOCKED
    }

private const val PLAY_STORE_HOST = "play.google.com"
