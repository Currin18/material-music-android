package com.jesusmoreira.materialmusic.ui.player

import android.content.Context
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jesusmoreira.materialmusic.R
import kotlinx.android.synthetic.main.fragment_player.view.*

/**
 * A placeholder fragment containing a simple view.
 */
class PlayerFragment() : Fragment() {

    companion object {
        var listener: PlayerListener? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_player, container, false)
        view.playOrPause.setOnClickListener {
            listener?.onPlayOrPause()
        }
        return view
    }

    override fun onAttach(context: Context) {
        if (context is PlayerListener) listener = context
        super.onAttach(context)
    }

    interface PlayerListener {
        fun onPlayOrPause()
    }
}
