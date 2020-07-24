package com.jesusmoreira.materialmusic.ui.fragments.artists

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.AlbumRecyclerViewAdapter
import com.jesusmoreira.materialmusic.adapters.SongRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController
import com.jesusmoreira.materialmusic.models.Artist
import com.jesusmoreira.materialmusic.ui.activities.ArtistDetailActivity
import com.jesusmoreira.materialmusic.ui.fragments.albums.AlbumListener
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener
import com.jesusmoreira.materialmusic.utils.GraphicUtil
import kotlinx.android.synthetic.main.artist_detail_fragment.view.*

class ArtistDetailFragment : Fragment() {

    companion object {

        private const val ARG_ARTIST = "ARG_ARTIST"

        fun newInstance(artist: Artist?) =
            ArtistDetailFragment().apply {
                arguments = Bundle().apply {
                    if (artist != null) putSerializable(ARG_ARTIST, artist)
                }
            }
    }

    private lateinit var viewModel: ArtistDetailViewModel
    private var songListener: SongListener? = null
    private var albumListener: AlbumListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ArtistDetailViewModel::class.java)

        arguments?.apply {
            viewModel.artist = getSerializable(ARG_ARTIST) as Artist
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.artist_detail_fragment, container, false)

        val actionBar = (activity as? ArtistDetailActivity)
            ?.supportActionBar

        context?.let { context ->

            viewModel.artist?.apply {
                actionBar?.title = artist

                viewModel.albumList = this.getAlbumes(context)

                val bitmap = getArtistArtBitmap(context, 500, 500)
                if (bitmap != null) {
                    view.artistArt.setImageBitmap(bitmap)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val mutableCopy = bitmap.copy(Bitmap.Config.RGB_565, true)
                        if (mutableCopy != null) {
                            GraphicUtil.getColorFromPalette(
                                GraphicUtil.getPaletteFromBitmap(mutableCopy)
                            )?.let {
                                view.detailLayout.setBackgroundColor(it)

                                actionBar?.setBackgroundDrawable(ColorDrawable(it))
                            }
                        }
                    }
                } else {
                    GraphicUtil.getBitmapFromVectorDrawable(context, R.drawable.ic_album_black_24dp)?.let {
                        view.artistArt.setImageBitmap(it)
                    }
                }

                ("$numberOfAlbums " + when (numberOfAlbums) {
                    1 -> "album"
                    else -> "albums"
                }).let { view.artistAlbumsText.text = it }
                ("$numberOfTracks " + when (numberOfTracks) {
                    1 -> "song"
                    else -> "songs"
                }).let { view.artistSongsText.text = it }

                viewModel.audioList = MediaController(context).getMusicListFromAlbumList(viewModel.albumList)
                val durationTotal = viewModel.audioList.sumBy { it.duration?.toInt() ?: 0 } / 1000
                val durationHour = (durationTotal / 60 / 60).let {
                    when {
                        it > 0 -> "$it:"
                        else -> ""
                    }
                }
                val durationMinutes = (durationTotal / 60 % 60).let {
                    when {
                        it == 0 -> "00:"
                        it < 10 -> "0$it:"
                        else -> "$it:"
                    }
                }
                val durationSeconds = (durationTotal % 60).let {
                    when {
                        it == 0 -> "00"
                        it < 10 -> "0$it"
                        else -> "$it"
                    }
                }
                view.artistTimeText.text = ("$durationHour$durationMinutes$durationSeconds")

                // Set the adapters
                if (view.listAlbums is RecyclerView) {
                    with(view.listAlbums) {
                        layoutManager = LinearLayoutManager(context).apply {
                            orientation = LinearLayoutManager.HORIZONTAL
                        }
                        adapter = AlbumRecyclerViewAdapter(context, viewModel.albumList, albumListener, true)
                    }
                }

                if (view.listSongs is RecyclerView) {
                    with(view.listSongs) {
                        layoutManager = LinearLayoutManager(context)
                        adapter = SongRecyclerViewAdapter(context, viewModel.audioList, songListener)
                    }
                }
            }
        }

        return view
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(ArtistDetailViewModel::class.java)
//        // TODO: Use the ViewModel
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SongListener) {
            songListener = context
        } else {
            throw RuntimeException("$context must implement SongListener")
        }

        if (context is AlbumListener) {
            albumListener = context
        } else {
            throw RuntimeException("$context must implement AlbumListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        songListener = null
        albumListener = null
    }

}
