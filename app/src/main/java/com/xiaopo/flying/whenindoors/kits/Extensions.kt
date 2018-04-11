package com.xiaopo.flying.whenindoors.kits

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.xiaopo.flying.whenindoors.BuildConfig
import com.xiaopo.flying.whenindoors.R

/**
 * @author wupanjie
 */

fun Fragment.toast(message: String? = "error", length: Int = Toast.LENGTH_SHORT): Unit {
  Toast.makeText(context, message, length).show()
}

fun Context.toast(message: String? = "error", length: Int = Toast.LENGTH_SHORT): Unit {
  Toast.makeText(this, message, length).show()
}

fun AppCompatActivity.replaceFragment(contentId: Int = R.id.fragment_content, fragment: Fragment) {
  supportFragmentManager.beginTransaction()
      .replace(contentId, fragment)
      .commit()
}

fun Any.logd(messgae: String?): Unit {
  if (BuildConfig.DEBUG) {
    Log.d(this.javaClass.simpleName, messgae)
  }
}