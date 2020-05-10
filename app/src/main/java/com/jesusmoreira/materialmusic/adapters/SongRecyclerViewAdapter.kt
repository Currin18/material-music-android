package com.jesusmoreira.materialmusic.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.fragments.songs.SongListener

import com.jesusmoreira.materialmusic.utils.GraphicUtil

import kotlinx.android.synthetic.main.item_list_song.view.*

/**
 * [RecyclerView.Adapter] that can display a [Audio] and makes a call to the
 * specified [OnSongListFragmentInteractionListener].
 */
class SongRecyclerViewAdapter(
    private val context: Context,
    private val arrayList: ArrayList<Audio>,
    private val listener: SongListener?
) : RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            listener?.onSongClicked(arrayList, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_song, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        arrayList[position].apply {
            val bitmap = this.getAlbumArtBitmap(context, 100, 100)
            if (bitmap != null) {
                holder.albumArtView.setImageBitmap(bitmap)
            } else {
                val defaultBitmap = GraphicUtil.getBitmapFromVectorDrawable(context, R.drawable.ic_album_black_24dp)
                holder.albumArtView.setImageBitmap(defaultBitmap)
            }

            holder.titleView.text = title
            holder.artistView.text = artist

            with(holder.view) {
                tag = position
                setOnClickListener(onClickListener)
            }
        }
    }

    override fun getItemCount(): Int = arrayList.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val artistView: TextView = view.artist
        val albumArtView: ImageView = view.image

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }
}
