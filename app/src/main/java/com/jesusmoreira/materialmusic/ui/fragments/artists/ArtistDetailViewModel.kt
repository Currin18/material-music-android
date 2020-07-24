package com.jesusmoreira.materialmusic.ui.fragments.artists

import androidx.lifecycle.ViewModel
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Artist
import com.jesusmoreira.materialmusic.models.Audio

class ArtistDetailViewModel : ViewModel() {
    var artist : Artist? = null
    var albumList: ArrayList<Album> = arrayListOf()
    var audioList: ArrayList<Audio> = arrayListOf()
}
