package com.jesusmoreira.materialmusic.utils

import android.content.Context
import android.widget.Toast

object GeneralUtil {
    fun shortToast(context: Context, text: String = "") {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
    fun longToast(context: Context, text: String = "") {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}