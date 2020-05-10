package com.jesusmoreira.materialmusic.ui.fragments.artists

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.ArtistRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController

/**
 * A simple [Fragment] subclass.
 * Use the [TabArtistsFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Activities containing this fragment MUST implement the
 * [ArtistListener] interface.
 */
class TabArtistsFragment : Fragment() {
    companion object {
        fun newInstance() = TabArtistsFragment()
    }

    private var listener: ArtistListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tab_artists, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ArtistRecyclerViewAdapter(context, MediaController(context).getArtistList(), listener)
            }
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ArtistListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ArtistListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
