package com.jesusmoreira.materialmusic.controllers

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.jesusmoreira.materialmusic.models.Song
import java.io.File


class SongController(private val context: Context) {
    fun getSongs(): List<Song> {
//        val audio = mutableListOf<String>()
//        val c = context.contentResolver.query(
////            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
//            arrayOf(MediaStore.Audio.Media.DISPLAY_NAME),
//            null,
//            null,
//            null
//        )
//        while (c != null && c.moveToNext()) {
//            val name = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
//            audio.add(name)
//
//        }

        val songs = mutableListOf<Song>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        ).use { cursor ->
            while(cursor?.moveToNext() == true) {
                val isMusic = cursor?.getInt(
                    cursor
                        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)
                )

                if (isMusic != 0) {
                    val music = Song()


                    music.path = cursor?.getString(
                        cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    )

                    if (music.path != null && File(music.path!!).exists()) {


                        music.songId = cursor?.getLong(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        )

                        music.songTitle = cursor?.getString(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        )


                        music.songTitle = cursor?.getString(
                            cursor
                                .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                        )


                        music.album = cursor?.getString(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        )


                        music.songArtist = cursor?.getString(
                            cursor
                                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        )

    //                    music.duration = cursor
    //                        .getLong(
    //                            cursor
    //                                .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
    //                        )

    //                    MediaMetadataRetriever mmr = new MediaMetadataRetriever()
    //                    mmr.setDataSource(music.path)
    //                    music.albumArt = getBitmap(mmr.getEmbeddedPicture())
    //                    mmr.release()

                        songs.add(music)
                    }
                }
            }
        }

        return songs.toList()
    }

//    fun getCollection(volumeName: String): {
//        val collection = MediaStore.Video.Media.getContentUri(volumeName)
//        return collection
//    }
}