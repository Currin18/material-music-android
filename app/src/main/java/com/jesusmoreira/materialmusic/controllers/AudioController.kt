package com.jesusmoreira.materialmusic.controllers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.jesusmoreira.materialmusic.models.Audio


class AudioController(private val context: Context) {
    private fun getContent(
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

    fun getMusicList(): List<Audio> {
        val songs = mutableListOf<Audio>()

        getContent().use { cursor ->
            while(cursor?.moveToNext() == true) {
                val isMusic = cursor.getInt(
                    cursor
                        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
                )

                if (isMusic != 0) {
                    val music = Audio()

                    music.id = cursor.getLong(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    )
                    music.title = cursor.getString(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    )
                    music.displayName = cursor.getString(
                        cursor
                            .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    )

                    /* Artist data */
                    music.artist = cursor.getString(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    )
                    music.artistId = cursor.getLong(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                    )
                    music.artistKey = cursor.getString(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_KEY)
                    )

                    /* Album data */
                    music.album = cursor.getString(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    )
                    music.albumId = cursor.getLong(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    )
                    music.albumKey = cursor.getString(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_KEY)
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        music.relativePath = cursor.getString(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
                        )
                        music.volumeName = cursor
                            .getString(
                                cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.VOLUME_NAME)
                            )
                        music.duration = cursor
                            .getLong(
                                cursor
                                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                            )

                        @Suppress("DEPRECATION")
                        music.path = cursor.getString(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        music.path = cursor.getString(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                        )
                    }

//                    if (music.path != null && File(music.path!!).exists()) {
//
//                    MediaMetadataRetriever mmr = new MediaMetadataRetriever()
//                    mmr.setDataSource(music.path)
//                    music.albumArt = getBitmap(mmr.getEmbeddedPicture())
//                    mmr.release()

                    songs.add(music)
                }
            }
        }

        return songs.toList()
    }
}