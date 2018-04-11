package com.xiaopo.flying.whenindoors.ui.page.roomdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.kits.AnotherBinder
import com.xiaopo.flying.whenindoors.kits.AnotherViewHolder
import com.xiaopo.flying.whenindoors.model.WiFiInfo
import kotlinx.android.synthetic.main.item_wifi.view.*

/**
 * @author wupanjie
 */
class WifiNetworkItemViewBinder : AnotherBinder<WiFiInfo>() {
  override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): AnotherViewHolder {
    val root = inflater.inflate(R.layout.item_wifi, parent, false)
    return AnotherViewHolder(root)
  }

  override fun renderView(holder: AnotherViewHolder, itemView: View, item: WiFiInfo) {
    itemView.tv_ssid.text = item.SSID
    itemView.tv_bssid.text = item.BSSID
    itemView.tv_rssi.text = "${item.RSSI} dB"
  }
}