package com.xiaopo.flying.whenindoors;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xiaopo.flying.whenindoors.model.SelectableWifiNetwork;

import java.util.TreeSet;

import me.drakeet.multitype.ItemViewBinder;

/**
 * @author wupanjie
 */
public class SelectableWifiNetworkViewBinder extends ItemViewBinder<SelectableWifiNetwork, SelectableWifiNetworkViewBinder.ViewHolder> {

  private final TreeSet<Integer> selectedPositions;
  private OnItemSelectedListener<SelectableWifiNetwork> onItemSelectedListener;

  public SelectableWifiNetworkViewBinder(TreeSet<Integer> selectedPositions) {
    this.selectedPositions = selectedPositions;
  }

  public void setOnItemSelectedListener(OnItemSelectedListener<SelectableWifiNetwork> onItemSelectedListener) {
    this.onItemSelectedListener = onItemSelectedListener;
  }

  @NonNull
  @Override
  protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
    View root = inflater.inflate(R.layout.item_wifi, parent, false);
    return new ViewHolder(root);
  }

  @Override
  protected void onBindViewHolder(@NonNull final ViewHolder holder, @NonNull final SelectableWifiNetwork wifi) {
    holder.tvSsid.setText(wifi.getSSID());
    holder.tvBssid.setText(wifi.getBSSID());
    holder.tvRssi.setText(wifi.getRssi() + " dB");

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (wifi.isSelected()) {
          setUnselected(holder);
          wifi.setSelected(false);
          selectedPositions.remove(holder.getAdapterPosition());

          if (onItemSelectedListener != null) {
            onItemSelectedListener.onPhotoSelected(wifi, holder.getAdapterPosition());
          }
        } else {
          setSelected(holder);
          wifi.setSelected(true);
          selectedPositions.add(holder.getAdapterPosition());

          if (onItemSelectedListener != null) {
            onItemSelectedListener.onPhotoSelected(wifi, holder.getAdapterPosition());
          }
        }
      }
    });

    if (wifi.isSelected()) {
      setSelected(holder);
    } else {
      setUnselected(holder);
    }
  }

  private void setSelected(ViewHolder holder) {
    holder.tvSsid.setTextColor(Color.RED);
  }

  private void setUnselected(ViewHolder holder) {
    holder.tvSsid.setTextColor(Color.BLACK);
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
