package jpark.bro.pacenote.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import jpark.bro.pacenote.core.model.GoalType
import jpark.bro.pacenote.core.navigation.AppIntents
import jpark.bro.pacenote.core.ui.RunFormat
import kotlin.math.roundToInt

private const val PERCENT = 100

/** 주간 목표 4×2: "이번 주", `12.40 / 15 km`, 진행바, 달리기 시작 버튼. 버튼은 P1부터, 나머지는 홈 */
@Composable
internal fun WeeklyGoalContent(data: WeeklyWidgetData) {
    val context = LocalContext.current
    val colors = WidgetColors(data.themeType)
    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(colors.surface)
                .cornerRadius(24.dp)
                .padding(16.dp)
                .clickable(openApp(context, startRun = false)),
    ) {
        Text(
            text = context.getString(R.string.widget_this_week),
            style = TextStyle(color = colors.textSecondary, fontSize = 13.sp),
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = RunFormat.distance(data.distanceM, data.distanceUnit),
                style = TextStyle(color = colors.textPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold),
            )
            goalText(context, data)?.let {
                Text(
                    text = " $it",
                    style = TextStyle(color = colors.textSecondary, fontSize = 15.sp),
                    modifier = GlanceModifier.padding(bottom = 4.dp),
                )
            }
        }
        if (data.goalType != null) {
            Spacer(GlanceModifier.height(8.dp))
            LinearProgressIndicator(
                progress = data.ratio.toFloat().coerceIn(0f, 1f),
                modifier =
                    GlanceModifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .semantics { contentDescription = progressDescription(context, data) },
                color = colors.primary,
                backgroundColor = colors.track,
            )
        }
        Spacer(GlanceModifier.defaultWeight())
        Box(
            modifier =
                GlanceModifier
                    .background(colors.primary)
                    .cornerRadius(20.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(openApp(context, startRun = true)),
        ) {
            Text(
                text = context.getString(R.string.widget_start_run),
                style = TextStyle(color = colors.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
}

/** 주간 목표 2×2: 진행 링 + 이번 주 거리. 탭하면 홈 */
@Composable
internal fun WeeklyGoalSmallContent(data: WeeklyWidgetData) {
    val context = LocalContext.current
    val colors = WidgetColors(data.themeType)
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(colors.surface)
                .cornerRadius(24.dp)
                .padding(12.dp)
                .clickable(openApp(context, startRun = false)),
        contentAlignment = Alignment.Center,
    ) {
        if (data.goalType != null) {
            Image(
                provider = ImageProvider(ProgressRing.track()),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colors.track),
                modifier = GlanceModifier.size(110.dp),
            )
            Image(
                provider = ImageProvider(ProgressRing.progress(data.ratio)),
                contentDescription = progressDescription(context, data),
                colorFilter = ColorFilter.tint(colors.primary),
                modifier = GlanceModifier.size(110.dp),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = RunFormat.distance(data.distanceM, data.distanceUnit),
                style = TextStyle(color = colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = goalText(context, data) ?: context.getString(RunFormat.unitLabelRes(data.distanceUnit)),
                style = TextStyle(color = colors.textSecondary, fontSize = 12.sp),
            )
        }
    }
}

/** "/ 15 km" · "/ 3회". 목표가 없으면 null */
private fun goalText(
    context: Context,
    data: WeeklyWidgetData,
): String? {
    val value = data.goalValue ?: return null
    return when (data.goalType) {
        GoalType.DISTANCE -> {
            context.getString(
                R.string.widget_goal_distance,
                (value / RunFormat.unitMeters(data.distanceUnit)).roundToInt().toString(),
                context.getString(RunFormat.unitLabelRes(data.distanceUnit)),
            )
        }

        GoalType.COUNT -> {
            context.getString(R.string.widget_goal_count, value.roundToInt())
        }

        null -> {
            null
        }
    }
}

private fun progressDescription(
    context: Context,
    data: WeeklyWidgetData,
): String = "${context.getString(R.string.widget_progress)} ${(data.ratio * PERCENT).roundToInt()}%"

/**
 * 앱을 연다. 백스택은 app이 만든다 (`AppStartRouter`). 위젯이 추적 서비스를 직접 시작하지 않는다.
 * [startRun]이면 `EXTRA_START_RUN`으로 러닝 준비(P1)부터.
 */
private fun openApp(
    context: Context,
    startRun: Boolean,
): Action {
    val intent =
        checkNotNull(context.packageManager.getLaunchIntentForPackage(context.packageName)) { "시작 화면 없음" }
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    if (startRun) intent.putExtra(AppIntents.EXTRA_START_RUN, true)
    return actionStartActivity(intent)
}
