package com.jesusmoreira.materialmusic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

abstract class PlayerBroadcast: BroadcastReceiver() {
    companion object {
        const val ACTION_PLAY: String = "PlayerBroadcast.ACTION_PLAY"
        const val ACTION_PAUSE: String = "PlayerBroadcast.ACTION_PAUSE"
        const val ACTION_REFRESH: String = "PlayerBroadcast.ACTION_REFRESH"
    }

    var isRegistered = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != null) {
            when {
                intent.action.equals(ACTION_PLAY) -> {
                    onActionPlay()
                }
                intent.action.equals(ACTION_PAUSE) -> {
                    onActionPause()
                }
                intent.action.equals(ACTION_REFRESH) -> {
                    onActionRefresh()
                }
            }
        }
    }

    fun registerReceiver(context: Context) {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_REFRESH)
        })
        isRegistered = true
    }

    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(this)
        isRegistered = false
    }

    abstract fun onActionPlay()
    abstract fun onActionPause()
    abstract fun onActionRefresh()
}