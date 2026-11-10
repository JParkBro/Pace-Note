package jpark.bro.pacenote.core.tracking

import android.content.Context
import jpark.bro.pacenote.core.data.repository.HomeWidgets
import jpark.bro.pacenote.core.tracking.di.trackingModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class TrackingModuleTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `trackingModule 의존성이 모두 연결됨`() {
        // HomeWidgets는 :widget이 제공한다 (app의 KoinModulesTest에서 함께 확인)
        trackingModule.verify(extraTypes = listOf(Context::class, HomeWidgets::class))
    }
}
