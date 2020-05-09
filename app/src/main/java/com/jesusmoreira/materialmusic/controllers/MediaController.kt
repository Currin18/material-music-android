package com.jesusmoreira.materialmusic.controllers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Audio


class MediaController(private val context: Context) {
    private fun getAudioContent(
        uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
    ): Cursor? {
        return context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }

    fun getMusicList(): ArrayList<Audio> {
        val musicList = arrayListOf<Audio>()

        getAudioContent().use { cursor ->
            while(cursor?.moveToNext() == true) {
                val isMusic = cursor.getInt(
                    cursor
                        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
                )

                if (isMusic != 0) {
                    val music = Audio(cursor)
                    musicList.add(music)
                }
            }
        }

        return musicList
    }

    fun getAlbumList(): ArrayList<Album> {
        val albumList = arrayListOf<Album>()

        getAudioContent().use { cursor ->
            while(cursor?.moveToNext() == true) {
                val isMusic = cursor.getInt(
                    cursor
                        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
                )

                if (isMusic != 0) {
                    val album = Album(cursor)
                    if (!albumList.contains(album)) {
                        albumList.add(album)
                    }
                }
            }
        }

        return albumList
    }
}