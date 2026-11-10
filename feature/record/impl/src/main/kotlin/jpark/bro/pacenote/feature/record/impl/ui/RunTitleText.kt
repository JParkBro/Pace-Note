package jpark.bro.pacenote.feature.record.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.pacenote.core.domain.stats.DefaultRunTitle
import jpark.bro.pacenote.core.domain.stats.TimeOfDay
import jpark.bro.pacenote.feature.record.impl.R
import java.time.format.TextStyle
import java.util.Locale

/** "수요일 아침 러닝" (product-spec 5-8) */
@Composable
internal fun defaultTitleText(title: DefaultRunTitle): String =
    stringResource(
        R.string.record_default_title,
        title.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN),
        stringResource(
            when (title.timeOfDay) {
                TimeOfDay.MORNING -> R.string.record_time_morning
                TimeOfDay.MIDDAY -> R.string.record_time_midday
                TimeOfDay.EVENING -> R.string.record_time_evening
                TimeOfDay.NIGHT -> R.string.record_time_night
            },
        ),
    )
