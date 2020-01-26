package com.jesusmoreira.materialmusic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_player.*
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService
import com.jesusmoreira.materialmusic.ui.player.PlayerFragment


class PlayerActivity : AppCompatActivity(), PlayerFragment.PlayerListener {

    companion object {
        const val SERVICE_STATE: String = "ServiceState"

        private var player: MediaPlayerService? = null
        var serviceBound = false

        // Binding this Client to AudioPlayer Service
        private var serviceConnection : ServiceConnection? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setSupportActionBar(toolbar)

//        fab.setOnClickListener { view ->
////            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                .setAction("Action", null).show()
//            playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg")
//        }

        serviceConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                val binder : MediaPlayerService.LocalBinder = service as MediaPlayerService.LocalBinder
                player = binder.getService()
                serviceBound = true

                Toast.makeText(this@PlayerActivity, "Service Bound", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SERVICE_STATE, serviceBound)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean(SERVICE_STATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            serviceConnection?.let{ unbindService(it)}
            // service is active
            player?.stopSelf()
        }
    }

    private fun playAudio(media: String) {
        // Check if service is active
        if (!serviceBound) {
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            playerIntent.putExtra("media", media)
            startService(playerIntent)
            serviceConnection?.let {
                bindService(playerIntent, it, Context.BIND_AUTO_CREATE)
            }
        } else {
            Toast.makeText(this@PlayerActivity, "", Toast.LENGTH_SHORT)
            // Service is active
            // Send media with BroadcastReceiver
        }
    }

    override fun onPlayOrPause() {
        playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg")
    }

}
