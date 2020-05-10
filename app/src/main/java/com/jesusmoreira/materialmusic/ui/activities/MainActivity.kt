package com.jesusmoreira.materialmusic.ui.activities

import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
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
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.GeneralUtil
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.activity_player.*

class MainActivity : AppCompatActivity(), PlayerFragment.PlayerListener,  SongListener, AlbumListener, ArtistListener {

    companion object {
        const val SERVICE_STATE: String = "ServiceState"

        private const val ARG_AUDIO_LIST: String = "ARG_AUDIO_LIST"
        private const val ARG_INDEX: String = "ARG_INDEX"

        fun newIntent(context: Context, audioList: ArrayList<Audio>, index: Int) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(ARG_AUDIO_LIST, StorageUtil.audioListToString(audioList))
                putExtra(ARG_INDEX, index)
            }
    }

    private var serviceBound = false
//    private var audioList : ArrayList<Audio>? = arrayListOf()

    private var player: MediaPlayerService? = null
    private var serviceConnection : ServiceConnection? = null

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

        intent?.extras?.apply {
            val audioList: ArrayList<Audio> = StorageUtil.audioListFromString(getString(ARG_AUDIO_LIST))
            val index: Int = getInt(ARG_INDEX)

            if (!audioList.isNullOrEmpty()) {
                startPlayer(audioList, index)
            }
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

    override fun getProgress(): Int? {
        return player?.getProgress()
    }

    override fun onSongClicked(audioList: ArrayList<Audio>, position: Int) {
        startPlayer(audioList, position)
    }

    override fun onAlbumClicked(album: Album) {
//        val playerIntent = Intent(applicationContext, MediaPlayerService::class.java)
//        stopService(playerIntent)
        startActivity(AlbumDetailActivity.newIntent(applicationContext, album))
    }

    override fun onArtistClicked(artist: Artist) {

    }

    private fun startPlayer(audioList: ArrayList<Audio>, position: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.player, PlayerFragment.newInstance(audioList, position, true))
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
