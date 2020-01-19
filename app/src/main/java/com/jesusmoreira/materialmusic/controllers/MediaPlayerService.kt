package com.jesusmoreira.materialmusic.controllers

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioAttributes
import java.io.IOException
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioFocusRequest
import android.os.Build
import android.util.Log
import java.lang.NullPointerException


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

        // Binder give clients
        private var iBinder : IBinder = LocalBinder()

        private var mediaPlayer: MediaPlayer? = null
        //path to the audio file
        private var mediaFile: String? = null

        //Used to pause/resume MediaPlayer
        private var resumePosition: Int = 0

        private var audioManager: AudioManager? = null
    }

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
                // Set the data source to the mediaFile location
                mediaPlayer.setDataSource(mediaFile)
            } catch (e: IOException) {
                e.printStackTrace()
                stopSelf()
            }
            mediaPlayer.prepareAsync()
        }
    }

    private fun playMedia() {
        mediaPlayer?.let {
            if (it.isPlaying) it.start()
        }
    }

    private fun stopMedia() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
        }
    }

    private fun pauseMedia() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                resumePosition = it.currentPosition
            }
        }
    }

    private fun resumeMedia() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.seekTo(resumePosition)
                it.start()
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
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    })
                    setAcceptsDelayedFocusGain(true)
                    build()
                }
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager?.abandonAudioFocusRequest(focusRequest)
            }
            else -> AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager?.abandonAudioFocus(this)
        }
    }

    // The system call this method when an activity, requests the service be started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            // An audio file is passed to the service through putExtra()
            mediaFile = intent?.extras?.getString("media")
        } catch (e: NullPointerException) {
            stopSelf()
        }

        // Request audio focus
        if (!requestAudioFocus()) {
            // Could not gain focus
            stopSelf()
        }

        if (!mediaFile.isNullOrBlank()) {
            initMediaPlayer()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.let {
            stopMedia()
            it.release()
        }
        removeAudioFocus()
    }

    class LocalBinder: Binder() {
        fun getService(): MediaPlayerService {
            return MediaPlayerService()
        }
    }
}