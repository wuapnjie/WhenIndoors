package com.xiaopo.flying.whenindoors.ui.page.locate

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.kits.toast
import com.xiaopo.flying.whenindoors.model.Room
import kotlinx.android.synthetic.main.activity_locate.*

class SelectLocateActivity : AppCompatActivity() {

  private lateinit var room: Room

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_locate)

    room = intent.getParcelableExtra("room")
    toast("(${room.width},${room.height})")

    initIndoorsImageView()
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
    indoors_image.needDrawGrid = true
    indoors_image.gridColumn = 25
    indoors_image.gridRow = 25
    indoors_image.onPickPositionListener = { pickedRoomX: Double, pickedRoomY: Double ->
      val data = Intent()
      data.putExtra("pickedX", pickedRoomX)
      data.putExtra("pickedY", pickedRoomY)
      setResult(Activity.RESULT_OK, data)
      finish()
    }
  }

  private fun placeWifiFingerprintMarks() {
    indoors_image.markDrawable = resources.getDrawable(R.drawable.ic_wifi_mark_24dp);
    room.positions?.let {
      indoors_image.markPositions = ArrayList(it)
    }
    indoors_image.roomWidth = room.width
    indoors_image.roomHeight = room.height
  }
}
