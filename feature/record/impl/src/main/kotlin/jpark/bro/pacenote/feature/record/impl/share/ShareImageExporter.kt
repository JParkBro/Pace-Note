package jpark.bro.pacenote.feature.record.impl.share

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import jpark.bro.pacenote.core.common.suspendRunCatching
import jpark.bro.pacenote.feature.record.impl.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** 공유 카드 PNG 만들기 · 저장 · 공유 (product-spec 5-6). Root에서만 쓴다. */
internal object ShareImageExporter {
    private const val EXPORT_WIDTH = 1_080
    private const val EXPORT_HEIGHT = 1_350
    private const val PNG_QUALITY = 100
    private const val PHOTO_MAX_SIZE = 2_048

    /** 화면에 그린 카드를 내보낼 크기로 맞춘다: 기본 · 사진 1080×1350, 투명 1080×1080 */
    fun toExportSize(
        captured: Bitmap,
        background: ShareBackground,
    ): Bitmap {
        val height = if (background == ShareBackground.TRANSPARENT) EXPORT_WIDTH else EXPORT_HEIGHT
        return captured.scale(EXPORT_WIDTH, height)
    }

    /** 사진 앱(`Pictures/런닝`)에 저장한다. API 29+ MediaStore라 저장소 권한이 필요 없다. */
    suspend fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            suspendRunCatching {
                val resolver = context.contentResolver
                val values =
                    ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/${context.getString(R.string.record_share_folder)}",
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                val uri =
                    checkNotNull(
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values),
                    ) { "저장 위치를 만들 수 없음" }
                suspendRunCatching {
                    resolver.openOutputStream(uri).use { out ->
                        check(
                            out != null && bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, out),
                        ) { "PNG 저장 실패" }
                    }
                    resolver.update(
                        uri,
                        ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                        null,
                        null,
                    )
                }.onFailure { resolver.delete(uri, null, null) }
                    .getOrThrow()
                Unit
            }
        }

    /** 캐시에 PNG를 쓰고 시스템 공유 시트를 연다. */
    suspend fun share(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
    ): Result<Unit> =
        suspendRunCatching {
            val file =
                withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "share").apply { mkdirs() }
                    File(dir, "$fileName.png").also { file ->
                        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }
                    }
                }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.share", file)
            val send =
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

    /** 사진 배경을 읽는다. 큰 사진은 줄여서 읽는다. */
    suspend fun loadPhoto(
        context: Context,
        uri: String,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            suspendRunCatching {
                val source = ImageDecoder.createSource(context.contentResolver, Uri.parse(uri))
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val longest = maxOf(info.size.width, info.size.height)
                    if (longest > PHOTO_MAX_SIZE) {
                        val ratio = PHOTO_MAX_SIZE.toFloat() / longest
                        decoder.setTargetSize((info.size.width * ratio).toInt(), (info.size.height * ratio).toInt())
                    }
                    // 하드웨어 비트맵은 Compose 캡처에서 다시 그릴 수 없어 소프트웨어로 읽는다
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }.getOrNull()
        }
}
