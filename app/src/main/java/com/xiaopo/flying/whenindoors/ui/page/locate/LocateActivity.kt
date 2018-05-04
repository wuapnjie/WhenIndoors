package com.xiaopo.flying.whenindoors.ui.page.locate

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.xiaopo.flying.awifi.AWifi
import com.xiaopo.flying.awifi.WiFiNetwork
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.RoomViewModel
import com.xiaopo.flying.whenindoors.kits.toast
import com.xiaopo.flying.whenindoors.model.NeedComputePosition
import com.xiaopo.flying.whenindoors.model.Room
import com.xiaopo.flying.whenindoors.model.WiFiInfo
import com.xiaopo.flying.whenindoors.ui.page.addroom.AddRoomActivity
import com.xiaopo.flying.whenindoors.ui.page.roomdetail.RoomDetailActivity
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionNo
import com.yanzhenjie.permission.PermissionYes
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_locate.*
import java.lang.ref.WeakReference

class LocateActivity : AppCompatActivity() {

  private lateinit var room: Room
  private var roomId = ""
  private lateinit var roomViewModel: RoomViewModel
  private lateinit var locateHandler: LocateHandler
  private var start = false

  companion object {
    const val PERMISSION_CODE = 3421
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_locate)

    locateHandler = LocateHandler(this)
    initViewModel()

    roomId = intent.getStringExtra("room_id") ?: ""

    initView()

    loadRoomInfo()

    AndPermission.with(this)
        .permission(Manifest.permission.ACCESS_WIFI_STATE)
        .requestCode(AddRoomActivity.PERMISSION_CODE)
        .callback(this)
        .start()
  }

  private fun initView() {
    toolbar.setNavigationOnClickListener { onBackPressed() }
    toolbar.inflateMenu(R.menu.menu_locate)
  }

  private fun showRoomDetail() {
    val intent = Intent(this, RoomDetailActivity::class.java)
    intent.putExtra("room_id", room.id)
    intent.putExtra("room_name", room.name)
    startActivity(intent)
  }

  private fun initListener() {
    toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.action_detail -> showRoomDetail()
      }

      return@setOnMenuItemClickListener true
    }

    btn_control_locate.setOnClickListener {
      if (start) {
        locateHandler.stopLocate()
      } else {
        locateHandler.startLocate()
      }
    }

    btn_show_grid.setOnClickListener {
      if (indoors_image.needDrawGrid) {
        indoors_image.needDrawGrid = false
        btn_show_grid.setImageResource(R.drawable.ic_grid_on_white_24dp)
      } else {
        indoors_image.needDrawGrid = true
        btn_show_grid.setImageResource(R.drawable.ic_grid_off_white_24dp)
      }
    }

    btn_show_mark.setOnClickListener {
      if (indoors_image.needDrawMark) {
        indoors_image.needDrawMark = false
        btn_show_mark.setImageResource(R.drawable.ic_show_mark_on_white_24dp)
      } else {
        indoors_image.needDrawMark = true
        btn_show_mark.setImageResource(R.drawable.ic_show_mark_off_white_24dp)
      }
    }
  }

  private fun loadRoomInfo() {
    roomViewModel.loadRoomInfo(roomId)
        .observe(this, Observer { result ->
          result?.fold(success = {
            room = it
            initIndoorsImageView()
            initListener()
          }, failure = {
            toast(it.message)
          })
        })
  }

  @PermissionYes(PERMISSION_CODE)
  private fun getPermissionYes(grantedPermissions: List<String>) {
  }

  @PermissionNo(PERMISSION_CODE)
  private fun getPermissionNo(deniedPermissions: List<String>) {
    Toast.makeText(this, "必须要权限呀", Toast.LENGTH_SHORT).show()
  }

  private fun initViewModel() {
    roomViewModel = ViewModelProviders.of(this).get(RoomViewModel::class.java)
  }

  private val picassoTarget = object : Target {
    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

    }

    override fun onBitmapFailed(errorDrawable: Drawable?) {

    }

    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
      indoors_image.setImageBitmap(bitmap)
      placeWifiFingerprintMarks()
    }

  }

  private fun initIndoorsImageView() {
    Picasso.with(this)
        .load(room.imageUrl)
        .into(picassoTarget)
  }

  private fun locate(needComputePosition: NeedComputePosition) {
    roomViewModel.fetchLocation(room.id, needComputePosition)
        .observe(this, Observer { result ->
          result?.fold(
              success = {
                tv_debug_info.text = "(${it.x}, ${it.y})"
                indoors_image.currentPosition = it
                locateHandler.startLocate()
              },
              failure = {
                tv_debug_info.text = it.message
                toast(it.message)
                locateHandler.startLocate()
              }
          )
        })
  }

  private fun switchIcon() {
    if (start) {
      btn_control_locate.setImageResource(R.drawable.ic_pause_white_24dp)
    } else {
      btn_control_locate.setImageResource(R.drawable.ic_play_arrow_white_24dp)
    }
  }

  override fun onResume() {
    super.onResume()
    if (start) {
      locateHandler.startLocate()
    }
  }

  override fun onPause() {
    super.onPause()
    if (start) {
      locateHandler.stopLocate()
    }
  }

  private fun placeWifiFingerprintMarks() {
    indoors_image.currentMarkDrawable = resources.getDrawable(R.drawable.ic_current_mark_red_500_18dp)
    indoors_image.needDrawCurrentMark = true
    indoors_image.markDrawable = resources.getDrawable(R.drawable.ic_wifi_mark_18dp);
    room.positions.let {
      indoors_image.markPositions = ArrayList(it)
    }
    indoors_image.roomWidth = room.width
    indoors_image.roomHeight = room.height
  }

  class LocateHandler(weakActivity: LocateActivity) : Handler() {
    private val weakActivityRef = WeakReference(weakActivity)
    private var disposable: Disposable? = null

    fun startLocate() {
      weakActivityRef.get()?.let {
        it.start = true
        it.switchIcon()
        disposable = AWifi.from(it.applicationContext)
            .subscribe({ scanResults ->
              val wifiInfos = java.util.ArrayList<WiFiInfo>()
              for (scanResult in scanResults) {
                val wiFiNetwork = WiFiNetwork.from(scanResult)
                wifiInfos.add(WiFiInfo(wiFiNetwork.ssid, wiFiNetwork.bssid, wiFiNetwork.rssi))
              }

              val needComputePosition = NeedComputePosition(wifiInfos)
              it.locate(needComputePosition)
            })
      }
    }

    fun stopLocate() {
      disposable?.dispose()
      weakActivityRef.get()?.let {
        it.start = false
        it.switchIcon()
      }
    }

  }
}
