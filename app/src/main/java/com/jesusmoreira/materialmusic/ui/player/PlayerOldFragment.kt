package com.jesusmoreira.materialmusic.ui.player

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Audio

class PlayerOldFragment : Fragment() {

    private var song: Audio? = null
    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            song = it.getSerializable(ARG_SONG) as Audio?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_player_old, container, false)
        val button: Button = root.findViewById(R.id.start)
        button.setOnClickListener {
            song?.path?.let {
                val myUri = Uri.parse("${song?.path}")
                val mediaPlayer: MediaPlayer = MediaPlayer()
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                context?.let { context ->
//                    mediaPlayer.setDataSource(context, myUri)
//                    if (mediaPlayer.isPlaying) {
//                        mediaPlayer.stop()
//
//                    } else {
//                        mediaPlayer.prepare()
//                        mediaPlayer.start()
//                    }
                }
            }
        }
        return root
    }

    companion object {
        const val ARG_SONG = "ARG_SONG"

        @JvmStatic
        fun newInstance(item: Audio?) =
            PlayerOldFragment().apply{
                arguments = Bundle().apply {
                    item?.apply {
                        putSerializable(ARG_SONG, item)
                    }
                }
            }

    }

}
