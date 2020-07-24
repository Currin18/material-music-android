package com.jesusmoreira.materialmusic.models

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.jesusmoreira.materialmusic.R
import java.io.FileNotFoundException
import java.io.IOException
import java.io.Serializable


data class Audio(
    var id: Long? = null,
    var title: String? = null,
    var displayName: String? = null,

    var artist: String? = null,
    var artistId: Long? = null,
    var artistKey: String? = null,

    var album: String? = null,
    var albumId: Long? = null,
    var albumKey: String? = null,
//    var albumArtPath: String? = null,
//    var albumArt: Bitmap? = null,

//    var genre: Short? = null,

    var path: String? = null,
    var relativePath: String? = null,
    var volumeName: String? = null,
    var duration: Long? = null

): Serializable {

    companion object {
        private const val TAG: String = "AudioModel"
    }

    constructor(cursor: Cursor): this() {
        id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
        displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))

        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
        artistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
        artistKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_KEY))

        album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
        albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
        albumKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY))
//        albumArtPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            relativePath =
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH))
            volumeName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.VOLUME_NAME))
            duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
        }

        path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
    }

    val uri: Uri?
        get() {
            return id?.let { id ->
                ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )
            }
        }

//    val albumArt: Uri?
//        get() {
//            return try {
//                val genericArtUri: Uri = Uri.parse("content://media/external/audio/albumart")
//                val currentArtUri: Uri? = albumId?.let { ContentUris.withAppendedId(genericArtUri, it) } ?: genericArtUri
//                currentArtUri
//            } catch(e: Exception) {
//                null
//            }
//        }

    fun getAlbumArtBitmap(context: Context, width: Int = 150, height: Int = 150): Bitmap? {
        val sArtworkUri = Uri
            .parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId!!)

        Log.d(TAG, albumArtUri.toString())
        var bitmap: Bitmap? = null
        try {
            bitmap = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(
                        context.contentResolver, albumArtUri
                    ))
                }
                else -> {
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver, albumArtUri
                    )
                }
            }
            bitmap = bitmap?.let { Bitmap.createScaledBitmap(it, width, height, true) }
        } catch (exception: FileNotFoundException) {
            Log.w(TAG, "${exception.message}")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap

//        return try {
//            val genericArtUri: Uri = Uri.parse("content://media/external/audio/albumart")
//            val currentArtUri: Uri? = albumId?.let { ContentUris.withAppendedId(genericArtUri, it) } ?: genericArtUri
//            BitmapFactory.decodeFile(currentArtUri.toString())
//        } catch(e: Exception) {
//            null
//        }
    }
}