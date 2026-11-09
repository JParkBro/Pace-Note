package jpark.bro.pacenote

import android.content.Context
import jpark.bro.pacenote.di.appModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class KoinModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `appModule 의존성이 모두 연결됨`() {
        // Function1: debug 빌드의 DebugLocationSource가 받는 GPX 재생 생성 함수 (모듈 안에서 직접 만든다)
        appModule.verify(extraTypes = listOf(Context::class, Function1::class))
    }
}
