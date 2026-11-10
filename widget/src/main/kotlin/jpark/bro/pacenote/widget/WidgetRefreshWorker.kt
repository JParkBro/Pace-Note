package jpark.bro.pacenote.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import jpark.bro.pacenote.core.data.repository.HomeWidgets
import jpark.bro.pacenote.core.domain.goal.WeeklyProgress
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * 모든 위젯을 다시 그리고, 다음 주 월요일 00:00에 다시 실행되도록 예약한다 (widget/CLAUDE.md "갱신 시점").
 * 앱 프로세스가 없어도 WorkManager가 실행한다.
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        allWidgets.forEach { it.updateAll(applicationContext) }
        scheduleNextWeek(applicationContext)
        return Result.success()
    }

    companion object {
        private const val NOW_WORK = "widget_refresh_now"
        private const val WEEKLY_WORK = "widget_refresh_weekly"

        /** [now]부터 다음 주 월요일 00:00(같은 시간대)까지. 월요일 00:00 정각이면 일주일 뒤 */
        internal fun delayToNextWeek(now: ZonedDateTime): Duration {
            val nextMonday = WeeklyProgress.weekStart(now.toLocalDate()).plusWeeks(1).atStartOfDay(now.zone)
            return Duration.between(now, nextMonday)
        }

        /** 지금 다시 그린다. 짧은 시간에 여러 번 불려도 하나만 남긴다 */
        fun refreshNow(context: Context) {
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    NOW_WORK,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build(),
                )
        }

        /** 기기 시간대 기준 다음 월요일 00:00에 다시 그린다 */
        fun scheduleNextWeek(
            context: Context,
            clock: Clock = Clock.systemDefaultZone(),
        ) {
            val delay = delayToNextWeek(ZonedDateTime.now(clock)).toMillis()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    WEEKLY_WORK,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                        .setInitialDelay(
                            delay,
                            TimeUnit.MILLISECONDS,
                        ).build(),
                )
        }
    }
}

/** `HomeWidgets` 구현: 기록 · 목표 · 테마가 바뀌면 위젯을 다시 그린다 */
internal class WidgetUpdater(
    private val context: Context,
) : HomeWidgets {
    override fun requestUpdate() = WidgetRefreshWorker.refreshNow(context)
}

/** 시각 · 시간대 변경: 이번 주가 달라질 수 있어 다시 그리고 월요일 예약을 다시 잡는다 */
class TimeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        WidgetRefreshWorker.refreshNow(context)
    }
}
