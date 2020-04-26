package com.jesusmoreira.materialmusic.models

import android.content.ContentUris
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.jesusmoreira.materialmusic.BuildConfig
import java.io.Serializable
import java.lang.Exception

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
//    var albumArt: Bitmap? = null,

//    var genre: Short? = null,

    var path: String? = null,
    var relativePath: String? = null,
    var volumeName: String? = null,
    var duration: Long? = null

): Serializable {

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

    val albumArt: Uri?
        get() {
            return try {
                val genericArtUri: Uri = Uri.parse("content://media/external/audio/albumart")
                val currentArtUri: Uri? = albumId?.let { ContentUris.withAppendedId(genericArtUri, it) } ?: genericArtUri
                currentArtUri
            } catch(e: Exception) {
                null
            }
        }

    val albumArtBitmap: Bitmap?
        get() {
            return try {
                val genericArtUri: Uri = Uri.parse("content://media/external/audio/albumart")
                val currentArtUri: Uri? = albumId?.let { ContentUris.withAppendedId(genericArtUri, it) } ?: genericArtUri
                BitmapFactory.decodeFile(currentArtUri.toString())
            } catch(e: Exception) {
                null
            }
        }
}