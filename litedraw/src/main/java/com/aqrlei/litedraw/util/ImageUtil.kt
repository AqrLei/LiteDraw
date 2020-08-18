package com.aqrlei.litedraw.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.TypedValue
import kotlin.math.min

/**
 * Created by AqrLei on 2019-06-05
 */
object ImageUtil {

    fun runtimeMaxMemory() = Runtime.getRuntime().maxMemory()

    fun dp2px(dpValue: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            Resources.getSystem().displayMetrics)
    }

    fun isSuitable(rW: Float, rH: Float, tW: Float, tH: Float, accuracy: Double): Boolean {
        return if (tW > 0F && tH > 0F) {
            val ratio = tW / tH * rH / rW * 1.00F
            Math.abs(1 - ratio) <= accuracy
        } else true
    }

    fun cropBitmap(bitmap: Bitmap, tW: Float, tH: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val reserveSize = Math.min(width, height)
        val finalW: Int
        val finalH: Int
        if (reserveSize == width) {
            finalW = reserveSize
            finalH = ((reserveSize * tH / tW).toInt()).takeIf { it <= height } ?: height
        } else {
            finalH = reserveSize
            finalW = (reserveSize * tW / tH).toInt().takeIf { it <= width } ?: width
        }
        val x = (width - finalW) / 2
        val y = (height - finalH) / 2
        return Bitmap.createBitmap(bitmap, x, y, finalW, finalH)
    }

    fun getAdaptedBitmapSampleSize(
        options: BitmapFactory.Options,
        targetWidth: Float,
        targetHeight: Float
    ): Int {
        val realHeight = options.outHeight
        val realWidth = options.outWidth
        return (min(realHeight / targetHeight, realWidth / targetWidth)).toInt()
    }

    fun scaleBitmap(w: Int, h: Int, bitmap: Bitmap?, scale: Float): Bitmap? {
        return bitmap?.let {
            val matrix = Matrix().apply {
                val scaleRatio =
                    Math.min(scale * w * 1.0f / bitmap.width, scale * h * 1.0f / bitmap.height)
                postScale(scaleRatio, scaleRatio)
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

}