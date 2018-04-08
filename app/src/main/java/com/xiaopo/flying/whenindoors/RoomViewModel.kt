package com.xiaopo.flying.whenindoors

import android.arch.lifecycle.ViewModel
import android.net.Uri
import com.xiaopo.flying.whenindoors.data.repository.IndoorsRepository
import com.xiaopo.flying.whenindoors.model.Room
import com.xiaopo.flying.whenindoors.model.WifiData
import javax.inject.Inject

/**
 * @author wupanjie
 */
class RoomViewModel : ViewModel() {

  @Inject lateinit var repository: IndoorsRepository

  init {
    initializeDagger()
  }

  private fun initializeDagger() {
    IndoorsApplication.appComponent.inject(this)
  }

  fun loadRoomList(limit: Int = 10, offset: Int = 0) = repository.getRoomList(limit, offset)

  fun loadRoomInfo(roomId : String) = repository.getRoomInfo(roomId)

  fun clearFingerprints(roomId: String) = repository.clearFingerprints(roomId)

  fun uploadWifi(roomId: String, wifiData: WifiData) = repository.uploadWifi(roomId, wifiData)

  fun uploadImage(uri: Uri) = repository.uploadImage(uri)

  fun createRoom(room: Room) = repository.createRoom(room)
}