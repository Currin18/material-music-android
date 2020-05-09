package com.jesusmoreira.materialmusic.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.ui.library.TabAlbumsFragment
import kotlinx.android.synthetic.main.item_list_album.view.*

class AlbumRecyclerViewAdapter(val context: Context, val arrayList: ArrayList<Album>, val listener: TabAlbumsFragment.OnRecyclerClick?): RecyclerView.Adapter<AlbumRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val albumArt: ImageView = view.albumArt
        val albumName: TextView = view.albumName
        val albumArtist: TextView = view.albumArtist
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_album, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = arrayList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrayList[position].apply {
            this.getAlbumArtBitmap(context, 250, 250)?.let {
                holder.albumArt.setImageBitmap(it)
            }

            holder.albumName.text = album
            val albumArtist = "$artist · $numberOfSongs " + when(numberOfSongs) {
                1 -> "song"
                else -> "songs"
            }
            holder.albumArtist.text = albumArtist
        }
    }
}