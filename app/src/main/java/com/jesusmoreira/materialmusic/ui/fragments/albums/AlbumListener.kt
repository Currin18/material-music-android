package com.jesusmoreira.materialmusic.ui.fragments.albums

import com.jesusmoreira.materialmusic.models.Album

interface AlbumListener {
    fun onAlbumClicked(album: Album)
}