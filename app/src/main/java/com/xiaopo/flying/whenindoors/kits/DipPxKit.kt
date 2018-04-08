package com.xiaopo.flying.whenindoors.kits

import android.content.Context
import android.content.res.Resources

/**
 * @author wupanjie
 */

fun Context.dipToPixel(dip: Float): Int
    = (dip * this.resources.displayMetrics.density + 0.5f).toInt()

val Int.dp: Int
  get() = this * Resources.getSystem().displayMetrics.density.toInt()

val Int.px: Int
  get() = this

val screenWidth = Resources.getSystem().displayMetrics.widthPixels

val screenHeight = Resources.getSystem().displayMetrics.heightPixels