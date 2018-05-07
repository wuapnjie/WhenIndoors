package com.xiaopo.flying.whenindoors.ui.page.roomdetail

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.RoomViewModel
import com.xiaopo.flying.whenindoors.SelectActivity
import com.xiaopo.flying.whenindoors.data.remote.STATUS_SUCCESS
import com.xiaopo.flying.whenindoors.kits.AnotherAdapter
import com.xiaopo.flying.whenindoors.kits.LinearDividerDecoration
import com.xiaopo.flying.whenindoors.kits.screenWidth
import com.xiaopo.flying.whenindoors.kits.toast
import com.xiaopo.flying.whenindoors.model.Room
import com.xiaopo.flying.whenindoors.model.RoomPosition
import com.xiaopo.flying.whenindoors.ui.page.bluetooth.bluetoothchat.BluetoothChatActivity
import com.xiaopo.flying.whenindoors.ui.page.locate.LocateActivity
import kotlinx.android.synthetic.main.activity_room_detail.*
import kotlinx.android.synthetic.main.content_room_detail.*

class RoomDetailActivity : AppCompatActivity() {

  companion object {
    val REQUEST_CODE = 12345
  }

  private lateinit var viewModel: RoomViewModel
  private var roomId = ""
  private val adapter = AnotherAdapter()
  private val fingerprints = arrayListOf<RoomPosition>()
  private var room: Room? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_room_detail)

    roomId = intent.getStringExtra("room_id") ?: ""

    viewModel = ViewModelProviders.of(this).get(RoomViewModel::class.java)

    initView()

    loadRoomInfo()
  }

  private fun initView() {
    toolbar.title = intent.getStringExtra("room_name") ?: "房间信息"
    toolbar.inflateMenu(R.menu.menu_room_detail)
    toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.action_clear -> clearFingerprint()
        R.id.action_bluetooth -> jumpToBluetooth()
      }

      return@setOnMenuItemClickListener true
    }

    swipe_refresh.post {
      swipe_refresh.isRefreshing = !swipe_refresh.isRefreshing
    }
    swipe_refresh.setOnRefreshListener {
      loadRoomInfo()
    }

    room_fingerprint_list.layoutManager = LinearLayoutManager(this)
    adapter.with(RoomPosition::class.java, FingerprintItemViewBinder())
    room_fingerprint_list.adapter = adapter
    room_fingerprint_list.addItemDecoration(LinearDividerDecoration(
        resources,
        R.color.divider,
        1,
        LinearLayoutManager.VERTICAL
    ))

    fab.setOnClickListener {
      val intent = Intent(this, SelectActivity::class.java)
      intent.putExtra("room", room)
      startActivityForResult(intent, REQUEST_CODE)
    }
  }

  private fun jumpToBluetooth() {
    val intent = Intent(this, BluetoothChatActivity::class.java)
    intent.putExtra("room_id", room?.id ?: "")
    startActivity(intent)
  }

  private fun clearFingerprint() {
    AlertDialog.Builder(this)
        .setMessage("确定要清空所有指纹？")
        .setNegativeButton("取消", { dialog, _ ->
          dialog.dismiss()
        })
        .setPositiveButton("确定", { dialog, _ ->
          dialog.dismiss()

          viewModel.clearFingerprints(roomId)
              .observe(this, Observer { result ->
                result?.fold(success = {
                  if (it.status == STATUS_SUCCESS) {
                    toast("清空成功")
                    fingerprints.clear()
                    adapter.update(fingerprints)
                  } else {
                    toast("清空失败")
                  }
                }, failure = {
                  toast(it.message)
                })
              })
        }).show()

  }

  private fun toLocatePosition() {
    val intent = Intent(this, LocateActivity::class.java)
    intent.putExtra("room", room)
    startActivity(intent)
  }

  private fun loadRoomInfo() {
    viewModel.loadRoomInfo(roomId)
        .observe(this, Observer { result ->
          swipe_refresh.post { swipe_refresh.isRefreshing = false }
          result?.fold(success = { room ->
            this.room = room
//            adjustAppBarHeight()
            Picasso.with(this)
                .load(room.imageUrl)
                .fit()
                .centerCrop()
                .into(room_image)
            room_image.setOnClickListener { toLocatePosition() }

            fingerprints.clear()
            room.positions.let {
              fingerprints.addAll(it)
            }
            adapter.update(fingerprints)
          }, failure = { cause ->
            toast(cause.message)
          })
        })
  }

  private fun adjustAppBarHeight() {
    val layoutParam = appbar.layoutParams
    // TODO 根据图片长宽比调整高度
    layoutParam.width = screenWidth
    layoutParam.height = (screenWidth.toFloat() * (room_image.drawable.intrinsicHeight.toFloat() / room_image.drawable.intrinsicWidth.toFloat())).toInt()
    appbar.layoutParams = layoutParam
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      loadRoomInfo()
    }
  }
}
