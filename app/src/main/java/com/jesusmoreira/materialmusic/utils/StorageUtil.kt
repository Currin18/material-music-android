package com.jesusmoreira.materialmusic.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jesusmoreira.materialmusic.models.Audio
import java.io.File
import java.lang.reflect.Type


class StorageUtil(val context: Context) {
    companion object {
        private const val STORAGE: String = "StorageUtil.STORAGE"

        fun audioListToString(audioList: ArrayList<Audio>?): String {
            return Gson().toJson(audioList)
        }

        fun audioListFromString(json: String?): ArrayList<Audio> {
            return when (json) {
                null -> arrayListOf()
                else -> Gson().fromJson(json, object : TypeToken<ArrayList<Audio>?>() {}.type)
            }
        }

        /**
         * Checks if a volume containing external storage is available to at least read.
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun isExternalStorageReadable(): Boolean =
            Environment.getExternalStorageState() in
                    setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)

        fun getExternalStorage(context: Context): File? {
            if (isExternalStorageReadable()) {
                val externalStorageVolumes: Array<out File> =
                    ContextCompat.getExternalFilesDirs(context, null)

                if (!externalStorageVolumes.isNullOrEmpty()) {
                    return externalStorageVolumes[0]
                }
            }

            return null
        }
    }

    private var preferences: SharedPreferences? = null

    fun storeAudio(arrayList: ArrayList<Audio>?) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        preferences?.edit()?.let { editor ->
            val gson = Gson()
            val json = gson.toJson(arrayList)
            editor.putString("audioArrayList", json)
            editor.apply()
        }
    }

    fun loadAudio(): ArrayList<Audio>? {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = preferences?.getString("audioArrayList", null)
        val type: Type = object : TypeToken<ArrayList<Audio>?>() {}.type
        return gson.fromJson(json, type)
    }

    fun storeAudioIndex(index: Int) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.putInt("audioIndex", index)
        editor.apply()
    }

    fun loadAudioIndex(): Int {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences!!.getInt("audioIndex", -1) //return -1 if no data found
    }

    fun clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences!!.edit()
        editor.clear()
        editor.commit()
    }
}