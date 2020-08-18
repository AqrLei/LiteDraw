package com.aqrlei.litedraw.loader

import android.graphics.Bitmap

/**
 * Created by AqrLei on 2019-06-03
 */

interface IImageLoader<T> {

    fun load(path: String, id: Int, width: Float, height: Float, callback: (Bitmap?, key: String) -> Unit)
}