package com.jesusmoreira.materialmusic.ui.library

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.SongRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController
import com.jesusmoreira.materialmusic.models.Audio

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [TabSongsFragment.OnSongListFragmentInteractionListener] interface.
 */
class TabSongsFragment : Fragment() {

    // TODO: Customize parameters
    private var columnCount = 1

    private var listener: OnSongListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        arguments?.let {
//            columnCount = it.getInt(ARG_COLUMN_COUNT)
//        }
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
        if (context is OnSongListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSongListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnSongListFragmentInteractionListener {
        fun onSongClicked(audioList: ArrayList<Audio>, position: Int)
    }

    companion object {

//        // TODO: Customize parameter argument names
//        const val ARG_COLUMN_COUNT = "column-count"
//

        @JvmStatic
        fun newInstance() =
            TabSongsFragment().apply {
//                arguments = Bundle().apply {
//                    putInt(ARG_COLUMN_COUNT, columnCount)
//                }
            }
    }
}
