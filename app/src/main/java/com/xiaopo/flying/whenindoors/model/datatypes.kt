package com.xiaopo.flying.whenindoors.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * @author wupanjie
 */

data class WifiNetwork(val SSID: String,
                       val BSSID: String,
                       val RSSI: Int,
                       val capabilities: String,
                       val channel: Int,
                       val frequency: String) : Parcelable{
  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.readInt(),
      parcel.readString(),
      parcel.readInt(),
      parcel.readString()) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(SSID)
    parcel.writeString(BSSID)
    parcel.writeInt(RSSI)
    parcel.writeString(capabilities)
    parcel.writeInt(channel)
    parcel.writeString(frequency)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<WifiNetwork> {
    override fun createFromParcel(parcel: Parcel): WifiNetwork {
      return WifiNetwork(parcel)
    }

    override fun newArray(size: Int): Array<WifiNetwork?> {
      return arrayOfNulls(size)
    }
  }

}

data class RoomPosition(val x: Double, val y: Double, val wifi_stats: List<WifiNetwork>? = null) : Parcelable{

  val macRSSIMap: HashMap<String, Int>
    get() {
      val map = hashMapOf<String, Int>()
      wifi_stats?.forEach {
        map.put(it.BSSID, it.RSSI)
      }

      return map
    }

  constructor(parcel: Parcel) : this(
      parcel.readDouble(),
      parcel.readDouble(),
      parcel.createTypedArrayList(WifiNetwork)) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeDouble(x)
    parcel.writeDouble(y)
    parcel.writeTypedList(wifi_stats)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<RoomPosition> {
    override fun createFromParcel(parcel: Parcel): RoomPosition {
      return RoomPosition(parcel)
    }

    override fun newArray(size: Int): Array<RoomPosition?> {
      return arrayOfNulls(size)
    }
  }
}

data class Position(val x: Double, val y: Double)

data class Room(
    @SerializedName("_id") val id: String,
    @SerializedName("room_name") val name: String,
    var positions: List<RoomPosition> = arrayListOf(),
    @SerializedName("image_url") val imageUrl: String = "",
    val width : Double,
    val height : Double) : Parcelable{
  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.createTypedArrayList(RoomPosition),
      parcel.readString(),
      parcel.readDouble(),
      parcel.readDouble()) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(id)
    parcel.writeString(name)
    parcel.writeTypedList(positions)
    parcel.writeString(imageUrl)
    parcel.writeDouble(width)
    parcel.writeDouble(height)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<Room> {
    override fun createFromParcel(parcel: Parcel): Room {
      return Room(parcel)
    }

    override fun newArray(size: Int): Array<Room?> {
      return arrayOfNulls(size)
    }
  }

}

data class RoomsData(
    val rooms: List<Room>
)

data class RoomData(
    val room: Room
)

data class TokenData(
    val token : String
)
