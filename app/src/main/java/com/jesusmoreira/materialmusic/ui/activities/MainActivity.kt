package com.jesusmoreira.materialmusic.ui.activities

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.models.Artist
import com.jesusmoreira.materialmusic.services.MediaPlayerService
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.fragments.albums.AlbumListener
import com.jesusmoreira.materialmusic.ui.fragments.artists.ArtistListener
import com.jesusmoreira.materialmusic.ui.fragments.songs.TabSongsFragment
import com.jesusmoreira.materialmusic.ui.fragments.player.PlayerFragment
import com.jesusmoreira.materialmusic.ui.fragments.player.PlayerListener
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.GeneralUtil
import com.jesusmoreira.materialmusic.utils.PlaybackUtil
import com.jesusmoreira.materialmusic.utils.ServiceUtil
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.activity_player.*

class MainActivity : AppCompatActivity(), ServiceConnection, PlayerListener,  SongListener, AlbumListener, ArtistListener {

    companion object {
        const val SERVICE_STATE: String = "ServiceState"

        const val AUDIO_LIST_REQUEST: Int = 1002

        const val ARG_AUDIO_LIST: String = "ARG_AUDIO_LIST"
        const val ARG_INDEX: String = "ARG_INDEX"

        fun newIntent(context: Context, audioList: ArrayList<Audio>, index: Int) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(ARG_AUDIO_LIST, StorageUtil.audioListToString(audioList))
                putExtra(ARG_INDEX, index)
            }
    }

    private var serviceBound = false
    private var audioList: ArrayList<Audio> = arrayListOf()
    private var audioIndex: Int = 0

    private var player: MediaPlayerService? = null
    private var serviceConnection : ServiceConnection? = null

    private var token: ServiceUtil.ServiceToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_library,
                R.id.navigation_podcast,
                R.id.navigation_folder,
                R.id.navigation_settings
            )
        )
        setSupportActionBar(toolbar)
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        serviceConnection = object: ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                val binder : MediaPlayerService.LocalBinder = service as MediaPlayerService.LocalBinder
                player = binder.getService()
                serviceBound = true

//                Toast.makeText(this@MainActivity, "Service Bound", Toast.LENGTH_SHORT).show()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }

//        button.setOnClickListener {
//            val storage = StorageUtil(this)
//            audioList?.let { supportFragmentManager.beginTransaction().replace(R.id.player, PlayerFragment.newInstance(it, 0, true)).commit() }
//        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            button.visibility = View.VISIBLE
//            loadAudio()
        } else {
            // Ask for permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

//        intent?.extras?.apply {
//            audioList = StorageUtil.audioListFromString(getString(ARG_AUDIO_LIST))
//            audioIndex = getInt(ARG_INDEX)
//        }
//        if (!audioList.isNullOrEmpty()) {
//            startPlayer(audioList, audioIndex)
//        }

        volumeControlStream = AudioManager.STREAM_MUSIC

        token = ServiceUtil.bindToService(this, this)
    }

    override fun onDestroy() {

        token?.let {
            ServiceUtil.unbindFromService(it)
            token = null
        }

        super.onDestroy()

        if (serviceBound) {
            serviceConnection?.let { unbindService(it) }
            // service is active
            player?.stopSelf()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.i("MainActivity", "Service connected: $name")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i("MainActivity", "Service disconnected: $name")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(PlayerActivity.SERVICE_STATE, serviceBound)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean(PlayerActivity.SERVICE_STATE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            button.visibility = View.VISIBLE
//            loadAudio()
        } else {
            GeneralUtil.longToast(this, "Permission not granted. Shutting down.")
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                AUDIO_LIST_REQUEST -> {
                    data?.extras?.apply {
                        audioList = StorageUtil.audioListFromString(getString(ARG_AUDIO_LIST))
                        audioIndex = getInt(ARG_INDEX)
                    }
                    if (!audioList.isNullOrEmpty()) {
                        startPlayer(audioList, audioIndex)
                    }
                }
            }
    }

    override fun onBackPressed() {
        val playerFragments = supportFragmentManager.fragments.filterIsInstance<PlayerFragment>()
        if (playerFragments.isNotEmpty()) {
            with(playerFragments[0]) {
                if (!this.isMinimized) {
                    this.minimize()
                } else {
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
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

    override fun onPlayOrPause(): Boolean {
        with(PlaybackUtil) {
            return if (isPlaying()) {
                pause()
                false
            } else {
                play()
                true
            }
        }
    }

    override fun onSkipToPrevious() {
        audioIndex = when {
            audioIndex - 1 < 0 -> audioList.size - 1
            else -> audioIndex - 1
        }
        startPlayer(audioList, audioIndex)
    }

    override fun onSkipToNext() {
        audioIndex = when {
            audioIndex + 1 >= audioList.size -> 0
            else -> audioIndex + 1
        }
        startPlayer(audioList, audioIndex)
    }

    override fun onSeekTo(position: Int) {
        PlaybackUtil.seek(position.toLong())
    }

    override fun onGetProgress(): Int? {
//        return player?.getProgress()
        return PlaybackUtil.getPosition().toInt()
    }

    override fun onSongClicked(audioList: ArrayList<Audio>, position: Int) {
        this.audioList = audioList
        this.audioIndex = position
        startPlayer(audioList, position)
    }

    override fun onAlbumClicked(album: Album) {
//        val playerIntent = Intent(applicationContext, MediaPlayerService::class.java)
//        stopService(playerIntent)
        startActivityForResult(AlbumDetailActivity.newIntent(applicationContext, album), AUDIO_LIST_REQUEST)
    }

    override fun onArtistClicked(artist: Artist) {
        startActivityForResult(ArtistDetailActivity.newIntent(applicationContext, artist), AUDIO_LIST_REQUEST)
    }

    private fun startPlayer(audioList: ArrayList<Audio>, position: Int) {
        with(PlaybackUtil) {
            if (isPlaying()) {
                stop()
            }
            openFile(audioList[position].uri.toString())

            play()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.player, PlayerFragment.newInstance(audioList, position, false))
            .commitNow()
    }

//    private fun loadAudio() {
//        val contentResolver: ContentResolver? = contentResolver
//        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
//        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
//        val cursor: Cursor? = contentResolver?.query(uri, null, selection, null, sortOrder)
//
//        cursor?.let {
//            if (it.count > 0) {
//                audioList = arrayListOf()
//                while (cursor.moveToNext()) {
//                    audioList?.add(Audio(cursor))
//                }
//            }
//            it.close()
//        }
//    }
}
