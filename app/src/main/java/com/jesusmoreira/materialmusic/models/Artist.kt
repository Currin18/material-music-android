package com.jesusmoreira.materialmusic.models

import android.database.Cursor
import android.provider.MediaStore
import java.io.Serializable

data class Artist(
    var artist: String? = null,
    var artistKey: String? = null,

    var numberOfAlbums: Int? = null,
    var numberOfTracks: Int? = null,

    var albumsList: ArrayList<Album>? = null
): Serializable {
    constructor(cursor: Cursor): this() {
        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST))
        artistKey = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST_KEY))

        numberOfAlbums = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
        numberOfTracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))

//        val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM))

    }
}