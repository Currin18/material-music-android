package com.jesusmoreira.materialmusic.models

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException
import java.io.Serializable

data class Album(
    var album: String? = null,
//    var albumArt: String? = null,
    var albumId: Long? = null,
//    var albumKey: String? = null,

    var artist: String? = null,
    var artistId: Long? = null,
    var artistKey: String? = null,

    var firstYear: Int? = null,
    var lastYear: Int? = null,

    var numberOfSongs: Int? = null,
    var numberOfSongsForArtist: Int? = null
): Serializable {

    companion object {
        private const val TAG: String = "AlbumModel"
    }

    constructor(cursor: Cursor): this() {
        album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
//        albumArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
        albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID))
//        albumKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY))

        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            artistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST_ID))
//        artistKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST_KEY))

        firstYear = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR))
        lastYear = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.LAST_YEAR))

        numberOfSongs = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS))
        numberOfSongsForArtist = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST))
    }

//    constructor(audio: Audio): this() {
//        album = audio.album
//        albumId = audio.albumId
//        artist = audio.artist
//        artistId = audio.artistId
//
//    }

    fun getAlbumArtBitmap(context: Context, width: Int = 150, height: Int = 150): Bitmap? {
        val sArtworkUri = Uri
            .parse("content://media/external/audio/albumart")
        val albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId!!)

        Log.d(TAG, albumArtUri.toString())
        var bitmap: Bitmap? = null
        try {
            bitmap = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
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