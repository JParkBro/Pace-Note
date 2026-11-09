package jpark.bro.pacenote.core.tracking

import android.content.Context
import jpark.bro.pacenote.core.tracking.di.trackingModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

class TrackingModuleTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `trackingModule 의존성이 모두 연결됨`() {
        trackingModule.verify(extraTypes = listOf(Context::class))
    }
}
