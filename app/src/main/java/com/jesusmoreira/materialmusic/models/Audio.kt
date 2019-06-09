package com.jesusmoreira.materialmusic.models

import android.graphics.Bitmap
import java.io.Serializable

data class Audio(
    var id: Long? = null,
    var title: String? = null,
    var displayName: String? = null,

    var artist: String? = null,
    var artistId: String? = null,
    var artistKey: String? = null,

    var album: String? = null,
    var albumId: String? = null,
    var albumKey: String? = null,
    var albumArt: Bitmap? = null,

//    var genre: Short? = null,

    var path: String? = null,
    var relativePath: String? = null,
    var volumeName: String? = null,
    var duration: Long? = null
): Serializable