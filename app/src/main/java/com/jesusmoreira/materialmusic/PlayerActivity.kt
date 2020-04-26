package com.jesusmoreira.materialmusic

import android.app.PendingIntent
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.player.PlayerFragment
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.fragment_player.*
import org.jetbrains.anko.imageResource


class PlayerActivity : AppCompatActivity(), PlayerFragment.PlayerListener {

    companion object {
        const val SERVICE_STATE: String = "ServiceState"
    }

    private var serviceBound = false
    private var audioList : ArrayList<Audio>? = arrayListOf()

    private var player: MediaPlayerService? = null
    private var serviceConnection : ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setSupportActionBar(toolbar)

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

        loadAudio()

        button.setOnClickListener {
            audioList?.let { supportFragmentManager.beginTransaction().replace(R.id.player, PlayerFragment.newInstance(it, 0, true)).commit() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            serviceConnection?.let { unbindService(it) }
            // service is active
            player?.stopSelf()
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

    override fun setServiceBoundState(serviceBoundState: Boolean) {
        serviceBound = serviceBoundState
    }

    override fun getServiceBoundState(): Boolean {
        return serviceBound
    }

    override fun bindService(service: Intent, flags: Int) {
        serviceConnection?.let {
            bindService(service, it, flags)
        }
    }

    private fun loadAudio() {
        val contentResolver: ContentResolver? = contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor: Cursor? = contentResolver?.query(uri, null, selection, null, sortOrder)

        cursor?.let {
            if (it.count > 0) {
                audioList = arrayListOf()
                while (cursor.moveToNext()) {
                    audioList?.add(Audio(cursor))
                }
            }
            it.close()
        }
    }
}
