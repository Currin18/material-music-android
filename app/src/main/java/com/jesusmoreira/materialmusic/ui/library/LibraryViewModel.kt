package com.jesusmoreira.materialmusic.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jesusmoreira.materialmusic.controllers.MediaController

class LibraryViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Library Fragment"
    }
    val text: LiveData<String> = _text

    var mediaController: MediaController? = null
}