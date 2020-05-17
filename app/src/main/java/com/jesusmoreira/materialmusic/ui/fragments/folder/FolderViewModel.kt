package com.jesusmoreira.materialmusic.ui.fragments.folder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FolderViewModel : ViewModel() {

//    private var _text = MutableLiveData<String>().apply {
//        value = "This is Folder Fragment"
//    }
    var text: MutableLiveData<String> = MutableLiveData<String>().apply {
        value = "This is Folder Fragment"
    }
}