package com.jesusmoreira.materialmusic.utils

import android.graphics.Bitmap
import android.util.Log
import androidx.palette.graphics.Palette
import com.jesusmoreira.materialmusic.R

object GraphicUtil {
    const val TAG : String = "GraphicUtil"

    fun getPaletteFromBitmap(bitmap: Bitmap) : Palette = Palette.from(bitmap).generate()

    fun getDominantFromBitmap(bitmap: Bitmap) : Int? = getPaletteFromBitmap(bitmap).dominantSwatch?.rgb
    fun getVibrantFromBitmap(bitmap: Bitmap) : Int? = getPaletteFromBitmap(bitmap).vibrantSwatch?.rgb
    fun getVibrantDarkFromBitmap(bitmap: Bitmap) : Int? = getPaletteFromBitmap(bitmap).darkVibrantSwatch?.rgb
    fun getVibrantLightFromBitmap(bitmap: Bitmap) : Int? = getPaletteFromBitmap(bitmap).lightVibrantSwatch?.rgb
    fun getMutedFromBitmap(bitmap: Bitmap) : Int? = getPaletteFromBitmap(bitmap).mutedSwatch?.rgb
    fun getMutedDarkFromBitmap(bitmap: Bitmap) : Int? = getPaletteFromBitmap(bitmap).darkMutedSwatch?.rgb
    fun getMutedLightFromBitmap(bitmap: Bitmap) : Int? = getPaletteFromBitmap(bitmap).lightMutedSwatch?.rgb

    fun getColorFromPalette(palette: Palette) : Int? = with(palette) {
        vibrantSwatch?.rgb
            ?: dominantSwatch?.rgb
            ?: darkVibrantSwatch?.rgb
            ?: lightVibrantSwatch?.rgb
            ?: mutedSwatch?.rgb
            ?: darkMutedSwatch?.rgb
            ?: lightMutedSwatch?.rgb
    }

    fun getContrastColorFromPalette(palette: Palette) : Int? = with(palette) {
        dominantSwatch?.rgb
            ?: darkVibrantSwatch?.rgb
            ?: vibrantSwatch?.rgb
            ?: lightVibrantSwatch?.rgb
            ?: mutedSwatch?.rgb
            ?: darkMutedSwatch?.rgb
            ?: lightMutedSwatch?.rgb
    }

    fun printPalette(palette: Palette) {
        with(palette) {
            Log.d(TAG, "dominantSwatch: ${intToRGB(dominantSwatch?.rgb)}")
            Log.d(TAG, "vibrantSwatch: ${intToRGB(vibrantSwatch?.rgb)}")
            Log.d(TAG, "darkVibrantSwatch: ${intToRGB(darkVibrantSwatch?.rgb)}")
            Log.d(TAG, "lightVibrantSwatch: ${intToRGB(lightVibrantSwatch?.rgb)}")
            Log.d(TAG, "mutedSwatch: ${intToRGB(mutedSwatch?.rgb)}")
            Log.d(TAG, "darkMutedSwatch: ${intToRGB(darkMutedSwatch?.rgb)}")
            Log.d(TAG, "lightMutedSwatch: ${intToRGB(lightMutedSwatch?.rgb)}")
        }
    }

    fun intToRGB(color: Int?): String {
        return color?.let {
            String.format("#%06X", 0xFFFFFF and color)
        } ?: "UNDEFINED"

//        val r = (color shr 16 and 0xff) / 255.0f
//        val g = (color shr 8 and 0xff) / 255.0f
//        val b = (color and 0xff) / 255.0f
//        val a = (color shr 24 and 0xff) / 255.0f
//        return "#$a$r$g$b"
    }
}