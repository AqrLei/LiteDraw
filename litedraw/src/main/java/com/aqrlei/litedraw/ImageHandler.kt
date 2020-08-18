package com.aqrlei.litedraw

import android.graphics.Bitmap
import com.aqrlei.litedraw.decorator.IImageDecorator
import com.aqrlei.litedraw.loader.IImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by AqrLei on 2019-06-03
 */
class ImageHandler private constructor(private val imageLoader: IImageLoader<*>) :
    IImageHandler {
    private var imageKey = ""
    private var imageId = -1
    private var loadCallback: ((Bitmap?) -> Unit)? = null
    private var decorateCallback: ((Bitmap?, Int) -> Unit)? = null
    private var decoratorList = ArrayList<IImageDecorator>()

    override fun load(path: String, id: Int): IImageHandler {
        imageKey = path
        imageId = id
        return this
    }

    override fun decorate(decorator: IImageDecorator): IImageHandler {
        decoratorList.add(decorator)
        return this
    }

    override fun decorate(decorator: List<IImageDecorator>): IImageHandler {
        decoratorList.addAll(decorator)
        return this
    }

    override fun setCallback(loadCallback: ((Bitmap?) -> Unit)?, decorateCallback: ((Bitmap?, Int) -> Unit)?) {
        this.loadCallback = loadCallback
        this.decorateCallback = decorateCallback
    }

    override fun display(onDisplay: (Bitmap?) -> Unit, width: Float, height: Float) {
        imageLoader.load(imageKey, imageId, width, height) { bitmap ,key->
            GlobalScope.launch(Dispatchers.Main) {
                loadCallback?.invoke(bitmap)
            }
            bitmap?.run {
                if (decoratorList.isEmpty()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        onDisplay(bitmap)
                    }
                } else {
                    var tempBp: Bitmap = bitmap
                    for (i in 0 until decoratorList.size) {
                        decoratorList[i].decorate(key,tempBp, width, height) {
                            if (i != decoratorList.size - 1) {
                                tempBp = it ?: tempBp
                            } else {
                                GlobalScope.launch(Dispatchers.Main) {
                                    onDisplay(it)
                                }
                            }
                            decorateCallback?.invoke(tempBp, i)
                        }
                    }
                }
            }
        }
    }

    class Builder : IImageHandler.Builder {
        private lateinit var imageLoader: IImageLoader<*>
        override fun loadFrom(loader: IImageLoader<*>): Builder {
            this.imageLoader = loader
            return this
        }

        override fun build(): IImageHandler {
            if (!::imageLoader.isInitialized) throw IllegalArgumentException()
            return ImageHandler(
                imageLoader)
        }
    }
}