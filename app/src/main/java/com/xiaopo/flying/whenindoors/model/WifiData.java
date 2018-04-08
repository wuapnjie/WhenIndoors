package com.xiaopo.flying.whenindoors.model;

import com.google.gson.annotations.SerializedName;
import com.xiaopo.flying.awifi.WiFiNetwork;

import java.util.List;

/**
 * @author wupanjie
 */
public class WifiData {

  private double x;
  private double y;
  @SerializedName("wifi_stats")
  private List<WiFiNetwork> wiFiNetworks;

  public WifiData(double x, double y , List<WiFiNetwork> networks) {
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

  public List<WiFiNetwork> getWiFiNetworks() {
    return wiFiNetworks;
  }

  public void setWiFiNetworks(List<WiFiNetwork> wiFiNetworks) {
    this.wiFiNetworks = wiFiNetworks;
  }
}
