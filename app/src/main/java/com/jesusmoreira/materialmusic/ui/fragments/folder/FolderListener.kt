package com.jesusmoreira.materialmusic.ui.fragments.folder

interface FolderListener {
    fun onClickFolder(folder: String)
    fun onClickFile(position: Int)
}