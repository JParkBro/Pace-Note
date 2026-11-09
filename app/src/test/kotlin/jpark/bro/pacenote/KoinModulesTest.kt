package jpark.bro.pacenote

import android.content.Context
import androidx.navigation3.runtime.NavKey
import jpark.bro.pacenote.di.appModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class KoinModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `appModule 의존성이 모두 연결됨`() {
        // 모듈 안에서 직접 넘기는 값: Function1(debug DebugLocationSource의 GPX 재생 생성 함수), NavKey(Navigator 시작 화면)
        appModule.verify(extraTypes = listOf(Context::class, Function1::class, NavKey::class))
    }
}
