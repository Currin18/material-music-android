package com.jesusmoreira.materialmusic.controllers

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.jesusmoreira.materialmusic.PlayerActivity
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.models.PlaybackStatus
import com.jesusmoreira.materialmusic.utils.NotificationUtil
import com.jesusmoreira.materialmusic.utils.StorageUtil
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MediaPlayerService : Service(),
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnInfoListener,
    MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    companion object {
        private const val TAG: String = "MediaPlayer"

        const val PLAY_NEW_AUDIO =
            "com.jesusmoreira.materialmusic.PLAY_NEW_AUDIO"

        const val ACTION_PLAY = "com.jesusmoreira.materialmusic.ACTION_PLAY"
        const val ACTION_PAUSE = "com.jesusmoreira.materialmusic.ACTION_PAUSE"
        const val ACTION_PREVIOUS = "com.jesusmoreira.materialmusic.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.jesusmoreira.materialmusic.ACTION_NEXT"
        const val ACTION_STOP = "com.jesusmoreira.materialmusic.ACTION_STOP"

        // AudioPlayer notification ID
        private const val NOTIFICATION_ID = 101

        // Binder give clients
        private var iBinder : IBinder = LocalBinder()

        private var mediaPlayer: MediaPlayer? = null
        // Path to the audio file
//        private var mediaFile: String? = null

        // Used to pause/resume MediaPlayer
        private var resumePosition: Int = 0

        private var audioManager: AudioManager? = null

        // Handle incoming phone calls
        private var ongoingCall = false
        private var phoneStateListener: PhoneStateListener? = null
        private var telephonyManager: TelephonyManager? = null

        // List of available Audio files
        private var audioList: ArrayList<Audio>? = null
        private var audioIndex = -1
        private var activeAudio: Audio? = null // An object of the currently playing audio
    }

    // MediaSession
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.let { mediaPlayer ->
            // Set up MediaPlayer event listener
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnErrorListener(this)
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnBufferingUpdateListener(this)
            mediaPlayer.setOnSeekCompleteListener(this)
            mediaPlayer.setOnInfoListener(this)
            // Reset so that the MediaPlayer is not pointing to another data source
            mediaPlayer.reset()

//        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                activeAudio?.uri?.let { uri ->
                    mediaPlayer.setDataSource(applicationContext, uri)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                stopSelf()
            }
            mediaPlayer.prepareAsync()
        }
    }

    private fun initMediaSession() {
        if (mediaSessionManager != null) return // mediaSessionManager exists

//        mediaSessionManager = MediaSessionManager.getSessionManager(applicationContext)
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        // Get MediaSessions transport controls
        transportControls = mediaSession?.controller?.transportControls
        // Set MediaSession -> ready to receive media commands
        mediaSession?.isActive = true
        // Indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        // Set mediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mediaSession?.setCallback(object: MediaSessionCompat.Callback() {
            // Implement callback

            override fun onPlay() {
                super.onPlay()
                resumeMedia()
                NotificationUtil.buildNotification(applicationContext, mediaSession, activeAudio, PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()
                pauseMedia()
                NotificationUtil.buildNotification(applicationContext, mediaSession, activeAudio, PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                skipToNext()
                updateMetaData()
                NotificationUtil.buildNotification(applicationContext, mediaSession, activeAudio, PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                skipToPrevious()
                NotificationUtil.buildNotification(applicationContext, mediaSession, activeAudio, PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
                NotificationUtil.removeNotification(applicationContext)
                // Stop the service
                stopSelf()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
            }
        })
    }

    private fun updateMetaData() {
        val albumArt = BitmapFactory.decodeResource(resources, R.drawable.ic_album_black_24dp)
        // Update the current metadata
        // Update the current metadata
        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio?.artistKey)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio?.albumKey)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio?.title)
                .build()
        )
    }

    private fun skipToNext() {
        audioList?.let {audioList ->
            activeAudio = when(audioIndex) {
                // If last in playlist
                audioList.size - 1 -> {
                    audioIndex = 0
                    audioList[audioIndex]
                }
                // Get next in playlist
                else -> audioList[++audioIndex]
            }

            // Update stored index
            StorageUtil(applicationContext).storeAudioIndex(audioIndex)

            stopMedia()
            // Reset mediaPlayer
            mediaPlayer?.reset()
            initMediaPlayer()
        }

    }

    private fun skipToPrevious() {
        audioList?.let {audioList ->
            activeAudio = when(audioIndex) {
                // If first in playlist
                0 -> {
                    audioIndex = audioList.size - 1
                    audioList[audioIndex]
                }
                // Get previous in playlist
                else -> audioList[--audioIndex]
            }

            // Update stored index
            StorageUtil(applicationContext).storeAudioIndex(audioIndex)

            stopMedia()
            // Reset mediaPlayer
            mediaPlayer?.reset()
            initMediaPlayer()
        }

    }

    private fun playMedia() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                sendBroadcast(Intent(PlayerActivity.PLAY))
            }
        }
    }

    private fun stopMedia() {
        mediaPlayer?.let {
            if (it.isPlaying){
                it.stop()
                sendBroadcast(Intent(PlayerActivity.PAUSE))
            }
        }
    }

    private fun pauseMedia() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                resumePosition = it.currentPosition
                sendBroadcast(Intent(PlayerActivity.PAUSE))
            }
        }
    }

    private fun resumeMedia() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.seekTo(resumePosition)
                it.start()
                sendBroadcast(Intent(PlayerActivity.PLAY))
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return iBinder
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        // Invoked indicating buffering status of
        // a media resource being streamed over the network
    }

    override fun onCompletion(mp: MediaPlayer?) {
        // Invoked when playback of a media source has completed
        stopMedia()
        // stop the service
        stopSelf()
    }

    // Handle errors
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        // Invoked when there has been an error during an asynchronous operation
        when(what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK
                    -> Log.d(TAG, "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra")
            MediaPlayer.MEDIA_ERROR_SERVER_DIED
                    -> Log.d(TAG, "MEDIA ERROR SERVER DIED $extra")
            MediaPlayer.MEDIA_ERROR_UNKNOWN
                    -> Log.d(TAG, "MEDIA ERROR UNKNOWN $extra")
        }
        return false
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        // Invoked to communicate some info
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        // Invoked when the media source is ready to playback
        playMedia()
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        // Invoked indicating the completion of a seek operation
    }

    override fun onAudioFocusChange(focusState: Int) {
        // invoked when tha udio focus of the system is updated
        when(focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mediaPlayer == null) initMediaPlayer()
                else if (!mediaPlayer!!.isPlaying) mediaPlayer!!.start()
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media payer
                mediaPlayer?.let {it ->
                    if(it.isPlaying) it.stop()
                    it.release()
                    mediaPlayer = null
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                mediaPlayer?.let { if (it.isPlaying) it.pause() }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                mediaPlayer?.let {
                    if(it.isPlaying) it.setVolume(0.1f, 0.1f)
                }
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        audioManager?.let {
            val result = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setWillPauseWhenDucked(true)
                        .setOnAudioFocusChangeListener(this)
                        .build()
                    it.requestAudioFocus(audioFocusRequest)
                }
                else -> {
                    it.requestAudioFocus(
                        this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                    )
                }
            }
            // Focus gained
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return true
        }
        // Could not fain focus
        return false
    }

    private fun removeAudioFocus(): Boolean {
//        return when {
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
//                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
//                    setAudioAttributes(AudioAttributes.Builder().run {
//                        setUsage(AudioAttributes.USAGE_MEDIA)
//                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        build()
//                    })
//                    setAcceptsDelayedFocusGain(true)
//                    build()
//                }
//                AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager?.abandonAudioFocusRequest(focusRequest)
//            }
//            else -> AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager?.abandonAudioFocus(this)
//        }
        return true
    }

    // The system call this method when an activity, requests the service be started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            // An audio file is passed to the service through putExtra()
