package com.jesusmoreira.materialmusic.controllers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Artist
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.models.Folder


class MediaController(private val context: Context) {

    companion object {
        private const val TAG = "MediaController"
        private const val SELECTION_AUDIO_IS_MUSIC = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
//            + " AND _data LIKE '%/Music/%'"
    }

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

    private fun getAlbumContent(
        uri: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
    ): Cursor? = getAudioContent(
        uri,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )

    private fun getArtistContent(
        uri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
    ): Cursor? = getAudioContent(
        uri,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )

    fun getMusicList(): ArrayList<Audio> {
        val musicList = arrayListOf<Audio>()
        getAudioContent(selection = SELECTION_AUDIO_IS_MUSIC).use { cursor ->
            while(cursor?.moveToNext() == true) {
                musicList.add(Audio(cursor))
            }
        }

        return musicList
    }

    fun getMusicListFromAlbum(album: Album): ArrayList<Audio> {
        val musicList = arrayListOf<Audio>()

        getAudioContent(selection = SELECTION_AUDIO_IS_MUSIC +
                " AND ${MediaStore.Audio.Media.ALBUM_ID} == ${album.albumId}").use { cursor ->
            while(cursor?.moveToNext() == true) {
                musicList.add(Audio(cursor))
            }
        }

        return musicList
    }

    fun getMusicListFromAlbumList(albumList: ArrayList<Album>): ArrayList<Audio> {
        val musicList = arrayListOf<Audio>()

        var selection = SELECTION_AUDIO_IS_MUSIC
        if (albumList.isNotEmpty()) {
            selection += " AND ("
            for (i in 0 until albumList.size) {
                if (i != 0) {
                    selection += " OR "
                }
                selection += "${MediaStore.Audio.Media.ALBUM_ID} == ${albumList[i].albumId}"
            }
            selection += ")"
        }

        getAudioContent(selection = selection).use { cursor ->
            while(cursor?.moveToNext() == true) {
                musicList.add(Audio(cursor))
            }
        }

        return musicList
    }

    fun getMusicListFromFolder(folder: Folder): ArrayList<Audio> {
        val musicList = arrayListOf<Audio>()

        var selection = SELECTION_AUDIO_IS_MUSIC // + " AND ${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        folder.fileList.let { list ->
            if (list.isNotEmpty()) {
                selection += " AND ("
                for (i in 0 until list.size) {
                    if (i != 0) {
                        selection += " OR "
                    }
                    selection += "${MediaStore.Audio.Media.DISPLAY_NAME} == ?" //'${list[i].replace("'","\'")}'"
                }
                selection += ")"
            }
        }

        Log.d(TAG, "getMusicListFromFolder: $selection")

        getAudioContent(selection = selection, selectionArgs = folder.fileList.toTypedArray()).use { cursor ->
            while(cursor?.moveToNext() == true) {
                musicList.add(Audio(cursor))
            }
        }

        return musicList
    }

    fun getAlbumListFromArtist(artist: Artist): ArrayList<Album> {
        val albumList = arrayListOf<Album>()

        getAlbumContent(
            uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artist.artistId),
            sortOrder = "${MediaStore.Audio.Artists.Albums.FIRST_YEAR} DESC"
        ).use { cursor ->
            while(cursor?.moveToNext() == true) {
                albumList.add(Album(cursor))
            }
        }

        return albumList
    }

    fun getAlbumList(): ArrayList<Album> {
        val albumList = arrayListOf<Album>()

        getAlbumContent().use { cursor ->
            while(cursor?.moveToNext() == true) {
                albumList.add(Album(cursor))
            }
        }

        return albumList
    }

    fun getArtistList(): ArrayList<Artist> {
        val artistList = arrayListOf<Artist>()

        getArtistContent().use { cursor ->
            while (cursor?.moveToNext() == true) {
                artistList.add(Artist(cursor))
            }
        }

        return artistList
    }
}
