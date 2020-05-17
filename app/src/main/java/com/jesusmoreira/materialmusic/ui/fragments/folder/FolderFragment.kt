package com.jesusmoreira.materialmusic.ui.fragments.folder

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jesusmoreira.materialmusic.R
import com.jesusmoreira.materialmusic.adapters.FolderRecyclerViewAdapter
import kotlinx.android.synthetic.main.fragment_folder.view.*
import java.io.File
import java.io.FilenameFilter

class FolderFragment : Fragment() {

    companion object {
        private const val TAG: String = "FolderFragment"
    }

    private lateinit var viewModel: FolderViewModel

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

//        var storage: File? = null
//        context?.let {
//            storage = StorageUtil.getExternalStorage(it)
//            storage = File(it.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "/")

//            val filepath =
//                Environment.getExternalStorageDirectory().path
//            val file = File("$filepath/AudioRecorder")
//            val file = File("/")

//            val sd = Environment.getExternalStorageDirectory()
            //This will return an array with all the Files (directories and files)
            //in the external storage folder
            //This will return an array with all the Files (directories and files)
            //in the external storage folder
//            val sdDirList = sd.listFiles()

//            val filterDirectoriesOnly =
//                FileFilter { pathname -> pathname?.isDirectory ?: false }
//            FileFilter() {
//                fun accept(file: File): Boolean {
//                    return file.isDirectory
//                }
//            }
//            val sdDirectories: Array<File> = sd.listFiles(filterDirectoriesOnly)

//            Log.d("FolderFragment", "$sdDirectories")
//        }

        viewModel.text.observe(viewLifecycleOwner, Observer {
            view.text_folders.text = it
        })

        viewModel.text.postValue(path.absolutePath
            .replace("/storage/emulated/0", "").let {
                when (it) {
                    "" -> "/"
                    else -> it
                }
            }
        )

        var folderAdapter: FolderRecyclerViewAdapter? = null

        val listener = View.OnClickListener { view ->
            with(view.tag as String) {
                Log.d(TAG, "tag: $this")
                if (this != "..")
                    pathList.add(this)
                else {
                    pathList.remove(pathList.last())
                }
            }

            var route = ""
            pathList.forEach {
                route += "$it/"
            }
            path = File("${Environment.getExternalStorageDirectory()}/$route")
            viewModel.text.postValue(path.absolutePath
                .replace("/storage/emulated/0", "").let {
                    when (it) {
                        "" -> "/"
                        else -> it
                    }
                }
            )

            loadFileList()?.let {
                folderAdapter?.items = it.apply { if (pathList.isNotEmpty()) add(0, "..") }
                folderAdapter?.notifyDataSetChanged()
            }
        }

        folderAdapter = loadFileList()?.let {
            FolderRecyclerViewAdapter(it.apply { if (pathList.isNotEmpty()) add(0, "..") }, listener)
        }

        with(view?.listFolders as RecyclerView) {
            adapter = folderAdapter
            layoutManager = LinearLayoutManager(context)
        }

        return view
    }

    private var pathList: ArrayList<String> = arrayListOf()
    private var fileList: Array<Item> = arrayOf()
    private var path = File("${Environment.getExternalStorageDirectory()}")

    private fun loadFileList(): ArrayList<String>? {
//        context?.let { path = File("${ContextCompat.getExternalFilesDirs(it, null)[0].absolutePath}/../../") }

//        path = File("/")

        Log.i(TAG, "path: ${path.absolutePath}")

        try {
            path.mkdirs()
        } catch (e: SecurityException) {
            Log.e(TAG, "unable to write on the sd card")
        }

        // Checks whether path exists
        if (path.exists()) {
            val filter = FilenameFilter { dir, name ->
                val sel = File(dir, name)
                (sel.isFile || sel.isDirectory) && !sel.isHidden
            }

            val fList: Array<String> = path.list(filter) ?: arrayOf()

            Log.i(TAG, "fList: ${fList.contentToString()}")

            return ArrayList(fList.toList())
        }

        return null
    }

    private class Item(var file: String, var icon: Int) {
        override fun toString(): String {
            return file
        }

    }
}