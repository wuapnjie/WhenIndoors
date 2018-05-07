package com.xiaopo.flying.whenindoors.data.remote

import com.xiaopo.flying.whenindoors.model.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author wupanjie
 */
interface IndoorsAPI {

  @GET("room/list")
  fun fetchRoomList(
      @Query(QUERY_LIMIT) limit: Int,
      @Query(QUERY_OFFSET) offset: Int
  ): Observable<DataResponseTemplate<RoomsData>>

  @GET("room/info")
  fun fetchRoomInfo(
      @Query(QUERY_ROOM_ID) roomId: String
  ): Observable<DataResponseTemplate<RoomData>>

  @POST("room/positions/clear")
  fun clearFingerprints(
      @Query(QUERY_ROOM_ID) roomId: String
  ): Observable<ResponseTemplate>

  @POST("wifi/upload")
  fun uploadWifi(
      @Query(QUERY_ROOM_ID) roomId: String,
      @Body wifiData: WifiData
  ): Observable<ResponseTemplate>

  @GET("upload/token")
  fun fetchUploadToken(): Observable<DataResponseTemplate<TokenData>>

  @POST("room/new")
  fun createRoom(
      @Body room: Room
  ): Observable<DataResponseTemplate<RoomData>>

  @POST("room/location")
  fun fetchLocation(
      @Query(QUERY_ROOM_ID) roomId: String,
      @Body needComputePosition: NeedComputePosition,
      @Query("k") k : Int
  ): Observable<DataResponseTemplate<RoomPosition>>
}