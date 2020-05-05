package com.jesusmoreira.materialmusic.ui.podcast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PodcastViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Podcast Fragment"
    }
    val text: LiveData<String> = _text
}