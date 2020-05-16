package com.jesusmoreira.materialmusic.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

abstract class PlayerBroadcast: BroadcastReceiver() {
    companion object {
        const val ACTION_PLAY_OR_PAUSE: String = "PlayerBroadcast.ACTION_PLAY_OR_PAUSE"
        const val ACTION_PREVIOUS: String = "PlayerBroadcast.ACTION_PREVIOUS"
        const val ACTION_NEXT: String = "PlayerBroadcast.ACTION_NEXT"
        const val ACTION_COMPLETION: String = "PlayerBroadcast.ACTION_COMPLETION"
    }

    var isRegistered = false

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != null) {
            when {
                intent.action.equals(ACTION_PLAY_OR_PAUSE) -> {
                    onActionPlayOrPause()
                }
                intent.action.equals(ACTION_PREVIOUS) -> {
                    onActionPrevious()
                }
                intent.action.equals(ACTION_NEXT) -> {
                    onActionNext()
                }
                intent.action.equals(ACTION_COMPLETION) -> {
                    onCompletion()
                }
            }
        }
    }

    fun registerReceiver(context: Context) {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(ACTION_PLAY_OR_PAUSE)
            addAction(ACTION_PREVIOUS)
            addAction(ACTION_NEXT)
            addAction(ACTION_COMPLETION)
        })
        isRegistered = true
    }

    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(this)
        isRegistered = false
    }

    abstract fun onActionPlayOrPause()
    abstract fun onActionPrevious()
    abstract fun onActionNext()
    abstract fun onCompletion()
}