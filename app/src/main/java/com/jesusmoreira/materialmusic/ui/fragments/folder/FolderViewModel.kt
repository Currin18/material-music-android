package com.jesusmoreira.materialmusic.ui.fragments.folder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jesusmoreira.materialmusic.models.Folder

class FolderViewModel : ViewModel() {

//    private var _text = MutableLiveData<String>().apply {
//        value = "This is Folder Fragment"
//    }
//    var path: MutableLiveData<String> = MutableLiveData<String>().apply {
//        value = "This is Folder Fragment"
//    }

    var folder: MutableLiveData<Folder> = MutableLiveData<Folder>().apply { value = Folder() }

    var pathList: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>().apply { value = arrayListOf() }

//    var numOfFolders: MutableLiveData<Int> = MutableLiveData<Int>().apply {
//        value = 0
//    }
//
//    var numOfFiles: MutableLiveData<Int> = MutableLiveData<Int>().apply {
//        value = 0
//    }
}