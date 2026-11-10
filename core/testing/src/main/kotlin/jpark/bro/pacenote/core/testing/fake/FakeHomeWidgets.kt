package jpark.bro.pacenote.core.testing.fake

import jpark.bro.pacenote.core.data.repository.HomeWidgets

/** 위젯 갱신 요청 횟수만 센다. */
class FakeHomeWidgets : HomeWidgets {
    var requestCount = 0
        private set

    override fun requestUpdate() {
        requestCount++
    }
}
