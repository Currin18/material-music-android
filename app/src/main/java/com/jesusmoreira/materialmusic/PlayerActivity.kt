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

    var serviceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setSupportActionBar(toolbar)
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
}
