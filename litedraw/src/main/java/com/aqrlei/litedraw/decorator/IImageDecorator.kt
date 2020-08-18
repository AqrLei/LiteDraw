package com.aqrlei.litedraw.decorator

import android.graphics.Bitmap

/**
 * Created by AqrLei on 2019-06-03
 */
interface IImageDecorator {

    fun decorate(key:String,bitmap: Bitmap,targetWidth:Float,targetHeight:Float,callback:((Bitmap?) ->Unit)? = null): Bitmap?
    fun getBitmap(key:String,width:Float,height:Float):Bitmap?
    fun refresh()
  
}
