package com.jesusmoreira.materialmusic.ui.fragments.player

import com.jesusmoreira.materialmusic.models.RepeatMode
import com.jesusmoreira.materialmusic.models.ShuffleMode
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener

interface PlayerListener: SongListener {
    fun onPlayOrPause(): Boolean
    fun onSkipToPrevious()
    fun onSkipToNext()
    fun onSeekTo(position: Int)
    fun onGetProgress(): Int?
    fun onChangeShuffle(shuffle: ShuffleMode)
    fun onChangeRepeat(repeat: RepeatMode)
}