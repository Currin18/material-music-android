package com.jesusmoreira.materialmusic.ui.fragments.player

import android.app.PendingIntent
import android.content.*
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.PlayListRecyclerViewAdapter
import com.jesusmoreira.materialmusic.services.MediaPlayerService
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.GraphicUtil
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.view.*
import kotlinx.android.synthetic.main.item_list.view.*
import org.jetbrains.anko.backgroundColor

/**
 * A placeholder fragment containing a simple view.
 */
class PlayerFragment : Fragment() {

    companion object {
        private const val TAG: String = "PlayerFragment"

        private const val ARG_AUDIO_LIST: String = "ARG_AUDIO_LIST"
        private const val ARG_AUDIO_INDEX: String = "ARG_AUDIO_INDEX"
        private const val ARG_MINIMIZED: String = "ARG_MINIMIZED"

        const val PLAY: String = "com.jesusmoreira.materialmusic.ui.activities.PlayerActivity.PLAY"
        const val PAUSE: String = "com.jesusmoreira.materialmusic.ui.activities.PlayerActivity.PAUSE"
        const val STOP: String = "com.jesusmoreira.materialmusic.ui.activities.PlayerActivity.STOP"
        const val PREVIOUS: String = "com.jesusmoreira.materialmusic.ui.activities.PlayerActivity.PREVIOUS"
        const val NEXT: String = "com.jesusmoreira.materialmusic.ui.activities.PlayerActivity.NEXT"

        private var audioList: ArrayList<Audio>? = null
        private var audioIndex: Int = 0

        fun newInstance(audioList: ArrayList<Audio>, audioIndex: Int, minimized: Boolean): PlayerFragment {
            val args = Bundle()
            args.putString(ARG_AUDIO_LIST, StorageUtil.audioListToString(audioList))
            args.putInt(ARG_AUDIO_INDEX, audioIndex)
            args.putBoolean(ARG_MINIMIZED, minimized)

            val fragment = PlayerFragment()
            fragment.arguments = args

            return fragment
        }
    }

    private var isPlaying: Boolean? = null
    var isMinimized: Boolean = false

    var palette: Palette? = null

    private lateinit var viewModel: PlayerViewModel
    private var playerListener: PlayerListener? = null

