package com.xiaopo.flying.whenindoors.ui.page.addroom

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.RoomViewModel
import com.xiaopo.flying.whenindoors.kits.logd
import com.xiaopo.flying.whenindoors.kits.toast
import com.xiaopo.flying.whenindoors.model.Room
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionNo
import com.yanzhenjie.permission.PermissionYes
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine
import kotlinx.android.synthetic.main.activity_add_room.*


class AddRoomActivity : AppCompatActivity() {

  private lateinit var roomViewModel: RoomViewModel
  private var selectedImage: Uri? = null

  companion object {
    const val REQUEST_CODE_CHOOSE = 53252
    const val PERMISSION_CODE = 3421
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_room)

    initViewModel();

    toolbar.inflateMenu(R.menu.menu_add_room)
    toolbar.setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.action_done -> createNewRoom()
      }

      return@setOnMenuItemClickListener true
    }
    iv_room.setOnClickListener {
      pickRoomPhoto()
    }
  }

  private fun initViewModel() {
    roomViewModel = ViewModelProviders.of(this).get(RoomViewModel::class.java)
  }

  private fun createNewRoom() {
    if (checkInfo()) {
      // TODO 上传图片，成功后创建房间
      selectedImage?.let {
        roomViewModel.uploadImage(uri = it)
            .observe(this, Observer { result ->
              result?.fold(success = {
                logd(it)
                createRoom(it)
              }, failure = { cause ->
                toast(cause.message)
              })
            })
      }

    }
  }

  private fun createRoom(imageUrl: String) {
    val name = et_room_name.text.toString().trim()
    val width = et_room_width.text.toString().trim().toDouble()
    val height = et_room_height.text.toString().trim().toDouble()

    val roomInfo = Room("-1", name, arrayListOf(), imageUrl, width, height)
    roomViewModel.createRoom(roomInfo)
        .observe(this, Observer { result ->
          result?.fold(success = { room ->
            logd(room.toString())
          }, failure = {
            logd(it.message!!)
            toast(it.message)
          })
        })
  }

  private fun checkInfo(): Boolean {
    var message = ""

    do {
      if (selectedImage == null) {
        message = "请选择房间图片"
        break
      }

      if (et_room_name.text.toString().trim().isBlank()) {
        message = "请输入房间名"
        break
      }

      val width = et_room_width.text.toString().trim().toDoubleOrNull()
      if (width == null || width <= 0) {
        message = "请输入正确的房间宽度"
        break
      }

      val height = et_room_height.text.toString().trim().toDoubleOrNull()
      if (height == null || height <= 0) {
        message = "请输入正确的房间高度"
        break
      }

    } while (false)

    if (message.isNotEmpty()) {
      toast(message)
      return false
    }

    return true
  }

  @SuppressLint("InlinedApi")
  private fun pickRoomPhoto() {
    AndPermission.with(this)
        .permission(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .requestCode(PERMISSION_CODE)
        .callback(this)
        .start()
  }


  @PermissionYes(PERMISSION_CODE)
  private fun getPermissionYes(grantedPermissions: List<String>) {
    Matisse.from(this)
        .choose(MimeType.of(MimeType.JPEG, MimeType.PNG))
        .maxSelectable(1)
        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        .thumbnailScale(0.85f)
        .imageEngine(PicassoEngine())
        .forResult(REQUEST_CODE_CHOOSE)
  }

  @PermissionNo(PERMISSION_CODE)
  private fun getPermissionNo(deniedPermissions: List<String>) {
    Toast.makeText(this, "必须要权限呀", Toast.LENGTH_SHORT).show()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
      val selected = Matisse.obtainResult(data)
      selected?.let {
        if (it.isNotEmpty()) {
          selectedImage = it[0]
          Picasso.with(this)
              .load(selectedImage)
              .fit()
              .centerCrop()
              .into(iv_room)
        }
      }
      toast(selectedImage.toString())
    }
  }

}
