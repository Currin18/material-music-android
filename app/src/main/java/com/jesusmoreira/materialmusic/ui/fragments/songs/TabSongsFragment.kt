package com.jesusmoreira.materialmusic.ui.fragments.songs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.SongRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController
import com.jesusmoreira.materialmusic.models.Audio

/**
 * A simple [Fragment] subclass.
 * Use the [TabSongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Activities containing this fragment MUST implement the
 * [SongListener] interface.
 */
class TabSongsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = TabSongsFragment()
    }

    private var listener: SongListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab_songs, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = SongRecyclerViewAdapter(context, MediaController(context).getMusicList(), listener)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SongListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SongListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
