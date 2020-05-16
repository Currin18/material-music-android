package com.jesusmoreira.materialmusic.ui.activities

import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.*
import com.jesusmoreira.materialmusic.services.PlayerBroadcast
import com.jesusmoreira.materialmusic.ui.fragments.albums.AlbumListener
import com.jesusmoreira.materialmusic.ui.fragments.artists.ArtistListener
import com.jesusmoreira.materialmusic.ui.fragments.player.PlayerFragment
import com.jesusmoreira.materialmusic.ui.fragments.player.PlayerListener
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.*
import kotlinx.android.synthetic.main.activity_player.*

class MainActivity : AppCompatActivity(), ServiceConnection, PlayerListener,  SongListener, AlbumListener, ArtistListener {

    companion object {
        const val TAG: String = "MainActivity"

        const val AUDIO_LIST_REQUEST: Int = 1002

        const val ARG_AUDIO_LIST: String = "ARG_AUDIO_LIST"
        const val ARG_INDEX: String = "ARG_INDEX"

        fun newIntent(context: Context, audioList: ArrayList<Audio>, index: Int) =
            Intent(context, MainActivity::class.java).apply {
                putExtra(ARG_AUDIO_LIST, StorageUtil.audioListToString(audioList))
                putExtra(ARG_INDEX, index)
            }
    }

    private var mediaSession: MediaSessionCompat? = null

    private var audioList: ArrayList<Audio> = arrayListOf()
    private var audioListBackup: ArrayList<Audio> = arrayListOf()
    private var audioIndex: Int = 0
    private var shuffleMode: ShuffleMode = ShuffleMode.NO_SHUFFLE
    private var repeatMode: RepeatMode = RepeatMode.NO_REPEAT

    private var token: ServiceUtil.ServiceToken? = null

    private var playerFragment: PlayerFragment? = null

