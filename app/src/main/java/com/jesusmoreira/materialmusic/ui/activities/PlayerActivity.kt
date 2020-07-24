package com.jesusmoreira.materialmusic.ui.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.services.MediaPlayerService
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.fragments.player.PlayerFragment
import com.jesusmoreira.materialmusic.utils.GeneralUtil
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.activity_player.*


class PlayerActivity : AppCompatActivity()/*, PlayerListener*/ {

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

        button.setOnClickListener {
            val storage = StorageUtil(this)
            audioList?.let { supportFragmentManager.beginTransaction().replace(R.id.player, PlayerFragment.newInstance(it, 0, true)).commit() }
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            button.visibility = View.VISIBLE
        } else {
            // Ask for permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            button.visibility = View.VISIBLE
        } else {
            GeneralUtil.longToast(this, "Permission not granted. Shutting down.")
            finish()
        }
    }

//    override fun setServiceBoundState(serviceBoundState: Boolean) {
//        serviceBound = serviceBoundState
//    }
//
//    override fun getServiceBoundState(): Boolean {
//        return serviceBound
//    }
//
//    override fun bindService(service: Intent, flags: Int) {
//        serviceConnection?.let {
//            bindService(service, it, flags)
//        }
//    }
//
//    override fun getProgress(): Int? {
//        return player?.getProgress()
//    }
}
