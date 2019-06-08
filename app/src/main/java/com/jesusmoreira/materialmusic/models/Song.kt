package com.jesusmoreira.materialmusic.models

import android.graphics.Bitmap

data class Song(
    var songId: Long? = null,
    var songTitle: String? = null,
    var songArtist: String? = null,
    var path: String? = null,
    var genre: Short? = null,
    var duration: Long? = null,
    var album: String? = null,
    var albumArt: Bitmap? = null
)