    private val playerBroadcast: PlayerBroadcast = object : PlayerBroadcast() {
        override fun onActionPlayOrPause() {
            if (onPlayOrPause()) {
                playerFragment?.play()
            } else {
                playerFragment?.pause()
            }
        }

        override fun onActionPrevious() {
            onSkipToPrevious()
        }

        override fun onActionNext() {
            onSkipToNext()
        }

        override fun onCompletion() {
            when(repeatMode) {
                RepeatMode.NO_REPEAT -> {
                    if (audioIndex + 1 < audioList.size) onSkipToNext()
                    else {
                        audioIndex = 0
//                        onSeekTo(0)
                        startPlayer(audioList, audioIndex, paused = true)
                    }
                }
                RepeatMode.REPEAT_ALL -> {
                    onSkipToNext()
                }
                RepeatMode.REPEAT_ONE -> {
                    audioIndex--
                    onSkipToNext()

                }
            }
        }
    }

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
        supportActionBar?.title = getString(R.string.title_activity_main)
//        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

        } else {
            // Ask for permission
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        volumeControlStream = AudioManager.STREAM_MUSIC

        token = ServiceUtil.bindToService(this, this)

        mediaSession = MediaSessionCompat(this@MainActivity, "MediaSession")
        playerBroadcast.registerReceiver(this@MainActivity)

        PreferenceUtil.apply {
            audioList = StorageUtil.audioListFromString(getAudioList(this@MainActivity))
            audioListBackup = StorageUtil.audioListFromString(getAudioListBackup(this@MainActivity))
            audioIndex = getAudioIndex(this@MainActivity) ?: 0
            shuffleMode = GeneralUtil.shuffleModeFromInt(getShuffleMode(this@MainActivity))
            repeatMode = GeneralUtil.repeatModeFromInt(getRepeatMode(this@MainActivity))
        }
    }

    override fun onDestroy() {
        PreferenceUtil.apply {
            setAudioList(this@MainActivity, StorageUtil.audioListToString(audioList))
            setAudioListBackup(this@MainActivity, StorageUtil.audioListToString(audioListBackup))
            setAudioIndex(this@MainActivity, audioIndex)
            setShuffleMode(this@MainActivity, shuffleMode.value)
            setRepeatMode(this@MainActivity, repeatMode.value)
        }


        NotificationUtil.removeNotification(this@MainActivity)
        playerBroadcast.unregisterReceiver(this@MainActivity)

        PlaybackUtil.stop()

        token?.let {
            ServiceUtil.unbindFromService(it)
            token = null
        }

        super.onDestroy()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.i(TAG, "Service connected: $name")
        if (audioList.isNotEmpty()) startPlayer(audioList, audioIndex,
            minimized = true,
            paused = true
        )
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i(TAG, "Service disconnected: $name")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                        onSongClicked(audioList, audioIndex)
                    }
                }
            }
    }


    var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (playerFragment?.isMinimized == false) {
            playerFragment?.minimize()
        } else {

            if (doubleBackToExitPressedOnce)
                super.onBackPressed()

            doubleBackToExitPressedOnce = true
            GeneralUtil.shortToast(this, "Please click BACK again to exit")

            Handler().postDelayed({
                doubleBackToExitPressedOnce = false
            }, 3000)
        }
    }

    override fun onPlayOrPause(): Boolean {
        with(PlaybackUtil) {
            return if (isPlaying()) {
                pause()
                NotificationUtil.buildNotification(
                    this@MainActivity,
                    mediaSession,
                    audioList[audioIndex],
                    PlaybackStatus.PAUSED
                )
                false
            } else {
                play()
                NotificationUtil.buildNotification(
                    this@MainActivity,
                    mediaSession,
                    audioList[audioIndex],
                    PlaybackStatus.PLAYING
                )
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
        return PlaybackUtil.getPosition().toInt()
    }

    override fun onChangeShuffle(shuffle: ShuffleMode) {
        shuffleMode = shuffle
        when (shuffleMode) {
            ShuffleMode.NO_SHUFFLE -> {
                audioIndex = audioListBackup.indexOf(audioList[audioIndex])
                audioList = audioListBackup
            }
            ShuffleMode.SHUFFLE -> {
                audioListBackup = audioList
                audioList = audioList.shuffled() as ArrayList<Audio>
                audioIndex = this.audioList.indexOf(audioListBackup[audioIndex])
            }
        }

//        startPlayer(audioList, audioIndex)
        playerFragment?.updatePlaylist(audioList, audioIndex)
    }

    override fun onChangeRepeat(repeat: RepeatMode) {
        repeatMode = repeat
    }

    override fun onSongClicked(audioList: ArrayList<Audio>, position: Int, preventShuffle: Boolean) {
        if (shuffleMode == ShuffleMode.SHUFFLE && !preventShuffle) {
            audioListBackup = audioList
            this.audioList = audioList.shuffled() as ArrayList<Audio>
            audioIndex = this.audioList.indexOf(audioList[position])
        } else {
            this.audioList = audioList
            audioIndex = position
        }
        startPlayer(this.audioList, audioIndex)
    }

    override fun onAlbumClicked(album: Album) {
        startActivityForResult(AlbumDetailActivity.newIntent(applicationContext, album), AUDIO_LIST_REQUEST)
    }

    override fun onArtistClicked(artist: Artist) {
        startActivityForResult(ArtistDetailActivity.newIntent(applicationContext, artist), AUDIO_LIST_REQUEST)
    }

    private fun startPlayer(list: ArrayList<Audio>, position: Int, minimized: Boolean = false, paused: Boolean = false) {
        audioList = list
        audioIndex = position

        if (PlaybackUtil.isPlaying()) {
            PlaybackUtil.stop()
        }
        PlaybackUtil.openFile(audioList[audioIndex].uri.toString())

        if (!paused) {
            PlaybackUtil.play()
        }

        NotificationUtil.buildNotification(
            this@MainActivity,
            mediaSession,
            audioList[audioIndex],
            PlaybackStatus.PLAYING
        )

        PlayerFragment.newInstance(
            audioList,
            audioIndex,
            minimized,
            paused,
            shuffleMode.value,
            repeatMode.value
        ).apply {
            playerFragment = this
            supportFragmentManager.beginTransaction()
                .replace(R.id.player, this)
                .commitNow()
        }
    }
}
