package com.jesusmoreira.materialmusic.ui.player

import android.app.PendingIntent
import android.content.*
import android.database.Cursor
import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.jesusmoreira.materialmusic.PlayerActivity
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.controllers.MediaPlayerService
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.utils.StorageUtil
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.view.*
import kotlinx.android.synthetic.main.fragment_player.view.playOrPause
import org.jetbrains.anko.imageResource

/**
 * A placeholder fragment containing a simple view.
 */
class PlayerFragment : Fragment() {

    companion object {
        private const val ARG_AUDIO_LIST: String = "ARG_AUDIO_LIST"
        private const val ARG_AUDIO_INDEX: String = "ARG_AUDIO_INDEX"
        private const val ARG_PLAY: String = "ARG_PLAY"

        const val PLAY: String = "com.jesusmoreira.materialmusic.PlayerActivity.PLAY"
        const val PAUSE: String = "com.jesusmoreira.materialmusic.PlayerActivity.PAUSE"
        const val STOP: String = "com.jesusmoreira.materialmusic.PlayerActivity.STOP"
        const val PREVIOUS: String = "com.jesusmoreira.materialmusic.PlayerActivity.PREVIOUS"
        const val NEXT: String = "com.jesusmoreira.materialmusic.PlayerActivity.NEXT"

        fun newInstance(audioList: ArrayList<Audio>, audioIndex: Int, play: Boolean): PlayerFragment {
            val args = Bundle()
            args.putString(ARG_AUDIO_LIST, Gson().toJson(audioList))
            args.putInt(ARG_AUDIO_INDEX, audioIndex)
            args.putBoolean(ARG_PLAY, play)

            val fragment = PlayerFragment()
            fragment.arguments = args

            return fragment
        }
    }

    var listener: PlayerListener? = null

    var audioList: ArrayList<Audio>? = null
    var audioIndex: Int = 0

    var isPlaying: Boolean? = null
    var isMinimized: Boolean = false

    private val updaterPlayButton: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                when {
                    intent.action.equals(PLAY) -> {
                        isPlaying = true
                        playOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
                    }
                    intent.action.equals(PAUSE) -> {
                        isPlaying = false
                        playOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
                    }
                    intent.action.equals(PAUSE) -> {
                        isPlaying = null
                        playOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
                    }
                    intent.action.equals(PREVIOUS) -> {
                        audioList?.let {
                            audioIndex = when (audioIndex) {
                                0 -> it.size
                                else -> --audioIndex
                            }
                        }
                    }
                    intent.action.equals(NEXT) -> {
                        audioList?.let {
                            audioIndex = when (audioIndex) {
                                it.size -> 0
                                else -> --audioIndex
                            }
                        }
                    }
                }
            }
        }
    }

    interface PlayerListener {
        fun setServiceBoundState(serviceBoundState: Boolean)
        fun getServiceBoundState(): Boolean
        fun bindService(service: Intent, flags: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getString(ARG_AUDIO_LIST)?.let {
            audioList = Gson().fromJson(
                it,
                object : com.google.gson.reflect.TypeToken<ArrayList<Audio>?>() {}.type
            )
        }
        arguments?.getInt(ARG_AUDIO_INDEX)?.let { audioIndex = it }
        val play = arguments?.getBoolean(ARG_PLAY)

        if (play == true) {
            onPlayOrPause()
        } else {
            isMinimized = true
        }
    }

    override fun onResume() {
        super.onResume()
        startBroadcast()
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(updaterPlayButton)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_player, container, false)

        view.bigPlayer.visibility = View.GONE
        view.smallPlayer.visibility = View.GONE

        audioList?.let {
            if (isMinimized) {
                view.smallPlayer.visibility = View.VISIBLE
            } else {
                view.bigPlayer.visibility = View.VISIBLE
            }
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
        return view
    }

    override fun onAttach(context: Context) {
        if (context is PlayerListener) listener = context
        super.onAttach(context)
    }

    private fun startBroadcast() {
        val filter = IntentFilter()
        filter.addAction(PLAY)
        filter.addAction(PAUSE)
        filter.addAction(PREVIOUS)
        filter.addAction(NEXT)
        context?.registerReceiver(updaterPlayButton, filter)
    }

    private fun playAudio(audioIndex: Int) {
        context?.let { context ->
            if (listener?.getServiceBoundState() != true) {
                // Store Serializable audioList to SharedPreferences
                val storage = StorageUtil(context)
                storage.storeAudio(audioList)
                storage.storeAudioIndex(audioIndex)

                val playerIntent = Intent(context, MediaPlayerService::class.java)
                context.startService(playerIntent)
                listener?.bindService(playerIntent, Context.BIND_AUTO_CREATE)
            } else {
                Toast.makeText(context, "Player Activity", Toast.LENGTH_SHORT).show()
                // Store the new audioIndex to SharedPreferences
                val storage = StorageUtil(context)
                storage.storeAudioIndex(audioIndex)

                // Service is active
                // Send media with BroadcastReceiver
                val broadcastIntent = Intent(MediaPlayerService.PLAY_NEW_AUDIO)
                context.sendBroadcast(broadcastIntent)
            }
        }
    }

    private fun onPlayOrPause() {
        isPlaying = when(isPlaying) {
            true -> {
                launchIntentToPlayer(MediaPlayerService.ACTION_PAUSE)
                false
            }
            false -> {
                launchIntentToPlayer(MediaPlayerService.ACTION_PLAY)
                true
            }
            else -> {
                playAudio(audioIndex)
                true
            }
        }
    }

    private fun onSkipToPrevious() {
        if (isPlaying != null) {
            launchIntentToPlayer(MediaPlayerService.ACTION_PREVIOUS)
        }
    }

    private fun onSkipToNext() {
        if (isPlaying != null) {
            launchIntentToPlayer(MediaPlayerService.ACTION_NEXT)
        }
    }

    private fun launchIntentToPlayer(action: String) {
        context?.let { context->
            val playbackAction = Intent(context, MediaPlayerService::class.java)
            playbackAction.action = action
            PendingIntent.getService(context, 0, playbackAction, 0).send()
        }
    }
}
