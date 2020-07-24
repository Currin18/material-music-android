package com.jesusmoreira.materialmusic.models

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FilenameFilter

class Folder(val path: String = DEFAULT_PATH) {
    companion object {
        private const val TAG = "models.Folder"
        val DEFAULT_PATH = "${Environment.getExternalStorageDirectory()}"
    }

    var folderList: ArrayList<String> = arrayListOf()
    var fileList: ArrayList<String> = arrayListOf()

    init {
        if (path != DEFAULT_PATH && path != "$DEFAULT_PATH/")
            folderList.add("..")

        loadFileList(File(path))?.let{ arrayList ->
            arrayList.forEach {
                if (it.endsWith(".mp3")) fileList.add(it)
                else  folderList.add(it)
            }
        }

        folderList.sort()
        fileList.sort()
    }

    private fun loadFileList(path: File): ArrayList<String>? {
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
//                Log.i(TAG, "file: ${sel.isFile} ${sel.path} ${sel.path.endsWith(".mp3")}")
                !sel.isHidden && (sel.isDirectory || (sel.isFile && sel.path.endsWith(".mp3")))
            }

            val fList: Array<String> = path.list(filter) ?: arrayOf()

            Log.i(TAG, "fList: ${fList.contentToString()}")

            return ArrayList(fList.toList())
        }

        return null
    }
}