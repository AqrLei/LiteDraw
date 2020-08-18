package com.aqrlei.litedraw.loader

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Created by AqrLei on 2019-06-03
 */
class ResImageLoader(private val resource: Resources) : AbstractImageLoader<Int>() {


    override fun load(path: String, id: Int, width: Float, height: Float, callback: (Bitmap?, String) -> Unit) {
        loadInternal(id, width, height, callback)
    }

    private fun loadInternal(
        resId: Int,
        targetWidthDp: Float,
        targetHeightDp: Float,
        callback: (Bitmap?, String) -> Unit
    ) {
        val key = generateMemoryKey(resId.toString(), targetWidthDp, targetHeightDp)
        callback(loadFromMemory(key) ?: loadFromRes(resId, targetWidthDp, targetHeightDp), resId.toString())
    }

    private fun loadFromMemory(key: String): Bitmap? {
        return cacheBitmapMemory.get(key)
    }

    private fun loadFromRes(resId: Int, targetWidthDp: Float, targetHeightDp: Float): Bitmap? {
        val targetWidth = dp2px(targetWidthDp)
        val targetHeight = dp2px(targetHeightDp)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resource, resId, options)
        options.inJustDecodeBounds = false
        if (targetWidth > 0F && targetHeight > 0F) {
            options.inScaled = false
            options.inSampleSize = getAdaptedBitmapSampleSize(options, targetWidth, targetHeight)
        }
        val bitmap = BitmapFactory.decodeResource(resource, resId, options)
        val key = generateMemoryKey(resId.toString(), targetWidthDp, targetHeightDp)
        cacheBitmapMemory.put(key, bitmap)
        return bitmap
    }
}