package jpark.bro.pacenote.core.data.repository

/**
 * 홈 위젯 갱신 요청 (widget/CLAUDE.md "갱신 시점"). 구현은 `:widget`의 `WidgetUpdater`.
 * 기록 저장 · 삭제, 목표 저장, 테마 · 거리 단위 변경 뒤에 부른다.
 * 갱신은 백그라운드 작업으로 넘기므로 호출한 쪽의 성공 · 실패에 영향을 주지 않는다.
 */
fun interface HomeWidgets {
    fun requestUpdate()
}
