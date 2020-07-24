package com.jesusmoreira.materialmusic.ui.fragments.albums

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.SongRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.ui.activities.AlbumDetailActivity
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.GraphicUtil
import kotlinx.android.synthetic.main.album_detail_fragment.view.*

class AlbumDetailFragment : Fragment() {

    companion object {
        private const val ARG_ALBUM = "ARG_ALBUM"

        fun newInstance(album: Album? = null) =
            AlbumDetailFragment().apply {
                arguments = Bundle().apply {
                    if (album != null) putSerializable(ARG_ALBUM, album)
                }
            }
    }

    private lateinit var viewModel: AlbumDetailViewModel
    private var listener: SongListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(AlbumDetailViewModel::class.java)

        arguments?.apply {
            viewModel.album = getSerializable(ARG_ALBUM) as Album
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.album_detail_fragment, container, false)

        val actionBar = (activity as? AlbumDetailActivity)
            ?.supportActionBar

        viewModel.album?.apply {
            actionBar?.title = album

            val bitmap = context?.let { getAlbumArtBitmap(it, 500, 500) }
            if (bitmap != null) {
                view.albumArt.setImageBitmap(bitmap)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val mutableCopy = bitmap.copy(Bitmap.Config.RGB_565, true)
                    if (mutableCopy != null) {
                        GraphicUtil.getColorFromPalette(
                            GraphicUtil.getPaletteFromBitmap(mutableCopy)
                        )?.let {
                            view.detailLayout.setBackgroundColor(it)

                            actionBar?.setBackgroundDrawable(ColorDrawable(it))
//                            actionBar?.elevation = 0f
//                            activity?.actionBar?.setBackgroundDrawable(ColorDrawable(it))
                        }
                    }
                }
            } else {
                context?.let {
                    GraphicUtil.getBitmapFromVectorDrawable(it, R.drawable.ic_album_black_24dp)
                }?.let {
                    view.albumArt.setImageBitmap(it)
                }
            }

            view.albumArtistText.text = artist
            ("$numberOfSongs " + when(numberOfSongs) {
                1 -> "song"
                else -> "songs"
            }).let { view.albumSongsText.text = it }
            view.albumTimeText.text = ""
            view.albumYearText.text = "$firstYear"

            context?.let{ MediaController(it).getMusicListFromAlbum(this) }?.let { audioList ->
                viewModel.audioList = audioList
                val durationTotal = audioList.sumBy { it.duration?.toInt() ?: 0 } / 1000
                val durationHour = (durationTotal/60/60).let {
                    when {
                        it > 0 -> "$it:"
                        else -> ""
                    }
                }
                val durationMinutes = (durationTotal/60%60).let {
                    when {
                        it == 0 -> "00:"
                        it < 10 -> "0$it:"
                        else -> "$it:"
                    }
                }
                val durationSeconds = (durationTotal%60).let {
                    when {
                        it == 0 -> "00"
                        it < 10 -> "0$it"
                        else -> "$it"
                    }
                }
                view.albumTimeText.text = ("$durationHour$durationMinutes$durationSeconds")

                // Set the adapter
                if (view.listSongs is RecyclerView) {
                    with(view.listSongs) {
                        layoutManager = LinearLayoutManager(context)
                        adapter = SongRecyclerViewAdapter(context, audioList, listener)
                    }
                }
            }
        }

        return view
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(AlbumDetailViewModel::class.java)
//
//        savedInstanceState?.apply {
//            viewModel.album = getSerializable(ARG_ALBUM) as Album
//        }
//    }

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
