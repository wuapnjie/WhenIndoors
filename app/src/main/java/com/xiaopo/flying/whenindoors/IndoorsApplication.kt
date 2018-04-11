package com.xiaopo.flying.whenindoors

import android.app.Application
import com.qiniu.android.dns.AndroidContext
import com.xiaopo.flying.whenindoors.di.AppComponent
import com.xiaopo.flying.whenindoors.di.AppModule
import com.xiaopo.flying.whenindoors.di.DaggerAppComponent
import com.xiaopo.flying.whenindoors.di.RemoteModule

/**
 * @author wupanjie
 */
class IndoorsApplication : Application() {

  companion object {
    lateinit var appComponent: AppComponent
  }

  override fun onCreate() {
    super.onCreate()
    AndroidContext.initialize(this)
    initialAppComponent()
  }

  private fun initialAppComponent() {
    appComponent =
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .remoteModule(RemoteModule())
            .build()
  }
}