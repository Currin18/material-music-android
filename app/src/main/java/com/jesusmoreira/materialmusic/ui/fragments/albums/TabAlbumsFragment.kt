package com.jesusmoreira.materialmusic.ui.fragments.albums

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.AlbumRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController

/**
 * A simple [Fragment] subclass.
 * Use the [TabAlbumsFragment.newInstance] factory method to
 * create an instance of this fragment.
 * Activities containing this fragment MUST implement the
 * [SongListener] interface.
 */
class TabAlbumsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = TabAlbumsFragment()
    }

    private var listener: AlbumListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tab_albums, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = GridLayoutManager(context, 2)
                adapter = AlbumRecyclerViewAdapter(context, MediaController(context).getAlbumList(), listener)
            }
        }

        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AlbumListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AlbumListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
