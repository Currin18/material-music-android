package com.jesusmoreira.materialmusic.ui.splash

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.jesusmoreira.materialmusic.MainActivity
import com.jesusmoreira.materialmusic.R

import kotlinx.android.synthetic.main.activity_splash.*
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService



class SplashActivity : AppCompatActivity() {

    companion object {
        private var player: MediaPlayerService? = null
        var serviceBound = false

        // Binding this Client to AudioPlayer Service
        private var serviceConnection : ServiceConnection? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
            playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg")
        }

        serviceConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                val binder : MediaPlayerService.LocalBinder = service as MediaPlayerService.LocalBinder
                player = binder.getService()
                serviceBound = true

                Toast.makeText(this@SplashActivity, "Service Bound", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }



    }

    private fun playAudio(media: String) {
        // Check if service is active
        if (!serviceBound) {
            val playerIntent = Intent(this, MediaPlayerService::class.java)
            playerIntent.putExtra("media", media)
            startService(playerIntent)
            serviceConnection?.let { bindService(playerIntent, it, Context.BIND_AUTO_CREATE) }
        } else {
            // Service is active
            // Send media with BroadcastReceiver
        }
    }

}
