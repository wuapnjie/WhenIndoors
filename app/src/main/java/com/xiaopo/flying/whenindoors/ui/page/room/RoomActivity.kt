package com.xiaopo.flying.whenindoors.ui.page.room

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.kits.replaceFragment
import com.xiaopo.flying.whenindoors.ui.page.addroom.AddRoomActivity
import kotlinx.android.synthetic.main.activity_room.*

class RoomActivity : AppCompatActivity() {

  companion object {
    val REQUEST_NEW_ROOM = 3424;
  }

  private lateinit var roomFragment: RoomFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_room)
    toolbar.title = getString(R.string.room_list)

    if (savedInstanceState == null) {
      roomFragment = RoomFragment.newInstance()
      replaceFragment(fragment = roomFragment)
    }

    fab.setOnClickListener {
      addNewRoom();
    }
  }

  private fun addNewRoom() {
    val intent = Intent(this, AddRoomActivity::class.java)
    startActivityForResult(intent, REQUEST_NEW_ROOM)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_NEW_ROOM && resultCode == Activity.RESULT_OK) {

    }
  }

}
