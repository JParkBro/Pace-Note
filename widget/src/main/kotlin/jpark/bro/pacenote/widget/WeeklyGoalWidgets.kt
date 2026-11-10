package jpark.bro.pacenote.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** 위젯 진입점: Koin은 Android 진입점에서만 꺼낸다 (CLAUDE.md) */
private abstract class WeeklyWidgetBase :
    GlanceAppWidget(),
    KoinComponent {
    private val dataSource: WidgetDataSource by inject()

    @Composable
    protected abstract fun Content(data: WeeklyWidgetData)

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val data = dataSource.load()
        provideContent { Content(data) }
    }
}

/** 주간 목표 4×2 */
private class WeeklyGoalWidget : WeeklyWidgetBase() {
    @Composable
    override fun Content(data: WeeklyWidgetData) = WeeklyGoalContent(data)
}

/** 주간 목표 2×2 */
private class WeeklyGoalSmallWidget : WeeklyWidgetBase() {
    @Composable
    override fun Content(data: WeeklyWidgetData) = WeeklyGoalSmallContent(data)
}

internal val allWidgets: List<GlanceAppWidget> get() = listOf(WeeklyGoalWidget(), WeeklyGoalSmallWidget())

class WeeklyGoalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyGoalWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshWorker.scheduleNextWeek(context)
    }
}

class WeeklyGoalSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyGoalSmallWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshWorker.scheduleNextWeek(context)
    }
}
