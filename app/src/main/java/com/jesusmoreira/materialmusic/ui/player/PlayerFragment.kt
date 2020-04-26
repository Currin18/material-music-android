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
        const val SERVICE_STATE: String = "ServiceState"

        const val PLAY: String = "com.jesusmoreira.materialmusic.PlayerActivity.PLAY"
        const val PAUSE: String = "com.jesusmoreira.materialmusic.PlayerActivity.PAUSE"
        const val PREVIOUS: String = "com.jesusmoreira.materialmusic.PlayerActivity.PREVIOUS"
        const val NEXT: String = "com.jesusmoreira.materialmusic.PlayerActivity.NEXT"

        fun onNewInstance(audioList: ArrayList<Audio>, audioIndex: Int): PlayerFragment {
            // TODO: add audioList
            return PlayerFragment()
        }
    }

    var listener: PlayerListener? = null

    private var player: MediaPlayerService? = null

    var audioList: ArrayList<Audio>? = arrayListOf()
    var audioIndex: Int = 0

    var isPlaying: Boolean? = null

    // Binding this Client to AudioPlayer Service
    private val serviceConnection : ServiceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder : MediaPlayerService.LocalBinder = service as MediaPlayerService.LocalBinder
            player = binder.getService()
            listener?.setServiceBoundState(true)

            Toast.makeText(context, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            listener?.setServiceBoundState(false)
        }
    }

    private val updaterPlayButton: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                if (intent.action.equals(PLAY)) {
                    isPlaying = true
                    playOrPause.setImageResource(R.drawable.ic_pause_white_24dp)
                } else if (intent.action.equals(PAUSE)) {
                    isPlaying = false
                    playOrPause.setImageResource(R.drawable.ic_play_arrow_white_24dp)
                }
            }
        }
    }

    interface PlayerListener {
        fun setServiceBoundState(serviceBoundState: Boolean)
        fun getServiceBoundState(): Boolean
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadAudio()
        startBroadcast()
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.let {
            if (it.getServiceBoundState()) {
                serviceConnection.let { context?.unbindService(it) }
                // service is active
                player?.stopSelf()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_player, container, false)
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
//            playerIntent.putExtra("media", media)
                context.startService(playerIntent)
                serviceConnection.let {
                    context.bindService(playerIntent, it, Context.BIND_AUTO_CREATE)
                }
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

    private fun loadAudio() {
        val contentResolver: ContentResolver? = context?.contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor: Cursor? = contentResolver?.query(uri, null, selection, null, sortOrder)

        cursor?.let {
            if (it.count > 0) {
                audioList = arrayListOf()
                while (cursor.moveToNext()) {
                    audioList?.add(Audio(cursor))
                }
            }
            it.close()
        }
    }

    private fun onPlayOrPause() {
        isPlaying = when(isPlaying) {
            true -> {
                playOrPause.imageResource = R.drawable.ic_play_arrow_white_24dp
                launchIntentToPlayer(MediaPlayerService.ACTION_PAUSE)
                false
            }
            false -> {
                playOrPause.imageResource = R.drawable.ic_pause_white_24dp
                launchIntentToPlayer(MediaPlayerService.ACTION_PLAY)
                true
            }
            else -> {
                playAudio(audioIndex)
                playOrPause.imageResource = R.drawable.ic_pause_white_24dp
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
