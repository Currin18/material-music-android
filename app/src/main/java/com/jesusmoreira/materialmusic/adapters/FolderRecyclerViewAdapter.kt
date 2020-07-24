package com.jesusmoreira.materialmusic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.models.Folder
import com.jesusmoreira.materialmusic.ui.fragments.folder.FolderListener

class FolderRecyclerViewAdapter(
    var folder: Folder,
    val listener: FolderListener?
): RecyclerView.Adapter<FolderRecyclerViewAdapter.ViewHolder>() {

    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            if (v.tag is String) {
                listener?.onClickFolder(v.tag as String)
            } else if (v.tag is Int) {
                listener?.onClickFile(v.tag as Int)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_folder, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = folder.folderList.size + folder.fileList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.header.visibility = View.VISIBLE
            holder.header.text = "Folders"
        } else if (position >= folder.folderList.size && position - folder.folderList.size == 0) {
            holder.header.visibility = View.VISIBLE
            holder.header.text = "Files"
        } else {
            holder.header.visibility = View.GONE
        }

        holder.itemIcon.setImageResource(when {
            position < folder.folderList.size -> R.drawable.ic_folder_black_24dp
            else -> R.drawable.ic_audiotrack_white_24dp
        })

        holder.itemText.text = when {
            position < folder.folderList.size -> folder.folderList[position]
            else -> folder.fileList[position - folder.folderList.size]
        }

        with(holder.view) {
            tag = when {
                position < folder.folderList.size -> folder.folderList[position]
                else -> position - folder.folderList.size
            }
            setOnClickListener(onClickListener)
        }
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.findViewById(R.id.header)
        val itemIcon: ImageView = view.findViewById(R.id.item_icon)
        val itemText: TextView = view.findViewById(R.id.item_text)
    }
}