//            mediaFile = intent?.extras?.getString("media")
            // Load data from SharedPreferences
            val storage = StorageUtil(applicationContext)
            audioList = storage.loadAudio()
            audioIndex = storage.loadAudioIndex()

            audioList?.let { audioList->
                if (audioIndex != -1 && audioIndex < audioList.size) {
                    // index is in a valid range
                    activeAudio = audioList[audioIndex]
                } else {
                    stopSelf()
                }
            }
        } catch (e: NullPointerException) {
            stopSelf()
        }

        // Request audio focus
        if (!requestAudioFocus()) {
            // Could not gain focus
            stopSelf()
        }

//        if (!mediaFile.isNullOrBlank()) {
//            initMediaPlayer()
//        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession()
                initMediaPlayer()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }
            NotificationUtil.buildNotification(applicationContext, mediaSession, activeAudio, PlaybackStatus.PLAYING)
        }

        // Handle Intent action from MediaSession.TransportControls
        if (intent != null) handleIncomingActions(intent.action)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener()
        // ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver()
        // Listen for new Audio to play -- BroadcastReceiver
        registerPlayNewAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            stopMedia()
            it.release()
        }
        removeAudioFocus()

        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }

        NotificationUtil.removeNotification(applicationContext)

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver)
        unregisterReceiver(playNewAudio)

        //clear cached playlist
        StorageUtil(getApplicationContext()).clearCachedAudioPlaylist()
    }

    class LocalBinder: Binder() {
        fun getService(): MediaPlayerService {
            return MediaPlayerService()
        }
    }

    //Becoming noisy
    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia()
            NotificationUtil.buildNotification(applicationContext, mediaSession, activeAudio, PlaybackStatus.PAUSED)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    //Handle incoming phone calls
    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING -> if (mediaPlayer != null) {
                        pauseMedia()
                        ongoingCall = true
                    }
                    TelephonyManager.CALL_STATE_IDLE ->                   // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeMedia()
                            }
                        }
                }
            }
        }
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager?.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )
    }

    private val playNewAudio: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            // Get the new media index form SharedPreferences
            audioIndex = StorageUtil(applicationContext).loadAudioIndex()
            audioList?.let { audioList ->
                if (audioIndex != -1 && audioIndex < audioList.size) {
                    // index is in a valid range
                    activeAudio = audioList[audioIndex]
                } else {
                    stopSelf()
                }
            }

            // A PLAY_NEW_AUDIO action received
            // reset mediaPlayer to play the new Audio
            stopMedia()
            mediaPlayer?.reset()
            initMediaPlayer()
            updateMetaData()
            NotificationUtil.buildNotification(applicationContext, mediaSession, activeAudio, PlaybackStatus.PLAYING)
        }
    }

    private fun registerPlayNewAudio() {
        // Register playNewMedia receiver
        val filter = IntentFilter(PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }

    private fun handleIncomingActions(action: String?) {
        when {
            ACTION_PLAY.equals(action, true) -> transportControls?.play()
            ACTION_PAUSE.equals(action, true) -> transportControls?.pause()
            ACTION_NEXT.equals(action, true) -> transportControls?.skipToNext()
            ACTION_PREVIOUS.equals(action, true) -> transportControls?.skipToPrevious()
            ACTION_STOP.equals(action, true) -> transportControls?.stop()
            else -> Log.d(TAG, "Action not registered: $action")
        }
    }
}