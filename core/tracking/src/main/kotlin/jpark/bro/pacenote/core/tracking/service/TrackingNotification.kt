package jpark.bro.pacenote.core.tracking.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import jpark.bro.pacenote.core.model.DistanceUnit
import jpark.bro.pacenote.core.model.PauseState
import jpark.bro.pacenote.core.navigation.AppIntents
import jpark.bro.pacenote.core.tracking.R
import jpark.bro.pacenote.core.tracking.TrackingState
import jpark.bro.pacenote.core.ui.RunFormat
import jpark.bro.pacenote.core.designsystem.R as DesignR

/** 진행 중 알림 N1 (tracking.md 7-4). 시간은 시스템 크로노미터가 그리고, 거리 · 페이스는 서비스가 5초마다 갱신한다. */
internal class TrackingNotification(
    private val context: Context,
) {
    private val manager = NotificationManagerCompat.from(context)

    fun createChannel() {
        manager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.tracking_notification_channel))
                .setVibrationEnabled(false)
                .setSound(null, null)
                .setShowBadge(false)
                .build(),
        )
    }

    /**
     * @param movingMs 지금 시점의 운동 시간. 크로노미터 시작점(`setWhen`)을 이것으로 보정한다
     */
    fun build(
        state: TrackingState.Recording?,
        unit: DistanceUnit,
        movingMs: Long,
    ): Notification {
        val pause = state?.pause ?: PauseState.RUNNING
        val running = pause == PauseState.RUNNING
        val distance = RunFormat.distance(state?.metrics?.distanceM ?: 0.0, unit)
        val unitLabel = context.getString(RunFormat.unitLabelRes(unit))
        val builder =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(DesignR.drawable.ic_gps_fixed)
                .setContentTitle(context.getString(titleRes(pause)))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setContentIntent(openAppIntent())
                .addAction(pauseOrResumeAction(pause))
                .addAction(0, context.getString(R.string.tracking_notification_action_open), openAppIntent())
        return if (running) {
            builder
                .setUsesChronometer(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis() - movingMs)
                .setContentText(
                    context.getString(
                        R.string.tracking_notification_text_running,
                        distance,
                        unitLabel,
                        RunFormat.pace(state?.metrics?.currentPaceSecPerKm, unit),
                    ),
                ).build()
        } else {
            builder
                .setUsesChronometer(false)
                .setShowWhen(false)
                .setContentText(
                    context.getString(
                        R.string.tracking_notification_text_paused,
                        distance,
                        unitLabel,
                        RunFormat.duration(movingMs),
                    ),
                ).build()
        }
    }

    /** 알림 권한이 없으면 갱신하지 않는다. 포그라운드 서비스는 권한 없이도 계속 돈다 (tracking.md 7-4). */
    fun notify(notification: Notification) {
        // Android 12 이하는 알림 런타임 권한이 없다
        val granted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (granted && manager.areNotificationsEnabled()) manager.notify(NOTIFICATION_ID, notification)
    }

    private fun titleRes(pause: PauseState): Int =
        when (pause) {
            PauseState.RUNNING -> R.string.tracking_notification_title_running
            PauseState.AUTO_PAUSED -> R.string.tracking_notification_title_auto_paused
            PauseState.USER_PAUSED -> R.string.tracking_notification_title_paused
        }

    private fun pauseOrResumeAction(pause: PauseState): NotificationCompat.Action {
        val resume = pause == PauseState.USER_PAUSED
        val intent =
            Intent(context, RunTrackingService::class.java)
                .setAction(if (resume) RunTrackingService.ACTION_RESUME else RunTrackingService.ACTION_PAUSE)
        val pending =
            PendingIntent.getService(
                context,
                REQUEST_PAUSE_RESUME,
                intent,
                PENDING_FLAGS,
            )
        val label =
            if (resume) R.string.tracking_notification_action_resume else R.string.tracking_notification_action_pause
        return NotificationCompat.Action(0, context.getString(label), pending)
    }

    /** 앱 화면을 열 뿐 서비스를 직접 다루지 않는다 (tracking.md 7-2). app이 extra를 보고 P3 · P4로 보낸다. */
    private fun openAppIntent(): PendingIntent? {
        val launch =
            context.packageManager
                .getLaunchIntentForPackage(context.packageName)
                ?.putExtra(AppIntents.EXTRA_OPEN_RUN, true)
                ?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                ?: return null
        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN,
            launch,
            PENDING_FLAGS,
        )
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "tracking"
        private const val REQUEST_OPEN = 1
        private const val REQUEST_PAUSE_RESUME = 2
        private const val PENDING_FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    }
}
