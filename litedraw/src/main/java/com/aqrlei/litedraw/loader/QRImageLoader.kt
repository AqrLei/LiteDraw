package com.aqrlei.litedraw.loader

import android.graphics.Bitmap
import com.aqrlei.litedraw.util.ImageUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*

/**
 * Created by AqrLei on 2019-06-10
 */
class QRImageLoader : AbstractImageLoader<String>() {

    private var logo: Bitmap? = null
    private var ratio: Float = 0.25F
    fun setLogo(logo: Bitmap?, ratio: Float = 0.25F): QRImageLoader {
        this.logo = logo
        this.ratio = ratio
        return this
    }


    override fun load(path: String, id: Int, width: Float, height: Float, callback: (Bitmap?, String) -> Unit) {
        if (width == 0F || height == 0f || path.isEmpty()) return
        loadInternal(path, width.toInt(), height.toInt(), callback)
    }


    private fun loadInternal(path: String, width: Int, height: Int, callback: (Bitmap?, String) -> Unit) {
        callback(
            cacheBitmapMemory.get(generateMemoryKey(path, width, height)) ?: generateQRCode(path, width, height),
            path
        )
    }

    private fun generateQRCode(qrContent: String, width: Int, height: Int): Bitmap? {
        var offsetX = width / 2
        var offsetY = height / 2
        var logoX = 0
        var logoY = 0
        if ((logo?.width ?: 0) / (width * 1.00F) > ratio || (logo?.height ?: 0) / (height * 1.00F) > ratio) {
            logo = ImageUtil.scaleBitmap(width, height, logo, ratio)
        }
        logo?.run {
            logoX = this.width
            logoY = this.height
            offsetX = (width - logoX) / 2
            offsetY = (height - logoY) / 2
        }
        val hints = Hashtable<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "utf-8")
            put(EncodeHintType.MARGIN, 0)
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)

        }
        val matrix = MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, width, height, hints)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (x >= offsetX && x < offsetX + logoX && y >= offsetY && y < offsetY + logoY) {

                    var pixel = logo?.getPixel(x - offsetX, y - offsetY) ?: 0
                    if (pixel == 0) {
                        pixel = if (matrix.get(x, y)) {
                            0XFF000000.toInt()
                        } else {
                            0XFFFFFFFF.toInt()
                        }
                    }
                    pixels[y * width + x] = pixel
                } else {
                    pixels[y * width + x] = if (matrix.get(x, y)) {
                        0XFF000000.toInt()
                    } else {
                        0XFFFFFFFF.toInt()
                    }
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
        val realKey = generateMemoryKey(qrContent, width, height)
        cacheBitmapMemory.put(realKey, bitmap)
        return cacheBitmapMemory.get(realKey)
    }
}