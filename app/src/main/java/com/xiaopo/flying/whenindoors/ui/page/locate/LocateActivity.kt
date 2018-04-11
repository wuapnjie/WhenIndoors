package com.xiaopo.flying.whenindoors.ui.page.locate

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
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
import com.xiaopo.flying.whenindoors.kits.logd
import com.xiaopo.flying.whenindoors.kits.toast
import com.xiaopo.flying.whenindoors.model.NeedComputePosition
import com.xiaopo.flying.whenindoors.model.Room
import com.xiaopo.flying.whenindoors.model.WiFiInfo
import com.xiaopo.flying.whenindoors.ui.page.addroom.AddRoomActivity
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionNo
import com.yanzhenjie.permission.PermissionYes
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_locate.*
import java.lang.ref.WeakReference

class LocateActivity : AppCompatActivity() {

  private lateinit var room: Room
  private lateinit var roomViewModel: RoomViewModel
  private lateinit var locateHandler: LocateHandler

  companion object {
    const val PERMISSION_CODE = 3421
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_locate)

    locateHandler = LocateHandler(this)
    initViewModel()

    room = intent.getParcelableExtra("room")

    initIndoorsImageView()

    AndPermission.with(this)
        .permission(Manifest.permission.ACCESS_WIFI_STATE)
        .requestCode(AddRoomActivity.PERMISSION_CODE)
        .callback(this)
        .start()
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
                logd("(${it.x}), ${it.y})")
                locateHandler.startLocate()
              },
              failure = {
                logd(it.message)
                toast(it.message)
                locateHandler.startLocate()
              }
          )
        })
  }

  override fun onResume() {
    super.onResume()
    locateHandler.startLocate()
  }

  override fun onPause() {
    super.onPause()
    locateHandler.stopLocate()
  }

  private fun placeWifiFingerprintMarks() {
    indoors_image.markDrawable = resources.getDrawable(R.drawable.ic_wifi_mark_24dp);
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
    }

  }
}