    private val handler = Handler()
    private val runnableProgress = object: Runnable {
        override fun run() {
            playerListener?.onGetProgress()?.let { progress ->
//                    Log.d(TAG, "getProgress: $progress")
                seekProgress.progress = progress
//                    Log.d(TAG, "seekProgress: ${seekProgress.progress}")
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val updaterPlayButton: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                when {
//                    intent.action.equals(PLAY) -> {
//                        isPlaying = true
//                        playOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
//                        miniPlayOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
//                        handler.post(runnableProgress)
//                    }
//                    intent.action.equals(PAUSE) -> {
//                        isPlaying = false
//                        playOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
//                        miniPlayOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
//                        handler.removeCallbacksAndMessages(null)
//                    }
//                    intent.action.equals(STOP) -> {
//                        isPlaying = null
//                        playOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
//                    }
//                    intent.action.equals(PREVIOUS) -> {
//                        audioList?.let {
//                            audioIndex = if (intent.hasExtra("index")) {
//                                intent.getIntExtra("index", 0)
//                            } else {
//                                when (audioIndex) {
//                                    0 -> it.size - 1
//                                    else -> --audioIndex
//                                }
//                            }
//                        }
//
//                        view?.let { printView(it) }
//                    }
//                    intent.action.equals(NEXT) -> {
//                        audioList?.let {
//                            audioIndex = if (intent.hasExtra("index")) {
//                                intent.getIntExtra("index", 0)
//                            } else {
//                                when (audioIndex) {
//                                    it.size - 1 -> 0
//                                    else -> ++audioIndex
//                                }
//                            }
//                        }
//
//                        view?.let { printView(it) }
//                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            ViewModelProvider(this).get(PlayerViewModel::class.java)

        arguments?.apply {
            getString(ARG_AUDIO_LIST)?.let {
                audioList = StorageUtil.audioListFromString(it)
            }

            if (containsKey(ARG_AUDIO_INDEX))
                audioIndex = getInt(ARG_AUDIO_INDEX)

            if (containsKey(ARG_MINIMIZED))
                isMinimized = getBoolean(ARG_MINIMIZED)
        }



//        if (context != null) {
//            audioList = StorageUtil(requireContext()).loadAudio()
//            audioIndex = StorageUtil(requireContext()).loadAudioIndex()
//        }




//        if (play == true) {
//            onPlayOrPause()
//        } else {
//            isMinimized = true
//        }
    }

    override fun onResume() {
        super.onResume()
        startBroadcast()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(updaterPlayButton)
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_player, container, false)

//        setSupportActionBar(toolbar)

        printView(view)

        handler.post(runnableProgress)

        view.seekProgress.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    seekBar?.progress?.let { onSeekTo(it) }
//                    Toast.makeText(context, "seekbar progress: ${seekBar?.progress}, progress: $progress", Toast.LENGTH_LONG).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        view.minimizeButton.setOnClickListener {
            minimize()
        }

        view.playOrPause.setOnClickListener {
            onPlayOrPause()
        }
        view.skipToPrevious.setOnClickListener {
            onSkipToPrevious()
        }
        view.skipToNext.setOnClickListener {
            onSkipToNext()
        }

        view.smallPlayer.setOnClickListener {
            maximize()
        }
        view.miniPlayOrPause.setOnClickListener {
            onPlayOrPause()
        }
        return view
    }

    private fun printView(view: View) {
        with(view) {
            bigPlayer.visibility = View.GONE
            smallPlayer.visibility = View.GONE

            audioList?.get(audioIndex)?.let { audio ->
                if (isMinimized) {
                    smallPlayer.visibility = View.VISIBLE
                } else {
                    bigPlayer.visibility = View.VISIBLE
                }

                context?.let { context ->
                    val bitmap = audio.getAlbumArtBitmap(context, 500, 500)
                    bitmap?.let { albumImage.setImageBitmap(it) }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val mutableCopy = bitmap?.copy(Bitmap.Config.RGB_565, true)
                        if (mutableCopy != null) {
                            palette = GraphicUtil.getPaletteFromBitmap(mutableCopy)
                            palette?.let { palette ->
                                GraphicUtil.printPalette(palette)
                                val backgroundColor = GraphicUtil.getColorFromPalette(palette)
                                if (backgroundColor != null) {
                                    Log.d(TAG, "BackgroundColor: ${GraphicUtil.intToRGB(backgroundColor)}")
                                    smallPlayerBg.backgroundColor = backgroundColor
                                    bigPlayerBg.backgroundColor = backgroundColor
                                }

//                                val tintColor = palette.lightMutedSwatch?.rgb
//                                if (tintColor != null) {
//                                    shuffle.setColorFilter(tintColor)
//                                    skipToPrevious.setColorFilter(tintColor)
//                                }
                            }
                        }
                    }

                }

                seekProgress.progress = 0
                audio.duration?.let { seekProgress.max = (it).toInt() }

                title.text = audio.title
                mimiTitle.text = audio.title

                val extraData = "${audio.album ?: "unknown"} · ${audio.artist ?: "unknown"}"
                text.text = extraData
            }

            audioList?.let {
                val adapter = PlayListRecyclerViewAdapter(it, playerListener)
                val recycler = player_recycler_view as RecyclerView
                recycler.setHasFixedSize(true)
                recycler.layoutManager = LinearLayoutManager(context)
                recycler.adapter = adapter
                recycler.scrollToPosition(when {
                    audioIndex + 1 < it.size -> audioIndex + 1
                    else -> it.size - 1
                })
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is PlayerListener) {
            playerListener = context
        } else {
            throw RuntimeException("$context must implement PlayerListener")
        }
    }

    private fun startBroadcast() {
        val filter = IntentFilter()
        filter.addAction(PLAY)
        filter.addAction(PAUSE)
        filter.addAction(PREVIOUS)
        filter.addAction(NEXT)
        context?.registerReceiver(updaterPlayButton, filter)
    }

//    private fun playAudio(audioIndex: Int) {
//        context?.let { context ->
//            if (listener?.getServiceBoundState() != true) {
//                // Store Serializable audioList to SharedPreferences
//                val storage = StorageUtil(context)
//                storage.storeAudio(audioList)
//                storage.storeAudioIndex(audioIndex)
//
//                val playerIntent = Intent(context, MediaPlayerService::class.java)
//                context.startService(playerIntent)
//                listener?.bindService(playerIntent, Context.BIND_AUTO_CREATE)
//            } else {
//                Toast.makeText(context, "Player Activity", Toast.LENGTH_SHORT).show()
//                // Store the new audioIndex to SharedPreferences
//                val storage = StorageUtil(context)
//                storage.storeAudioIndex(audioIndex)
//
//                // Service is active
//                // Send media with BroadcastReceiver
//                val broadcastIntent = Intent(MediaPlayerService.PLAY_NEW_AUDIO)
//                context.sendBroadcast(broadcastIntent)
//            }
//        }
//    }

    private fun play() {
        playOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
        miniPlayOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
    }

    private fun pause() {
        playOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        miniPlayOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
    }

    private fun onPlayOrPause() {
//        isPlaying = when(isPlaying) {
//            true -> {
//                launchIntentToPlayer(MediaPlayerService.ACTION_PAUSE)
//                false
//            }
//            false -> {
//                launchIntentToPlayer(MediaPlayerService.ACTION_PLAY)
//                true
//            }
//            else -> {
//                playAudio(audioIndex)
//                true
//            }
//        }
        isPlaying = playerListener?.onPlayOrPause()
        when (isPlaying) {
            true -> play()
            false -> pause()
        }
    }

    private fun onSkipToPrevious() {
//        if (isPlaying != null) {
//            launchIntentToPlayer(MediaPlayerService.ACTION_PREVIOUS)
//        }
        playerListener?.onSkipToPrevious()
    }

    private fun onSkipToNext() {
//        if (isPlaying != null) {
//            launchIntentToPlayer(MediaPlayerService.ACTION_NEXT)
//        }
        playerListener?.onSkipToNext()
    }

    private fun onSeekTo(progress: Int) {
//        if (isPlaying != null) {
//            launchIntentToPlayer(MediaPlayerService.ACTION_SEEK, progress, progress)
//        }
        playerListener?.onSeekTo(progress)
    }

    private fun launchIntentToPlayer(action: String, requestCode: Int = -1, progress: Int? = null) {
        context?.let { context->
            val playbackAction = Intent(context, MediaPlayerService::class.java)
            playbackAction.action = action
            Log.d(TAG, "progress: $progress")
            progress?.let { playbackAction.putExtra(MediaPlayerService.ARG_SEEK_DURATION, progress) }
            PendingIntent.getService(context, requestCode, playbackAction, 0).send()
        }
    }

    fun minimize() {
        view?.bigPlayer?.visibility = View.GONE
        view?.smallPlayer?.visibility = View.VISIBLE
    }

    fun maximize() {
        view?.bigPlayer?.visibility = View.VISIBLE
        view?.smallPlayer?.visibility = View.GONE
    }
}
