package com.xiaopo.flying.whenindoors.ui.page.setting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xiaopo.flying.whenindoors.R

/**
 * @author wupanjie
 */
class SettingActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_setting)

    if (savedInstanceState == null) {
      fragmentManager.beginTransaction()
          .replace(R.id.fragment_content, SettingFragment.newInstance())
          .commit()
    }
  }
}