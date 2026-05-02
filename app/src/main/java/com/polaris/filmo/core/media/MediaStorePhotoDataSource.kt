package com.polaris.filmo.core.media

import android.content.ContentValues
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.polaris.filmo.core.renderer.CapturedFrame
import com.polaris.filmo.data.datasource.PhotoLocalDataSource
import com.polaris.filmo.domain.model.CapturedPhoto
import com.polaris.filmo.domain.model.FilmType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class MediaStorePhotoDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PhotoLocalDataSource {
    override suspend fun savePhoto(
        frame: CapturedFrame,
        filmType: FilmType
    ): CapturedPhoto = withContext(Dispatchers.IO) {
        val createdAt = System.currentTimeMillis()
        val displayName = "Filmo_${fileTimestamp(createdAt)}"
        val uri = insertImage(displayName)

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val bitmap = frame.toBitmap()
                try {
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)) {
                        "Photo encoding failed."
                    }
                } finally {
                    bitmap.recycle()
                }
            } ?: error("Photo output stream could not be opened.")
            markReady(uri)

            CapturedPhoto(
                id = uri.toString(),
                uri = uri.toString(),
                filmType = filmType,
                createdAt = createdAt,
                width = frame.width,
                height = frame.height
            )
        } catch (throwable: Throwable) {
            context.contentResolver.delete(uri, null, null)
            throw throwable
        }
    }

    override suspend fun getCapturedPhotos(): List<CapturedPhoto> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )
        val selection: String
        val selectionArgs: Array<String>

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ? AND " +
                "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
            selectionArgs = arrayOf(FILMO_RELATIVE_PATH, "$FILMO_FILE_PREFIX%")
        } else {
            selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            selectionArgs = arrayOf("%/Pictures/Filmo/$FILMO_FILE_PREFIX%")
        }

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val photos = mutableListOf<CapturedPhoto>()

        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val mediaId = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(collection, mediaId)
                photos += CapturedPhoto(
                    id = uri.toString(),
                    uri = uri.toString(),
                    filmType = FilmType.Portra400,
                    createdAt = cursor.getLong(dateAddedColumn) * MILLIS_PER_SECOND,
                    width = cursor.getInt(widthColumn),
                    height = cursor.getInt(heightColumn)
                )
            }
        }

        photos
    }

    override suspend fun deletePhoto(photoId: String) = withContext(Dispatchers.IO) {
        val deletedRows = context.contentResolver.delete(Uri.parse(photoId), null, null)
        check(deletedRows > 0) {
            "Photo could not be deleted."
        }
    }

    private fun insertImage(displayName: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Filmo")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Filmo"
                )
                check(directory.exists() || directory.mkdirs()) {
                    "Photo directory could not be created."
                }
                put(MediaStore.Images.Media.DATA, File(directory, "$displayName.jpg").absolutePath)
            }
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: error("Photo could not be created.")

        return uri
    }

    private fun markReady(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    private fun CapturedFrame.toBitmap(): Bitmap {
        val pixels = IntArray(width * height)
        var targetIndex = 0
        for (y in height - 1 downTo 0) {
            val rowStart = y * width * BYTES_PER_PIXEL
            for (x in 0 until width) {
                val sourceIndex = rowStart + x * BYTES_PER_PIXEL
                val r = rgbaBytes[sourceIndex].toInt() and 0xFF
                val g = rgbaBytes[sourceIndex + 1].toInt() and 0xFF
                val b = rgbaBytes[sourceIndex + 2].toInt() and 0xFF
                val a = rgbaBytes[sourceIndex + 3].toInt() and 0xFF
                pixels[targetIndex++] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun fileTimestamp(timestamp: Long): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestamp))

    private companion object {
        const val JPEG_QUALITY = 94
        const val BYTES_PER_PIXEL = 4
        const val FILMO_RELATIVE_PATH = "Pictures/Filmo/"
        const val FILMO_FILE_PREFIX = "Filmo_"
        const val MILLIS_PER_SECOND = 1_000L
    }
}
