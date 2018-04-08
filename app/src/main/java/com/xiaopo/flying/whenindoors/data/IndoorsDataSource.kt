package com.xiaopo.flying.whenindoors.data

import com.xiaopo.flying.whenindoors.model.*
import io.reactivex.Observable

/**
 * @author wupanjie
 */
interface IndoorsDataSource {

  fun fetchRoomList(limit: Int, offset: Int): Observable<RoomsData>

  fun fetchRoomInfo(roomId: String): Observable<Room>

  fun clearFingerprints(roomId: String): Observable<ResponseTemplate>

  fun uploadWifi(roomId: String, wifiData: WifiData): Observable<ResponseTemplate>

  fun fetchUploadToken() : Observable<String>

  fun createRoom(room: Room): Observable<Room>
}