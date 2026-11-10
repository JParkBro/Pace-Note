package jpark.bro.pacenote.widget.di

import jpark.bro.pacenote.core.data.repository.HomeWidgets
import jpark.bro.pacenote.widget.WidgetDataSource
import jpark.bro.pacenote.widget.WidgetUpdater
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val widgetModule =
    module {
        singleOf(::WidgetDataSource)
        single<HomeWidgets> { WidgetUpdater(androidContext()) }
    }
