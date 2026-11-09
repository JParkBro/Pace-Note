package jpark.bro.pacenote

import android.app.Application
import jpark.bro.pacenote.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PacenoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PacenoteApplication)
            modules(appModule)
        }
    }
}
