package com.jesusmoreira.materialmusic.models

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.provider.MediaStore
import com.jesusmoreira.materialmusic.controllers.MediaController
import java.io.Serializable

data class Artist(
    var artist: String? = null,
    var artistId: Long = 0L,
    var artistKey: String? = null,

    var numberOfAlbums: Int? = null,
    var numberOfTracks: Int? = null,

    var albumsList: ArrayList<Album>? = null
): Serializable {
    constructor(cursor: Cursor): this() {
        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST))
        artistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID))
        artistKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST_KEY))

        numberOfAlbums = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
        numberOfTracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))

//        val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM))

    }

    fun getAlbumes(context: Context): ArrayList<Album> =
        MediaController(context).getAlbumListFromArtist(this)

    fun getArtistArtBitmap(context: Context, width: Int = 150, height: Int = 150): Bitmap? {
        val albumList = getAlbumes(context)

        if (albumList.isNotEmpty()) {
            return albumList[0].getAlbumArtBitmap(context, width, height)
        }

        return null
    }
}