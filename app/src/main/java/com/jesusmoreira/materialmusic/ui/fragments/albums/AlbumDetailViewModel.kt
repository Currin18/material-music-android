package com.jesusmoreira.materialmusic.ui.fragments.albums

import androidx.lifecycle.ViewModel
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Audio

class AlbumDetailViewModel : ViewModel() {
    var album: Album? = null
    var audioList: ArrayList<Audio>? = null
}
