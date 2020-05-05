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


import com.jesusmoreira.materialmusic.ui.library.SongsTabFragment.OnSongListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_song_list_item.view.*

/**
 * [RecyclerView.Adapter] that can display a [Audio] and makes a call to the
 * specified [OnSongListFragmentInteractionListener].
 */
class SongRecyclerViewAdapter(
    private val context: Context,
    private val musicList: ArrayList<Audio>,
    private val mListener: OnSongListFragmentInteractionListener?
) : RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onSongClicked(musicList, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_song_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Audio = musicList[position]
        holder.titleView.text = item.title
        holder.artistView.text = item.artist

        holder.albumArtView.setImageBitmap(item.getAlbumArtBitmap(context, 100, 100))

        with(holder.mView) {
            tag = position
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = musicList.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val titleView: TextView = mView.title
        val artistView: TextView = mView.artist
        val albumArtView: ImageView = mView.image

        override fun toString(): String {
            return super.toString() + " '" + titleView.text + "'"
        }
    }
}
