package jpark.bro.pacenote.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * 2×2 위젯의 진행 링. Glance는 Canvas를 쓸 수 없어 비트맵으로 그린다 (widget/CLAUDE.md).
 * 흰색 마스크로 그리고 `ColorFilter.tint`로 테마 색을 입혀 기기 라이트 · 다크를 따른다.
 */
internal object ProgressRing {
    private const val SIZE_PX = 240
    private const val STROKE_RATIO = 0.1f
    private const val START_DEGREES = -90f
    private const val FULL_DEGREES = 360f

    /** 링 바탕 (한 바퀴) */
    fun track(): Bitmap = draw(FULL_DEGREES)

    /** 진행 부분. [ratio]가 1을 넘으면 한 바퀴 */
    fun progress(ratio: Double): Bitmap = draw(FULL_DEGREES * ratio.toFloat().coerceIn(0f, 1f))

    private fun draw(sweep: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE_PX, SIZE_PX, Bitmap.Config.ARGB_8888)
        val stroke = SIZE_PX * STROKE_RATIO
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = stroke
                strokeCap = Paint.Cap.ROUND
            }
        val inset = stroke / 2
        if (sweep > 0f) {
            Canvas(
                bitmap,
            ).drawArc(RectF(inset, inset, SIZE_PX - inset, SIZE_PX - inset), START_DEGREES, sweep, false, paint)
        }
        return bitmap
    }
}
