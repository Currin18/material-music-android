package com.jesusmoreira.materialmusic.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceUtil {
    private const val PREFERENCE = "PreferenceUtil.PREFERENCE"

    private const val PREFERENCE_AUDIO_LIST = "$PREFERENCE.PREFERENCE_AUDIO_LIST"
    private const val PREFERENCE_AUDIO_LIST_BACKUP = "$PREFERENCE.PREFERENCE_AUDIO_LIST_BACKUP"
    private const val PREFERENCE_AUDIO_INDEX = "$PREFERENCE.PREFERENCE_AUDIO_INDEX"
    private const val PREFERENCE_AUDIO_PROGRESSION = "$PREFERENCE.PREFERENCE_AUDIO_PROGRESSION"
    private const val PREFERENCE_SHUFFLE_MODE = "$PREFERENCE.PREFERENCE_SHUFFLE_MODE"
    private const val PREFERENCE_REPEAT_MODE = "$PREFERENCE.PREFERENCE_REPEAT_MODE"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE)
    }

    private fun getEditor(context: Context): SharedPreferences.Editor {
        return getPreferences(context).edit()
    }

    private fun getString(context: Context, key: String): String? {
        return getPreferences(context).getString(key, null)
    }

    private fun putString(context: Context, key: String, value: String): Boolean {
        return getEditor(context).let { editor ->
            editor.putString(key, value)
            editor.commit()
        }
    }

    private fun getInt(context: Context, key: String): Int? {
        return getPreferences(context).getInt(key, -1).let {
            when {
                it < 0 -> null
                else -> it
            }
        }
    }

    private fun putInt(context: Context, key: String, value: Int): Boolean {
        return getEditor(context).let { editor ->
            editor.putInt(key, value)
            editor.commit()
        }
    }

    private fun getLong(context: Context, key: String): Long? {
        return getPreferences(context).getLong(key, -1L).let {
            when {
                it < 0L -> null
                else -> it
            }
        }
    }

    private fun putLong(context: Context, key: String, value: Long): Boolean {
        return getEditor(context).let { editor ->
            editor.putLong(key, value)
            editor.commit()
        }
    }

    fun getAudioList(context: Context) = getString(context, PREFERENCE_AUDIO_LIST)
    fun setAudioList(context: Context, value: String) =
        putString(context, PREFERENCE_AUDIO_LIST, value)

    fun getAudioListBackup(context: Context) = getString(context, PREFERENCE_AUDIO_LIST_BACKUP)
    fun setAudioListBackup(context: Context, value: String) =
        putString(context, PREFERENCE_AUDIO_LIST_BACKUP, value)

    fun getAudioIndex(context: Context) = getInt(context, PREFERENCE_AUDIO_INDEX)
    fun setAudioIndex(context: Context, value: Int) =
        putInt(context, PREFERENCE_AUDIO_INDEX, value)

    fun getAudioProgress(context: Context) = getLong(context, PREFERENCE_AUDIO_PROGRESSION)
    fun setAudioProgress(context: Context, value: Long) =
        putLong(context, PREFERENCE_AUDIO_PROGRESSION, value)

    fun getShuffleMode(context: Context) = getInt(context, PREFERENCE_SHUFFLE_MODE)
    fun setShuffleMode(context: Context, value: Int) =
        putInt(context, PREFERENCE_SHUFFLE_MODE, value)

    fun getRepeatMode(context: Context) = getInt(context, PREFERENCE_REPEAT_MODE)
    fun setRepeatMode(context: Context, value: Int) =
        putInt(context, PREFERENCE_REPEAT_MODE, value)
}