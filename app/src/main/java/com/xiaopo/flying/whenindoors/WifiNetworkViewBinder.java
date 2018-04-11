package com.xiaopo.flying.whenindoors;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaopo.flying.whenindoors.model.WiFiInfo;

import me.drakeet.multitype.ItemViewBinder;

/**
 * @author wupanjie
 */
public class WifiNetworkViewBinder extends ItemViewBinder<WiFiInfo, WifiNetworkViewBinder.ViewHolder> {

  @NonNull
  @Override
  protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
    View root = inflater.inflate(R.layout.item_wifi, parent, false);
    return new ViewHolder(root);
  }

  @Override
  protected void onBindViewHolder(@NonNull final ViewHolder holder, @NonNull final WiFiInfo wifi) {
    holder.tvSsid.setText(wifi.getSSID());
    holder.tvBssid.setText(wifi.getBSSID());
    holder.tvRssi.setText(wifi.getRSSI() + " dB");
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    private TextView tvSsid;
    private TextView tvBssid;
    private TextView tvRssi;

    ViewHolder(View itemView) {
      super(itemView);

      tvSsid = itemView.findViewById(R.id.tv_ssid);
      tvBssid = itemView.findViewById(R.id.tv_bssid);
      tvRssi = itemView.findViewById(R.id.tv_rssi);
    }
  }
}
