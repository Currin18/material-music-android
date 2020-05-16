package com.jesusmoreira.materialmusic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.ui.fragments.player.PlayerListener
import kotlinx.android.synthetic.main.item_list.view.*

class PlayListRecyclerViewAdapter(
    var playList: ArrayList<Audio>,
    var index: Int,
    private val listener: PlayerListener?
): RecyclerView.Adapter<PlayListRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val position = v.tag as Int
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            listener?.onSongClicked(playList, position, true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = playList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = playList[position]

        holder.dragView.visibility = View.GONE
        holder.imageText.apply {
            visibility = View.VISIBLE
            text = "${position - index}"
        }

        holder.title.text = item.title
        "${item.album ?: "unknown"} · ${item.artist ?: "unknown"}".let { holder.text.text = it }

        with(holder.view) {
            tag = position
            setOnClickListener(onClickListener)
        }
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val dragView: View = view.drag_view
        val imageText: TextView = view.image_text
        var title: TextView = view.title
        var text: TextView = view.text
    }
}