package jpark.bro.pacenote.core.navigation

/** 앱 밖(진행 알림 · 위젯)에서 앱을 열 때 붙이는 extra. 받는 쪽(app)이 백스택을 만든다. */
object AppIntents {
    /** 진행 알림 탭: 러닝 중 화면(P3 · P4)으로 */
    const val EXTRA_OPEN_RUN = "jpark.bro.pacenote.extra.OPEN_RUN"

    /** 위젯 "달리기 시작": 준비 화면(P1)부터 */
    const val EXTRA_START_RUN = "jpark.bro.pacenote.extra.START_RUN"
}
