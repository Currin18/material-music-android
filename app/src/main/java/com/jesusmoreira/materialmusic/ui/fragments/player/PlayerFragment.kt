package com.jesusmoreira.materialmusic.ui.fragments.player

import android.content.Context
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
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.models.ShuffleStatus
import com.jesusmoreira.materialmusic.utils.GraphicUtil
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.view.*
import kotlinx.android.synthetic.main.item_list.view.*

/**
 * A placeholder fragment containing a simple view.
 */
class PlayerFragment : Fragment() {

    companion object {
        private const val TAG: String = "PlayerFragment"

        private const val ARG_AUDIO_LIST: String = "ARG_AUDIO_LIST"
        private const val ARG_AUDIO_INDEX: String = "ARG_AUDIO_INDEX"
        private const val ARG_MINIMIZED: String = "ARG_MINIMIZED"
        private const val ARG_SHUFFLE: String = "ARG_SHUFFLE"
        private const val ARG_REPEAT: String = "ARG_REPEAT"

        private var audioList: ArrayList<Audio>? = null
        private var audioIndex: Int = 0

        fun newInstance(
            audioList: ArrayList<Audio>,
            audioIndex: Int,
            minimized: Boolean = false,
            shuffle: Int = 0,
            repeat: Int = 0
        ): PlayerFragment {
            val args = Bundle()
            args.putString(ARG_AUDIO_LIST, StorageUtil.audioListToString(audioList))
            args.putInt(ARG_AUDIO_INDEX, audioIndex)
            args.putBoolean(ARG_MINIMIZED, minimized)
            args.putInt(ARG_SHUFFLE, shuffle)
            args.putInt(ARG_REPEAT, repeat)

            val fragment = PlayerFragment()
            fragment.arguments = args

            return fragment
        }
    }

    private var isPlaying: Boolean? = null
    var isMinimized: Boolean = false
    var shuffleStatus: ShuffleStatus = ShuffleStatus.NO_SHUFFLE

    private var palette: Palette? = null

    private lateinit var viewModel: PlayerViewModel
    private var playerListener: PlayerListener? = null

    private val handler = Handler()
    private val runnableProgress = object: Runnable {
        override fun run() {
            playerListener?.onGetProgress()?.let { progress ->
                seekProgress.progress = progress
            }
            handler.postDelayed(this, 1000)
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

            if (containsKey(ARG_SHUFFLE))
                shuffleStatus = when (getInt(ARG_SHUFFLE)) {
                    1 -> ShuffleStatus.SHUFFLE
                    else -> ShuffleStatus.NO_SHUFFLE
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        view.shuffle.setOnClickListener {
            playerListener?.onChangeShuffle(when(shuffleStatus) {
                ShuffleStatus.NO_SHUFFLE -> ShuffleStatus.SHUFFLE
                ShuffleStatus.SHUFFLE -> ShuffleStatus.NO_SHUFFLE
            })
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

            shuffle.setImageResource(when (shuffleStatus) {
                ShuffleStatus.NO_SHUFFLE -> R.drawable.ic_shuffle_disabled_white_24dp
                ShuffleStatus.SHUFFLE -> R.drawable.ic_shuffle_white_24dp
            })

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
                                    smallPlayerBg.setBackgroundColor(backgroundColor)
                                    bigPlayerBg.setBackgroundColor(backgroundColor)
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

    fun play() {
        playOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
        miniPlayOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
    }

    fun pause() {
        playOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        miniPlayOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
    }

    private fun onPlayOrPause() {
        isPlaying = playerListener?.onPlayOrPause()
        when (isPlaying) {
            true -> play()
            false -> pause()
        }
    }

    private fun onSkipToPrevious() {
        playerListener?.onSkipToPrevious()
    }

    private fun onSkipToNext() {
        playerListener?.onSkipToNext()
    }

    private fun onSeekTo(progress: Int) {
        playerListener?.onSeekTo(progress)
    }

    fun minimize() {
        isMinimized = true
        view?.bigPlayer?.visibility = View.GONE
        view?.smallPlayer?.visibility = View.VISIBLE
    }

    fun maximize() {
        isMinimized = false
        view?.bigPlayer?.visibility = View.VISIBLE
        view?.smallPlayer?.visibility = View.GONE
    }
}
