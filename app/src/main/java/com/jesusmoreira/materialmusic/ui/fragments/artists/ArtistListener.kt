package com.jesusmoreira.materialmusic.ui.fragments.artists

import com.jesusmoreira.materialmusic.models.Artist

interface ArtistListener {
    fun onArtistClicked(artist: Artist)
}