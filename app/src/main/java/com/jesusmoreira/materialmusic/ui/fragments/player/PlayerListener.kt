package com.jesusmoreira.materialmusic.ui.fragments.player

import android.content.Intent
import com.jesusmoreira.materialmusic.models.ShuffleStatus
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener

interface PlayerListener: SongListener {
    fun onPlayOrPause(): Boolean
    fun onSkipToPrevious()
    fun onSkipToNext()
    fun onSeekTo(position: Int)
    fun onGetProgress(): Int?
    fun onChangeShuffle(shuffleStatus: ShuffleStatus)

//    fun setServiceBoundState(serviceBoundState: Boolean)
//    fun getServiceBoundState(): Boolean
//    fun bindService(service: Intent, flags: Int)
}