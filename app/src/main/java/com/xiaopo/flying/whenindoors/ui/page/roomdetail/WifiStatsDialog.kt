package com.xiaopo.flying.whenindoors.ui.page.roomdetail

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.kits.AnotherAdapter
import com.xiaopo.flying.whenindoors.kits.LinearDividerDecoration
import com.xiaopo.flying.whenindoors.model.WiFiInfo
import kotlinx.android.synthetic.main.dialog_wifi_stats.view.*

/**
 * @author wupanjie
 */
class WifiStatsDialog(val context: Context, wifiStats: List<WiFiInfo>) {
  val dialog = BottomSheetDialog(context)

  init {
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_stats, null)

    val wifiList = view.wifi_list
    wifiList.layoutManager = LinearLayoutManager(context)
    val adapter = AnotherAdapter().with(WiFiInfo::class.java, WifiNetworkItemViewBinder())
    wifiList.adapter = adapter
    adapter.update(wifiStats)
    wifiList.addItemDecoration(LinearDividerDecoration(
        context.resources,
        R.color.divider,
        1,
        LinearLayoutManager.VERTICAL
    ))


    dialog.setContentView(view)
    dialog.show()
  }

}