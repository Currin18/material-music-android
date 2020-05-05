package com.jesusmoreira.materialmusic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Audio
import kotlinx.android.synthetic.main.item_list.view.*

class PlayListRecyclerViewAdapter(private var playList: ArrayList<Audio>): RecyclerView.Adapter<PlayListRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView? = null
        var text: TextView? = null

        init {
            title = view.title
            text = view.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return playList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = playList[position]

        holder.title?.text = item.title
        val extraData = "${item.album ?: "unknown"} · ${item.artist ?: "unknown"}"
        holder.text?.text = extraData
    }
}