package com.jesusmoreira.materialmusic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Artist
import com.jesusmoreira.materialmusic.ui.fragments.artists.ArtistListener
import kotlinx.android.synthetic.main.item_list_artist.view.*

class ArtistRecyclerViewAdapter(
    private val context: Context,
    private val arrayList: ArrayList<Artist> = arrayListOf(),
    private val listener: ArtistListener?
): RecyclerView.Adapter<ArtistRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            listener?.onArtistClicked(arrayList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_artist, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = arrayList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrayList[position].apply {
            holder.artistName.text = artist
            val artistData = "$numberOfAlbums " + when(numberOfAlbums) {
                1 -> "album"
                else -> "albums"
            } + " · $numberOfTracks" + when(numberOfTracks) {
                1 -> "song"
                else -> "songs"
            }
            holder.artistData.text = artistData

            with(holder.view) {
                tag = position
                setOnClickListener(onClickListener)
            }
        }
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.image
        val artistName: TextView = view.artistName
        val artistData: TextView = view.artistData
    }
}