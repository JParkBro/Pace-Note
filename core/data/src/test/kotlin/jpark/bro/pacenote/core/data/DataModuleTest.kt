package jpark.bro.pacenote.core.data

import android.content.Context
import jpark.bro.pacenote.core.data.di.dataModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class DataModuleTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `dataModule 의존성이 모두 연결됨`() {
        dataModule.verify(extraTypes = listOf(Context::class))
    }
}
