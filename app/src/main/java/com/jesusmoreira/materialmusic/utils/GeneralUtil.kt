package com.jesusmoreira.materialmusic.utils

import android.content.Context
import android.widget.Toast
import com.jesusmoreira.materialmusic.models.RepeatMode
import com.jesusmoreira.materialmusic.models.ShuffleMode

object GeneralUtil {
    fun shortToast(context: Context, text: String = "") {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
    fun longToast(context: Context, text: String = "") {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun shuffleModeFromInt(shuffle: Int?): ShuffleMode = when(shuffle) {
        ShuffleMode.SHUFFLE.value -> ShuffleMode.SHUFFLE
        else -> ShuffleMode.NO_SHUFFLE
    }

    fun repeatModeFromInt(repeat: Int?): RepeatMode = when(repeat) {
        RepeatMode.REPEAT_ALL.value -> RepeatMode.REPEAT_ALL
        RepeatMode.REPEAT_ONE.value -> RepeatMode.REPEAT_ONE
        else -> RepeatMode.NO_REPEAT
    }
}