package com.xiaopo.flying.whenindoors.data.remote

import com.xiaopo.flying.whenindoors.data.IndoorsDataSource
import com.xiaopo.flying.whenindoors.model.ResponseTemplate
import com.xiaopo.flying.whenindoors.model.Room
import com.xiaopo.flying.whenindoors.model.RoomsData
import com.xiaopo.flying.whenindoors.model.WifiData
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author wupanjie
 */
class RemoteDataSource @Inject constructor(private val indoorsAPI: IndoorsAPI) : IndoorsDataSource {
  override fun fetchUploadToken(): Observable<String> {
    return indoorsAPI.fetchUploadToken()
        .map { resp -> resp.data.token }
  }

  override fun fetchRoomInfo(roomId: String): Observable<Room> {
    return indoorsAPI.fetchRoomInfo(roomId)
        .map { resp -> resp.data.room }
  }

  override fun fetchRoomList(limit: Int, offset: Int): Observable<RoomsData> {
    return indoorsAPI.fetchRoomList(limit, offset)
        .map { resp -> resp.data }
  }

  override fun clearFingerprints(roomId: String): Observable<ResponseTemplate> {
    return indoorsAPI.clearFingerprints(roomId)
  }

  override fun uploadWifi(roomId: String, wifiData: WifiData): Observable<ResponseTemplate> {
    return indoorsAPI.uploadWifi(roomId, wifiData)
  }

  override fun createRoom(room: Room): Observable<Room> {
    return indoorsAPI.createRoom(room)
        .map { resp -> resp.data.room }
  }

}