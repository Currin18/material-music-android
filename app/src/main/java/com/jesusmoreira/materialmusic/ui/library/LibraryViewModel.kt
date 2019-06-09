package com.jesusmoreira.materialmusic.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jesusmoreira.materialmusic.controllers.AudioController
import com.jesusmoreira.materialmusic.models.Audio

class LibraryViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is library Fragment"
    }
    val text: LiveData<String> = _text

    var audioController: AudioController? = null
}