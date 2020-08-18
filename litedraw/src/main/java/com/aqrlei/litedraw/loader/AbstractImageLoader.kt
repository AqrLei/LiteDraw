package com.aqrlei.litedraw.loader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import com.aqrlei.litecache.lru.ICacheTask
import com.aqrlei.litedraw.util.ImageUtil

/**
 * Created by AqrLei on 2019-06-05
 */
abstract class AbstractImageLoader<T>(protected val imageCacheTask: ICacheTask? = null) :
    IImageLoader<T> {

    companion object {
        @JvmStatic
        protected val cacheBitmapMemory: LruCache<String, Bitmap> =
            object : LruCache<String, Bitmap>((ImageUtil.runtimeMaxMemory() / 20).toInt()) {
                override fun sizeOf(key: String, value: Bitmap): Int {
                    return value.byteCount
                }
            }
    }

    protected fun dp2px(dpValue: Float): Float {
        return ImageUtil.dp2px(dpValue)
    }

    protected fun getAdaptedBitmapSampleSize(
        options: BitmapFactory.Options,
        targetWidth: Float,
        targetHeight: Float
    ): Int {
        return ImageUtil.getAdaptedBitmapSampleSize(options, targetWidth, targetHeight)
    }

    protected fun generateMemoryKey(key: String, width: Float, height: Float): String {
        return "$width$key$height"
    }

    protected fun generateMemoryKey(key: String, width: Int, height: Int): String {
        return "$width$key$height"
    }
}