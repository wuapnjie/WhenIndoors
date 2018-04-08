package com.xiaopo.flying.whenindoors.ui.page.room

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xiaopo.flying.whenindoors.R
import com.xiaopo.flying.whenindoors.RoomViewModel
import com.xiaopo.flying.whenindoors.kits.AnotherAdapter
import com.xiaopo.flying.whenindoors.kits.LinearDividerDecoration
import com.xiaopo.flying.whenindoors.kits.toast
import com.xiaopo.flying.whenindoors.model.Room
import kotlinx.android.synthetic.main.activity_room.*
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
    roomAdapter.with(Room::class.java, RoomItemViewBinder())
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
