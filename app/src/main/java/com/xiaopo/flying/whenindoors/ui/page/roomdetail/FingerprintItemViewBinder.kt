package com.xiaopo.flying.whenindoors.ui.page.roomdetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.kits.AnotherBinder
import com.xiaopo.flying.whenindoors.kits.AnotherViewHolder
import com.xiaopo.flying.whenindoors.model.RoomPosition
import kotlinx.android.synthetic.main.item_fingerprint.view.*

/**
 * @author wupanjie
 */
class FingerprintItemViewBinder : AnotherBinder<RoomPosition>() {
  override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): AnotherViewHolder {
    val itemView = inflater.inflate(R.layout.item_fingerprint, parent, false)
    return AnotherViewHolder(itemView)
  }

  override fun renderView(holder: AnotherViewHolder, itemView: View, item: RoomPosition) {
    itemView.tv_position.text = "指纹坐标: \n(${item.x.toFloat()},${item.y.toFloat()})"
//    itemView.tv_wifi_stat_count.text = "${item.wifi_stats?.size ?: 0}个WiFi信息"
//    itemView.setOnClickListener {
//      WifiStatsDialog(it.context, item.wifi_stats)
//    }
  }
}