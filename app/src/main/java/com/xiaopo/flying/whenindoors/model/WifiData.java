package com.xiaopo.flying.whenindoors.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author wupanjie
 */
public class WifiData {

  private double x;
  private double y;
  @SerializedName("wifi_stats")
  private List<WiFiInfo> wiFiNetworks;

  public WifiData(double x, double y, List<WiFiInfo> networks) {
    this.x = x;
    this.y = y;
    this.wiFiNetworks = networks;
  }

  public double getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public List<WiFiInfo> getWiFiNetworks() {
    return wiFiNetworks;
  }

  public void setWiFiNetworks(List<WiFiInfo> wiFiNetworks) {
    this.wiFiNetworks = wiFiNetworks;
  }
}
