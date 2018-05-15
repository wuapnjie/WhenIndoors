package com.xiaopo.flying.whenindoors.ui.page.room

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.RoomViewModel
import com.xiaopo.flying.whenindoors.kits.AnotherAdapter
import com.xiaopo.flying.whenindoors.kits.LinearDividerDecoration
import com.xiaopo.flying.whenindoors.kits.toast
import com.xiaopo.flying.whenindoors.model.RoomInfo
import com.xiaopo.flying.whenindoors.ui.page.addroom.AddRoomActivity
import com.xiaopo.flying.whenindoors.ui.page.locate.LocateActivity
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.PermissionNo
import com.yanzhenjie.permission.PermissionYes
import kotlinx.android.synthetic.main.fragment_room.*

/**
 * A placeholder fragment containing a simple view.
 */
class RoomFragment : Fragment() {

  companion object {
    fun newInstance() = RoomFragment()
  }

  private lateinit var roomViewModel: RoomViewModel
  private val roomAdapter = AnotherAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initViewModel()

    AndPermission.with(this)
        .permission(Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.ACCESS_COARSE_LOCATION)
        .requestCode(AddRoomActivity.PERMISSION_CODE)
        .callback(this)
        .start()
  }

  @PermissionYes(LocateActivity.PERMISSION_CODE)
  private fun getPermissionYes(grantedPermissions: List<String>) {
  }

  @PermissionNo(LocateActivity.PERMISSION_CODE)
  private fun getPermissionNo(deniedPermissions: List<String>) {
    Toast.makeText(context, "必须要权限呀", Toast.LENGTH_SHORT).show()
  }

  private fun initViewModel() {
    roomViewModel = ViewModelProviders.of(this).get(RoomViewModel::class.java)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_room, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initUI()
  }

  private fun loadRoomList() {
    roomViewModel.loadRoomList()
        .observe(this, Observer { result ->
          swipe_refresh.isRefreshing = false

          val items = arrayListOf<Any>()
          result?.fold(success = {
            items.addAll(it.rooms)
            roomAdapter.update(items)
          }, failure = {
            toast(message = it.message)
          })
        })
  }

  private fun initUI() {
    room_list.layoutManager = LinearLayoutManager(context)
    roomAdapter.with(RoomInfo::class.java, RoomItemViewBinder())
    room_list.adapter = roomAdapter
    room_list.addItemDecoration(LinearDividerDecoration(
        resources,
        R.color.divider,
        1,
        LinearLayoutManager.VERTICAL
    ))

    swipe_refresh.setOnRefreshListener {
      loadRoomList()
    }

    swipe_refresh.post {
      swipe_refresh.isRefreshing = true
      loadRoomList()
    }
  }
}
