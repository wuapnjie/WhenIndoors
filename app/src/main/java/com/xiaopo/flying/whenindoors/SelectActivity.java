package com.xiaopo.flying.whenindoors;

import android.Manifest;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaopo.flying.awifi.AWifi;
import com.xiaopo.flying.awifi.WiFiNetwork;
import com.xiaopo.flying.whenindoors.model.Room;
import com.xiaopo.flying.whenindoors.model.SelectableWifiNetwork;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import me.drakeet.multitype.MultiTypeAdapter;

public class SelectActivity extends AppCompatActivity implements OnItemSelectedListener<SelectableWifiNetwork>, View.OnClickListener {
  private static final int PERMISSION_CODE = 1;
  public static final int REQUEST_CODE = 12345;

  private RecyclerView wifiList;
  private MultiTypeAdapter adapter;

  private ArrayList<SelectableWifiNetwork> items = new ArrayList<>();
  private Toolbar toolbar;
  private FloatingActionButton fab;
  private TextView tvSelectedCount;
  private final TreeSet<Integer> selectedPositions = new TreeSet<>();

  private Room room;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select);

    room = getIntent().getParcelableExtra("room");

    initView();

    AndPermission.with(this)
        .permission(Manifest.permission.ACCESS_COARSE_LOCATION)
        .requestCode(PERMISSION_CODE)
        .callback(this)
        .start();
  }

  private void initView() {
    wifiList = findViewById(R.id.wifi_list);
    fab = findViewById(R.id.fab);
    toolbar = findViewById(R.id.toolbar);
    toolbar.setTitle("正在扫描WiFi...");
    tvSelectedCount = findViewById(R.id.tv_selected_count);

    wifiList.setLayoutManager(new LinearLayoutManager(this));
    adapter = new MultiTypeAdapter();
    SelectableWifiNetworkViewBinder wifiNetworkViewBinder = new SelectableWifiNetworkViewBinder(selectedPositions);
    wifiNetworkViewBinder.setOnItemSelectedListener(this);
    adapter.register(SelectableWifiNetwork.class, wifiNetworkViewBinder);
    wifiList.setAdapter(adapter);

    setSupportActionBar(toolbar);

    fab.setOnClickListener(this);
  }

  @PermissionYes(PERMISSION_CODE)
  private void getPermissionYes(List<String> grantedPermissions) {
    startScanning();
  }

  @PermissionNo(PERMISSION_CODE)
  private void getPermissionNo(List<String> deniedPermissions) {
    Toast.makeText(this, "必须要权限呀", Toast.LENGTH_SHORT).show();
  }

  private void startScanning() {
    final ArrayList<SelectableWifiNetwork> wifiNetworks = new ArrayList<>();
    AWifi.from(getApplicationContext())
        .subscribe(result -> {
              for (ScanResult scanResult : result) {
                final SelectableWifiNetwork network = new SelectableWifiNetwork(WiFiNetwork.from(scanResult));
                wifiNetworks.add(network);
              }


              toolbar.setTitle("请选择要上传的WiFi");

              items.clear();
              items.addAll(wifiNetworks);
              adapter.setItems(items);
              adapter.notifyDataSetChanged();

              Toast.makeText(SelectActivity.this, "获取Wifi信息成功", Toast.LENGTH_SHORT).show();
            },
            throwable -> Log.e("RX-WIFI-SingleScan", throwable.getMessage()));
  }

  @Override
  public void onPhotoSelected(SelectableWifiNetwork item, int position) {
    if (selectedPositions.isEmpty()) {
      fab.setImageResource(R.drawable.ic_refresh_white_24dp);
    } else {
      fab.setImageResource(R.drawable.ic_done_white_24dp);
    }
    tvSelectedCount.setText(String.valueOf(selectedPositions.size()));
  }

  @Override
  public void onClick(View v) {
    final int selectedSize = selectedPositions.size();

    if (selectedSize == 0) {
      startScanning();
      return;
    }

    if (selectedSize < 2) {
      Toast.makeText(this, "你必须选择2个Wifi以上", Toast.LENGTH_SHORT).show();
      return;
    }

    ArrayList<String> bssidFilters = new ArrayList<>(selectedSize);
    for (Integer selectedPosition : selectedPositions) {
      bssidFilters.add(items.get(selectedPosition).getBSSID());
    }

    Intent intent = new Intent(this, UploadActivity.class);
    intent.putStringArrayListExtra("bssid_filter", bssidFilters);
    intent.putExtra("room", room);

    startActivityForResult(intent, 12345);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE & resultCode == RESULT_OK) {
      setResult(RESULT_OK);
      finish();
    }
  }
}
