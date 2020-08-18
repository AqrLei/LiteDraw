package com.aqrlei.litedraw.loader

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.aqrlei.litecache.lru.IAppCache
import com.aqrlei.litecache.lru.ICacheTask
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by AqrLei on 2019-06-04
 */
class SourceImageLoader(
    private val context: Context?,
    imageCacheTask: ICacheTask,
    private val localCache: IAppCache
) : AbstractImageLoader<String>(imageCacheTask) {

    override fun load(
        path: String,
        id: Int,
        width: Float,
        height: Float,
        callback: (Bitmap?, String) -> Unit) {
        loadInternal(path, width, height, callback)
    }

    private fun loadInternal(
        path: String,
        width: Float,
        height: Float,
        callback: (Bitmap?, String) -> Unit) {
        val bitmap = cacheBitmapMemory.get(generateMemoryKey(path, width, height))
        if (bitmap != null) {
            callback(bitmap, path)
            return
        }
        imageCacheTask?.let { loadTask ->
            localCache.load<Bitmap?>(
                originKey = path,
                callback = { bitmap -> callback(bitmap, path) },
                transformer = { originValue, byteArray ->
                    byteArray?.run { createBitmap(originValue, this, width, height) }
                },
                loadCacheTask = loadTask,
                loadFromCustom = { loadFromCustom(path) })
        }
    }


    private fun loadFromCustom(path: String): ByteArray? {
        val uri = Uri.parse(path)
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme || ContentResolver.SCHEME_FILE == uri.scheme) {
            loadFromFile(uri)
        } else {
            loadFromNet(path)
        }
    }

    private fun loadFromFile(uri: Uri?): ByteArray? {
        return try {
            uri?.let {
                context?.contentResolver?.openInputStream(it)?.readBytes()
            }
        } catch (e: FileNotFoundException) {
            null
        }
    }

    private fun loadFromNet(url: String): ByteArray? {
        var result: ByteArray?
        var bufferIn: BufferedInputStream? = null
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as? HttpURLConnection)?.apply {
                connectTimeout = 5000
                readTimeout = 10000
            }
            bufferIn = BufferedInputStream(connection?.inputStream)
            result = bufferIn.readBytes()
        } catch (e: Exception) {
            result = null
        } finally {
            connection?.disconnect()
            bufferIn?.close()
        }
        return result
    }

    private fun createBitmap(
        path: String,
        byteArray: ByteArray,
        width: Float,
        height: Float
    ): Bitmap? {
        val targetW = dp2px(width)
        val targetH = dp2px(height)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)
        options.inJustDecodeBounds = false
        if (targetH > 0F && targetW > 0F) {
            options.inScaled = false
            options.inSampleSize = getAdaptedBitmapSampleSize(options, targetW, targetH)
        }
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)
        bitmap?.run {
            cacheBitmapMemory.put(generateMemoryKey(path, width, height), this)
        }
        return bitmap
    }
}