package com.xiaopo.flying.whenindoors.ui.page.room

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.kits.AnotherBinder
import com.xiaopo.flying.whenindoors.kits.AnotherViewHolder
import com.xiaopo.flying.whenindoors.model.RoomInfo
import com.xiaopo.flying.whenindoors.ui.page.bluetooth.bluetoothchat.BluetoothChatActivity
import com.xiaopo.flying.whenindoors.ui.page.locate.LocateActivity
import kotlinx.android.synthetic.main.item_room.view.*

/**
 * @author wupanjie
 */
class RoomItemViewBinder : AnotherBinder<RoomInfo>() {
  override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): AnotherViewHolder {
    val itemView = inflater.inflate(R.layout.item_room, parent, false)
    return AnotherViewHolder(itemView)
  }

  override fun renderView(holder: AnotherViewHolder, itemView: View, item: RoomInfo) {
    itemView.tv_room_name.text = item.name
    itemView.tv_fingerprint_count.text = "${item.positions_count} 个位置指纹"
    itemView.setOnClickListener {
      val intent = Intent(it.context, LocateActivity::class.java)
      intent.putExtra("room_id", item.id)
      it.context.startActivity(intent)
//      jumpToBluetooth(it.context,item.id)
    }

    Picasso.with(itemView.context)
        .load(item.imageUrl)
        .fit()
        .into(itemView.iv_room)
  }

  private fun jumpToBluetooth(context: Context, roomId : String) {
    val intent = Intent(context, BluetoothChatActivity::class.java)
    intent.putExtra("room_id", roomId)
    context.startActivity(intent)
  }
}