package com.xiaopo.flying.whenindoors.data.repository

import android.arch.lifecycle.LiveData
import android.net.Uri
import com.xiaopo.flying.whenindoors.kits.Result
import com.xiaopo.flying.whenindoors.model.*

/**
 * @author wupanjie
 */
interface Repository {

  fun getRoomList(limit: Int, offset: Int): LiveData<Result<RoomsData, Throwable>>

  fun getRoomInfo(roomId: String): LiveData<Result<Room, Throwable>>

  fun clearFingerprints(roomId: String): LiveData<Result<ResponseTemplate, Throwable>>

  fun uploadWifi(roomId: String, wifiData: WifiData): LiveData<Result<ResponseTemplate, Throwable>>

  fun uploadImage(uri: Uri): LiveData<Result<String, Throwable>>

  fun createRoom(room: Room): LiveData<Result<Room, Throwable>>

  fun fetchLocation(roomId: String, needComputePosition: NeedComputePosition, k : Int): LiveData<Result<RoomPosition, Throwable>>
}