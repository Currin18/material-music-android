package com.jesusmoreira.materialmusic.models

import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
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
    var albumArt: Bitmap? = null,

//    var genre: Short? = null,

    var path: String? = null,
    var relativePath: String? = null,
    var volumeName: String? = null,
    var duration: Long? = null
): Serializable {
    fun getURI() = id?.let { id ->
        ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
        )
    }

    fun getAlbumArt(): Uri? {
        return try {
            val genericArtUri: Uri = Uri.parse("content://media/external/audio/albumart")
            val actualArtUri: Uri? = albumId?.let { ContentUris.withAppendedId(genericArtUri, it) }
            actualArtUri
        } catch(e: Exception) {
            null
        }
    }
}