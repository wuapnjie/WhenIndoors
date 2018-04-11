package com.xiaopo.flying.whenindoors.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * @author wupanjie
 */

data class WiFiInfo(val SSID: String,
                    val BSSID: String,
                    val RSSI: Int) : Parcelable {
  constructor(parcel: Parcel) : this(
      parcel.readString(),
      parcel.readString(),
      parcel.readInt()) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(SSID)
    parcel.writeString(BSSID)
    parcel.writeInt(RSSI)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<WiFiInfo> {
    override fun createFromParcel(parcel: Parcel): WiFiInfo {
      return WiFiInfo(parcel)
    }

    override fun newArray(size: Int): Array<WiFiInfo?> {
      return arrayOfNulls(size)
    }
  }

}

data class RoomPosition(val x: Double, val y: Double) : Parcelable {

  constructor(parcel: Parcel) : this(
      parcel.readDouble(),
      parcel.readDouble()) {
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeDouble(x)
    parcel.writeDouble(y)
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
    val width: Double,
    val height: Double) : Parcelable {
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

data class RoomInfo(
    @SerializedName("_id") val id: String,
    @SerializedName("room_name") val name: String,
    @SerializedName("image_url") val imageUrl: String = "",
    val width: Double,
    val height: Double,
    val positions_count: Int)


data class RoomsData(
    val rooms: List<RoomInfo>
)

data class RoomData(
    val room: Room
)

data class TokenData(
    val token: String
)

data class NeedComputePosition(
    val fingerprint: List<WiFiInfo>
)