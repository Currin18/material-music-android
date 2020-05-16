package com.jesusmoreira.materialmusic.controllers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Artist
import com.jesusmoreira.materialmusic.models.Audio


class MediaController(private val context: Context) {

    companion object {
        private const val SELECTION_AUDIO_IS_MUSIC = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
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

        getAudioContent(selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 " +
                "AND ${MediaStore.Audio.Media.ALBUM_ID} == ${album.albumId}").use { cursor ->
            while(cursor?.moveToNext() == true) {
                musicList.add(Audio(cursor))
            }
        }

        return musicList
    }

    fun getMusicListFromAlbumList(albumList: ArrayList<Album>): ArrayList<Audio> {
        val musicList = arrayListOf<Audio>()

        var selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
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



//android.database.sqlite.SQLiteException: unrecognized token: " (code 1 SQLITE_ERROR): , while compiling: SELECT title_key, instance_id, duration, is_ringtone, album_artist, orientation, artist, height, is_drm, bucket_display_name, is_audiobook, owner_package_name, volume_name, title_resource_uri, date_modified, date_expires, composer, _display_name, datetaken, mime_type, is_notification, _id, year, _data, _hash, _size, album, is_alarm, title, track, width, is_music, album_key, is_trashed, group_id, document_id, artist_id, artist_key, is_pending, date_added, is_podcast, album_id, primary_directory, secondary_directory, original_document_id, bucket_id, bookmark, relative_path FROM audio WHERE ((is_pending=0) AND (is_trashed=0) AND (volume_name IN ( 'external_primary' ))) AND (is_music != 0 AND artist_key ==) ORDER BY title_key
