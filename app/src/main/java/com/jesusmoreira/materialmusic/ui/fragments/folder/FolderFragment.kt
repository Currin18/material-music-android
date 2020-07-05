package com.jesusmoreira.materialmusic.ui.fragments.folder

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.FolderRecyclerViewAdapter
import com.jesusmoreira.materialmusic.controllers.MediaController
import com.jesusmoreira.materialmusic.models.Audio
import com.jesusmoreira.materialmusic.models.Folder
import com.jesusmoreira.materialmusic.ui.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_folder.view.*
import java.io.File
import java.io.FilenameFilter

class FolderFragment : Fragment(), FolderListener {

    companion object {
        private const val TAG: String = "FolderFragment"
    }

    private lateinit var viewModel: FolderViewModel
    private var folderAdapter: FolderRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this).get(FolderViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_folder, container, false)

        folderAdapter = FolderRecyclerViewAdapter(Folder(), this)

        with(view?.listFolders as RecyclerView) {
            adapter = folderAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.folder.observe(viewLifecycleOwner, Observer {
            folderAdapter?.folder = it
            folderAdapter?.notifyDataSetChanged()

            with(it.path.replace(Folder.DEFAULT_PATH, "")) {
                view.text_folders.text = when (this) {
                    "" -> "/"
                    else -> this
                }
            }

            with("${it.folderList.size} folders and ${it.fileList.size} files") {
                view.info_folders.text = this
            }
        })

        viewModel.folder.postValue(com.jesusmoreira.materialmusic.models.Folder())

        return view
    }

    override fun onClickFolder(folder: String) {
        val pathList = viewModel.pathList.value

        Log.d(TAG, "tag: $folder")
        if (folder == "..") {
            pathList?.remove(pathList.last())
        } else {
            pathList?.add(folder)
        }

        var route = ""
        pathList?.forEach {
            route += "$it/"
        }

        viewModel.pathList.postValue(pathList)

        viewModel.folder.postValue(Folder("${Folder.DEFAULT_PATH}/$route"))
    }

    override fun onClickFile(position: Int) {
        context?.let { context ->
            MediaController(context).apply {
                viewModel.folder.value?.let {
                    val musicList = getMusicListFromFolder(it)
                    val musicIndex = musicList.indexOfFirst { item -> item.displayName == it.fileList[position]}
                    if (activity is MainActivity) {
                        (activity as MainActivity).onSongClicked(musicList, musicIndex)
                    }
                }
            }
        }
    }
}