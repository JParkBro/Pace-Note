package jpark.bro.pacenote.feature.run.impl.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import jpark.bro.pacenote.feature.run.impl.RunAction

// 러닝 Root가 쓰는 시스템 연동: 권한 · 기기 상태 확인, 위치 켜기 요청, 화면 켜짐 유지

private const val LOCATION_INTERVAL_MS = 1_000L

@Composable
internal fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}

internal fun requiredPermissions(): Array<String> =
    buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        // 거부해도 기록은 된다. 진행 알림만 안 보인다 (product-spec 5-2)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

internal fun Context.deviceChecked() =
    RunAction.DeviceChecked(
        fineLocationGranted = granted(Manifest.permission.ACCESS_FINE_LOCATION),
        coarseLocationGranted = granted(Manifest.permission.ACCESS_COARSE_LOCATION),
        locationEnabled =
            getSystemService(LocationManager::class.java)?.let(LocationManagerCompat::isLocationEnabled) ?: false,
        ignoringBatteryOptimizations =
            getSystemService(PowerManager::class.java)?.isIgnoringBatteryOptimizations(packageName) ?: true,
    )

internal fun Context.granted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/** 기기 위치가 꺼져 있으면 시스템 설정 대화상자로 켜기를 요청하고, 그럴 수 없으면 위치 설정 화면을 연다. */
internal fun requestLocationSettings(
    context: Context,
    launchResolution: (PendingIntent) -> Unit,
) {
    val request =
        LocationSettingsRequest
            .Builder()
            .addLocationRequest(LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS).build())
            .build()
    LocationServices
        .getSettingsClient(context)
        .checkLocationSettings(request)
        .addOnFailureListener { error ->
            if (error is ResolvableApiException) {
                launchResolution(error.resolution)
            } else {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
}
