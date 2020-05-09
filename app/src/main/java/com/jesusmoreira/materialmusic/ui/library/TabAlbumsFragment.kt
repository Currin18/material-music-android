package com.jesusmoreira.materialmusic.ui.library

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.AlbumRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController
import com.jesusmoreira.materialmusic.models.Album

/**
 * A simple [Fragment] subclass.
 * Use the [TabAlbumsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TabAlbumsFragment : Fragment() {

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AlbumsTabFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            TabAlbumsFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
            }
    }

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var listener: OnRecyclerClick? = null

    interface OnRecyclerClick {
        fun OnAlbumClicked(album: Album)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
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
}
