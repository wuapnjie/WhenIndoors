/*
 * Copyright (c) 2015 Ennova S.r.l.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 *  of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.xiaopo.flying.awifi;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import com.xiaopo.flying.awifi.internal.WifiChannel;
import com.xiaopo.flying.awifi.internal.WifiFrequency;

import io.reactivex.annotations.NonNull;


/**
 * This class represents the minimum amount of information needed for showing the different networks
 * on a graph
 */
public class WiFiNetwork implements Parcelable {

  private final String SSID;
  private final String BSSID;
  private final String capabilities;
  private final int channel;
  private final int RSSI;
  private final WifiFrequency frequency;

  public static WiFiNetwork from(ScanResult scanResult) {
    WifiChannel channel = WifiChannel.fromFrequency(scanResult.frequency);

    return new WiFiNetwork(
        scanResult.SSID,
        scanResult.BSSID,
        scanResult.capabilities,
        channel.channel,
        scanResult.level,
        channel.frequency);
  }

  public WiFiNetwork(@NonNull String SSID, @NonNull String BSSID, @NonNull String capabilities,
                     int channel, int rssi, WifiFrequency frequency) {

    this.SSID = SSID;
    this.BSSID = BSSID;
    this.capabilities = capabilities;
    this.channel = channel;
    this.RSSI = rssi;
    this.frequency = frequency;
  }

  @NonNull
  public String getSSID() {
    return SSID;
  }

  @NonNull
  public String getBSSID() {
    return BSSID;
  }

  @NonNull
  public String getCapabilities() {
    return capabilities;
  }

  public int getChannel() {
    return channel;
  }

  public int getRssi() {
    return RSSI;
  }

  public WifiFrequency getFrequency() {
    return frequency;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WiFiNetwork that = (WiFiNetwork) o;

    return BSSID.equals(that.BSSID);

  }

  @Override
  public int hashCode() {
    return BSSID.hashCode();
  }

  @Override
  public String toString() {
    return "WiFiNetwork{" +
        "SSID='" + SSID + '\'' +
        ", BSSID='" + BSSID + '\'' +
        ", capabilities='" + capabilities + '\'' +
        ", channel=" + channel +
        ", rssi=" + RSSI +
        ", frequency=" + frequency +
        '}';
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.SSID);
    dest.writeString(this.BSSID);
    dest.writeString(this.capabilities);
    dest.writeInt(this.channel);
    dest.writeInt(this.RSSI);
    dest.writeInt(this.frequency == null ? -1 : this.frequency.ordinal());
  }

  protected WiFiNetwork(Parcel in) {
    this.SSID = in.readString();
    this.BSSID = in.readString();
    this.capabilities = in.readString();
    this.channel = in.readInt();
    this.RSSI = in.readInt();
    int tmpFrequency = in.readInt();
    this.frequency = tmpFrequency == -1 ? null : WifiFrequency.values()[tmpFrequency];
  }

  public static final Creator<WiFiNetwork> CREATOR = new Creator<WiFiNetwork>() {
    @Override
    public WiFiNetwork createFromParcel(Parcel source) {
      return new WiFiNetwork(source);
    }

    @Override
    public WiFiNetwork[] newArray(int size) {
      return new WiFiNetwork[size];
    }
  };
}
