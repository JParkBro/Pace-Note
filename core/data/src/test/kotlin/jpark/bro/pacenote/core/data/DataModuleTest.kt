package jpark.bro.pacenote.core.data

import android.content.Context
import jpark.bro.pacenote.core.data.di.dataModule
import jpark.bro.pacenote.core.data.repository.HomeWidgets
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class DataModuleTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `dataModule 의존성이 모두 연결됨`() {
        // HomeWidgets는 :widget이 제공한다 (app의 KoinModulesTest에서 함께 확인)
        dataModule.verify(extraTypes = listOf(Context::class, HomeWidgets::class))
    }
}
