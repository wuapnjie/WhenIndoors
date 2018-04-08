package com.xiaopo.flying.whenindoors.model;

import android.support.annotation.NonNull;

import com.xiaopo.flying.awifi.WiFiNetwork;

/**
 * @author wupanjie
 */
public class SelectableWifiNetwork extends WiFiNetwork {

  private boolean selected;

  public SelectableWifiNetwork(@NonNull WiFiNetwork wiFiNetwork) {
    super(wiFiNetwork.getSSID(), wiFiNetwork.getBSSID(), wiFiNetwork.getCapabilities(), wiFiNetwork.getChannel(), wiFiNetwork.getRssi(), wiFiNetwork.getFrequency());
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }
}
