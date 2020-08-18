package com.aqrlei.litedraw.decorator

import android.graphics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by AqrLei on 2019-06-03
 */
class ShapeImageDecorator : AbstractImageDecorator() {
    private var decorateBitmap: Bitmap? = null

    private var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 1F
        isDither = true
    }
    private var topLeftRadius: Float = 0F
    private var topRightRadius: Float = 0F
    private var bottomLeftRadius: Float = 0F
    private var bottomRightRadius: Float = 0F

    private var strokeWidth: Float = 0F
    private var strokeColor: Int = Color.TRANSPARENT

    private var asCircle: Boolean = false

    private var radii = FloatArray(8) { 0F }
    override fun decorate(
        key: String,
        bitmap: Bitmap,
        targetWidth: Float,
        targetHeight: Float,
        callback: ((Bitmap?) -> Unit)?
    ): Bitmap? {
        decorateBitmap = try {
            onDecorate(key, bitmap, targetWidth, targetHeight)
        } catch (e: Exception) {
            null
        }
        GlobalScope.launch(Dispatchers.Main) {
            callback?.invoke(decorateBitmap)
        }
        return decorateBitmap
    }


    fun setAllRoundRadius(roundRadius: Float): ShapeImageDecorator {
        setRoundRadius(roundRadius, roundRadius, roundRadius, roundRadius)
        return this
    }

    private fun setRoundRadius(
        leftTop: Float,
        rightTop: Float,
        leftBottom: Float,
        rightBottom: Float): ShapeImageDecorator {
        topLeftRadius = dp2px(leftTop)
        topRightRadius = dp2px(rightTop)
        bottomLeftRadius = dp2px(leftBottom)
        bottomRightRadius = dp2px(rightBottom)
        return this
    }

    fun asCircle(flag: Boolean): ShapeImageDecorator {
        asCircle = flag
        return this
    }

    fun setLeftTopRoundRadius(radius: Float): ShapeImageDecorator {
        topLeftRadius = dp2px(radius)
        return this
    }

    fun setRightTopRoundRadius(radius: Float): ShapeImageDecorator {
        topRightRadius = dp2px(radius)
        return this
    }

    fun setLeftBottomRoundRadius(radius: Float): ShapeImageDecorator {
        bottomLeftRadius = dp2px(radius)
        return this
    }

    fun setRightBottomRoundRadius(radius: Float): ShapeImageDecorator {
        bottomRightRadius = dp2px(radius)
        return this
    }

    fun setBorders(color: Int, strokeWidth: Float): ShapeImageDecorator {
        strokeColor = color
        this.strokeWidth = dp2px(strokeWidth)
        return this
    }

    private fun onDecorate(
        key: String,
        bitmap: Bitmap,
        targetWidthDp: Float,
        targetHeightDp: Float): Bitmap? {
        val targetWidth = dp2px(targetWidthDp)
        val targetHeight = dp2px(targetHeightDp)
        var height = bitmap.height
        var width = bitmap.width
        var processBitmap = bitmap
        if (!isSuitable(width.toFloat(), height.toFloat(), targetWidth, targetHeight)) {
            processBitmap = cropBitmap(bitmap, targetWidth, targetHeight)
            height = processBitmap.height
            width = processBitmap.width
        }
        val size = Math.min(width, height)
        if (asCircle) {
            height = size
            width = size
        }
        val ratio =
            if (targetHeight > 0F && targetWidth > 0F)
                Math.sqrt((width * height) / (targetHeight * targetWidth).toDouble()).toFloat()
            else 1.0F
        val path = Path()
        val cx = width / 2F
        val cy = height / 2F

        val radius = Math.min(cx, cy)

        topLeftRadius *= ratio
        bottomLeftRadius *= ratio
        topRightRadius *= ratio
        bottomRightRadius *= ratio

        val roundedCornerRadius = if (asCircle) radius else Math.min(
            Math.min(topLeftRadius, topRightRadius),
            Math.min(topRightRadius, bottomRightRadius)
        )
        val paintingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(paintingBitmap)

        val rect = Rect(0, 0, width, height)
        val rectF = RectF(rect)

        if (asCircle) {
            path.addCircle(cx, cy, roundedCornerRadius, Path.Direction.CW)
        } else {
            radii[0] = topLeftRadius
            radii[1] = topLeftRadius
            radii[2] = topRightRadius
            radii[3] = topRightRadius
            radii[4] = bottomLeftRadius
            radii[5] = bottomLeftRadius
            radii[6] = bottomRightRadius
            radii[7] = bottomRightRadius
            path.addRoundRect(rectF, radii, Path.Direction.CW)
        }
        path.close()
        canvas.clipPath(path)
        canvas.drawBitmap(processBitmap, rect, rect, paint)
        paint.style = Paint.Style.STROKE
        paint.color = strokeColor
        paint.strokeWidth = strokeWidth
        canvas.drawPath(path, paint)
        cacheBitmapMemory.put(generateMemoryKey(key, targetWidthDp, targetHeightDp), paintingBitmap)
        refresh()
        return getBitmap(key, targetWidthDp, targetHeightDp)
    }

    override fun refresh() {
        setAllRoundRadius(0F)
        paint.run {
            reset()
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 1F
            isDither = true
        }
    }
}