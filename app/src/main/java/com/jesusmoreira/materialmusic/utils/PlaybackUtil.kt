package com.jesusmoreira.materialmusic.utils

import android.os.RemoteException

/**
 * Provides convenience methods for interacting with the service & silently handling exceptions
 */
object PlaybackUtil {
    fun openFile(path: String) {
        try {
            ServiceUtil.sService?.openFile(path)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun play() {
        try {
            ServiceUtil.sService?.play()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            ServiceUtil.sService?.pause()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            ServiceUtil.sService?.stop()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun getDuration(): Long =
        try {
            ServiceUtil.sService?.duration ?: 0
        } catch (e: RemoteException) {
            e.printStackTrace()
            0
        }

    fun getPosition(): Long =
        try {
            ServiceUtil.sService?.position ?: 0
        } catch (e: RemoteException) {
            e.printStackTrace()
            0
        }

    fun isPlaying(): Boolean =
        try {
            ServiceUtil.sService?.isPlaying ?: false
        } catch (e: RemoteException) {
            e.printStackTrace()
            false
        }

    fun seek(position: Long) {
        try {
            ServiceUtil.sService?.seek(position)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }
}