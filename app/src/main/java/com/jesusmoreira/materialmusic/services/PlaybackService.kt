package com.jesusmoreira.materialmusic.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import java.io.IOException
import java.lang.ref.WeakReference


/**
 * Service:
 * Note that this is designed for playback of files located in the assets folder. You'd need to modify the 'setDataSource' method
 * To enable other playback options
 */
class PlaybackService: Service() {

    companion object {
        private const val TAG = "PlabackService"

        private const val FOCUS_CHANGE = 10
        private const val FADE_DOWN = 11
        private const val FADE_UP = 12
        private const val SERVER_DIED = 13
    }

    private val binder: IBinder = ServiceStub(this)

    private var mediaPlayback: MediaPlayback? = null

    private var audioManager: AudioManager? = null

    private var mediaPlayerHandler: MediaPlaybackHandler? = null

    private val audioFocusListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            mediaPlayerHandler?.obtainMessage(FOCUS_CHANGE, focusChange, 0)?.sendToTarget()
        }

    private var pauseByTransientLossOfFocus = false
    private var isSupposedToBePlaying = false

    override fun onCreate() {
        super.onCreate()

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt the UI.
        val thread: HandlerThread = HandlerThread("MediaPlaybackHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        // Initialize the handlers
        mediaPlayerHandler = MediaPlaybackHandler(this, thread.looper)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        Log.i(TAG, "MediaPlayback class instantiated")
        mediaPlayback = MediaPlayback()
        mediaPlayback?.handler = mediaPlayerHandler
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.w(TAG, "Destroying service")
        mediaPlayback?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    fun stop() {
        // TODO: Fade down nicely

        synchronized(this) {
            mediaPlayback?.let {
                if (it.isPlayerInitialised) it.stop()
            }
        }
    }

    fun play() {
        synchronized(this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager?.requestAudioFocus(
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setOnAudioFocusChangeListener(audioFocusListener)
                        .build()
                )
            } else {
                audioManager?.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }
        }
    }

    fun pause() {
        // TODO: Fade down nicely

        synchronized(this) {
            mediaPlayerHandler?.removeMessages(FADE_UP)
            if (isSupposedToBePlaying) {
                mediaPlayback?.pause()
                isSupposedToBePlaying = false
                pauseByTransientLossOfFocus = false
            }
        }
    }

    fun openFile(path: String?): Boolean {
        synchronized(this) {
            if (path == null) return false

            mediaPlayback?.let {
                it.setDataSource(path)
                if (it.isPlayerInitialised)
                    return true
            }
            stop()
            return false
        }
    }

    fun getDuration(): Long {
        synchronized(this) {
            mediaPlayback?.let {
                if (it.isPlayerInitialised) return it.getDuration()
            }
        }
        return -1
    }

    fun getPosition(): Long {
        synchronized(this) {
            mediaPlayback?.let {
                if (it.isPlayerInitialised) return it.getPosition()
            }
        }
        return 0
    }

    fun seek(position: Long) {
        synchronized(this) {
            mediaPlayback?.let {
                if (it.isPlayerInitialised) {
                    it.seek(when {
                        position < 0 -> 0
                        position > it.getDuration() -> it.getDuration()
                        else -> position
                    })
                }
            }
        }
    }

    /**
     * Provides an interface for dealing with playback of audio files
     */
    inner class MediaPlayback: MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        private var player: MediaPlayer? = MediaPlayer()
        var handler: Handler? = null
        var isPlayerInitialised = false

        var volume: Float = 0f
            set(volume) {
                player?.setVolume(volume, volume)
                 field = volume
            }

        fun start() {
            player?.start()
        }

        fun stop() {
            player?.reset()
            isPlayerInitialised = false
        }

        /**
         * You CANNOT use this player after calling release
         */
        fun release() {
            stop()
            player?.release()
        }

        fun pause() {
            player?.pause()
        }

        fun getDuration(): Long = when {
            player != null && isPlayerInitialised -> player!!.duration.toLong()
            else -> -1
        }

        fun getPosition(): Long = when {
            player != null && isPlayerInitialised -> player!!.currentPosition.toLong()
            else -> 0
        }

        fun seek(whereTo: Long) { player?.seekTo(whereTo.toInt()) }

        fun setDataSource(path: String) {
            player?.let {
                isPlayerInitialised = setDataSource(it, path)
            }
        }

        private fun setDataSource(mediaPlayer: MediaPlayer, path: String): Boolean {
            try {
//                val afd: AssetFileDescriptor = assets.openFd(path)
                mediaPlayer.reset()
                // TODO: implement OnPreparedListener
                mediaPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener {
                    start()
                })
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
//                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
//                afd.close()
                mediaPlayer.setDataSource(applicationContext, Uri.parse(path))
//                mediaPlayer.prepare()
                mediaPlayer.prepareAsync()

                mediaPlayer.setOnCompletionListener(this)
                mediaPlayer.setOnErrorListener(this)

            } catch (e: IOException) {
                e.printStackTrace()
                return false
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
                return false
            } catch (ex: SecurityException) {
                ex.printStackTrace()
                return false
            }
            return true
        }

        override fun onCompletion(mp: MediaPlayer?) {
            player?.release()
            player = null
        }

        override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
            Log.e(TAG, "Error: $what")

            when(what) {
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                    isPlayerInitialised = false
                    player?.release()
                    player = MediaPlayer()

                    handler?.let {
                        it.sendMessageDelayed(it.obtainMessage(SERVER_DIED), 2000)
                    }
                    return true
                }
            }

            return false
        }
    }

    inner class MediaPlaybackHandler(service: PlaybackService, looper: Looper) :
        android.os.Handler(looper) {

        private var _service: WeakReference<PlaybackService>? = null
        private var currentVolume = 0.8f

        init {
            _service = WeakReference(service)
            _service?.get()?.mediaPlayback?.let { mediaPlayback ->
                currentVolume = mediaPlayback.volume
            }
        }

        override fun handleMessage(msg: Message) {
            val service: PlaybackService = _service?.get() ?: return

            when(msg.what) {
                FOCUS_CHANGE -> {
                    when(msg.arg1) {
                        FADE_DOWN -> {
                            currentVolume -= .05f
                            if (currentVolume > .2f) {
                                sendEmptyMessageDelayed(FADE_DOWN, 10)
                            } else {
                                currentVolume = .2f
                            }
                            service.mediaPlayback?.volume = currentVolume
                        }
                        FADE_UP -> {
                            // TODO: Only fade up to original volume
                            currentVolume += .01f
                            if (currentVolume < 1.0f) {
                                sendEmptyMessageDelayed(FADE_UP, 10)
                            } else {
                                currentVolume = 1.0f
                            }
                            service.mediaPlayback?.volume = currentVolume
                        }
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            if (service.isSupposedToBePlaying) {
                                service.pauseByTransientLossOfFocus = false
                            }
                            service.pause()
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            removeMessages(FADE_UP)
                            sendEmptyMessage(FADE_DOWN)
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            if (service.isSupposedToBePlaying) {
                                service.pauseByTransientLossOfFocus = true
                            }
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            if (!service.isSupposedToBePlaying
                                && service.pauseByTransientLossOfFocus) {
                                service.pauseByTransientLossOfFocus = false
                                currentVolume = 0f
                                service.mediaPlayback?.volume = currentVolume
                                service.play()
                            } else {
                                removeMessages(FADE_DOWN)
                                sendEmptyMessage(FADE_UP)
                            }
                        }
                        else -> {
                            // Log.d(TAG, "Unknown audio focus change code")
                        }
                    }
                }
            }
        }

    }

    private inner class ServiceStub(service: PlaybackService) : IPlaybackService.Stub() {
        private var _service: WeakReference<PlaybackService>? = null

        init {
            _service = WeakReference(service)
        }

        override fun stop() {
            _service?.get()?.stop()
        }

        override fun play() {
            _service?.get()?.play()
        }

        override fun pause() {
            _service?.get()?.pause()
        }

        @Throws(RemoteException::class)
        override fun openFile(path: String): Boolean {
            return _service?.get()?.openFile(path) ?: false
        }

        override fun getDuration(): Long {
            return _service?.get()?.getDuration() ?: 0L
        }

        override fun getPosition(): Long {
            return _service?.get()?.getPosition() ?: 0L
        }

        override fun seek(position: Long) {
            _service?.get()?.seek(position)
        }

        override fun isPlaying(): Boolean {
            return _service?.get()?.isSupposedToBePlaying ?: false
        }
    }
}