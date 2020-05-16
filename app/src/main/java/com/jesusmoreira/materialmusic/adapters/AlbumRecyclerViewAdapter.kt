package com.jesusmoreira.materialmusic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Album
import com.jesusmoreira.materialmusic.ui.fragments.albums.AlbumListener
import com.jesusmoreira.materialmusic.utils.GraphicUtil
import kotlinx.android.synthetic.main.item_list_album.view.*

class AlbumRecyclerViewAdapter(
    private val context: Context,
    private val arrayList: ArrayList<Album>,
    private val listener: AlbumListener?,
    private val small: Boolean = false
): RecyclerView.Adapter<AlbumRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            listener?.onAlbumClicked(arrayList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                if (small) R.layout.item_list_album_small else R.layout.item_list_album,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = arrayList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrayList[position].apply {
            val bitmap = this.getAlbumArtBitmap(context, 250, 250)
            if (bitmap != null) {
                holder.albumArt.setImageBitmap(bitmap)
            } else {
                val defaultBitmap = GraphicUtil.getBitmapFromVectorDrawable(context, R.drawable.ic_album_black_24dp)
                holder.albumArt.setImageBitmap(defaultBitmap)
            }

            holder.albumName.text = album
            val albumArtist = "$artist · $numberOfSongs " + when(numberOfSongs) {
                1 -> "song"
                else -> "songs"
            }
            holder.albumArtist.text = albumArtist

            with(holder.view) {
                tag = position
                setOnClickListener(onClickListener)
            }
        }
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val albumArt: ImageView = view.albumArt
        val albumName: TextView = view.albumName
        val albumArtist: TextView = view.albumArtist
    }
}