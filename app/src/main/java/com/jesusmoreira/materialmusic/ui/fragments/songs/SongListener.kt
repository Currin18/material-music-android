package com.jesusmoreira.materialmusic.ui.fragments.songs

import com.jesusmoreira.materialmusic.models.Audio

interface SongListener {
    fun onSongClicked(audioList: ArrayList<Audio>, position: Int)
}