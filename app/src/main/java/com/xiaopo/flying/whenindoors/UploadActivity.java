package com.xiaopo.flying.whenindoors;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaopo.flying.awifi.AWifi;
import com.xiaopo.flying.awifi.WiFiNetwork;
import com.xiaopo.flying.whenindoors.model.Room;
import com.xiaopo.flying.whenindoors.model.RoomPosition;
import com.xiaopo.flying.whenindoors.model.WiFiInfo;
import com.xiaopo.flying.whenindoors.model.WifiData;
import com.xiaopo.flying.whenindoors.ui.page.locate.SelectLocateActivity;
import com.xiaopo.flying.whenindoors.ui.page.setting.SettingActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import me.drakeet.multitype.MultiTypeAdapter;

public class UploadActivity extends AppCompatActivity {
  private static final String TAG = "UploadActivity";
  private static final int PERMISSION_CODE = 1124;
  private static final int REQUEST_PICK_POSITION = 3073;

  private Button btnScan;
  private Button btnUpload;
  private Button btnPickPosition;
  private TextView tvPickedPosition;
  private Toolbar toolbar;

  private final ArrayList<WiFiInfo> wifiInfos = new ArrayList<>();
  private ArrayList<String> bssidFilters;
  private RecyclerView wifiList;
  private MultiTypeAdapter adapter;

  private RoomViewModel viewModel;
  private SharedPreferences sharedPreferences;

  private Room room;
  private ProgressDialog waitForScan;
  private Disposable disposable;

  private double pickedX = -1;
  private double pickedY = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_upload);

    bssidFilters = getIntent().getStringArrayListExtra("bssid_filter");
    room = getIntent().getParcelableExtra("room");
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    initView();
    initListener();

    viewModel = ViewModelProviders.of(this).get(RoomViewModel.class);

    AndPermission.with(this)
        .permission(Manifest.permission.ACCESS_WIFI_STATE)
        .requestCode(PERMISSION_CODE)
        .callback(this)
        .start();

    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

  }

  @PermissionYes(PERMISSION_CODE)
  private void getPermissionYes(List<String> grantedPermissions) {
//    startScanning();
  }

  @PermissionNo(PERMISSION_CODE)
  private void getPermissionNo(List<String> deniedPermissions) {
    Toast.makeText(this, "必须要权限呀", Toast.LENGTH_SHORT).show();
  }


  private void startScanning() {
    waitForScan.show();

    wifiInfos.clear();
    adapter.setItems(wifiInfos);
    adapter.notifyDataSetChanged();


    String scanCount = sharedPreferences.getString("pref_scan_count", "10");
    int count = 1;

    disposable = AWifi.from(getApplicationContext())
        .subscribe(scanResults -> {

        });

    disposable = AWifi.from(getApplicationContext(), count)
        .subscribe(scanResults -> {
              wifiInfos.clear();
              for (ScanResult scanResult : scanResults) {
                WiFiNetwork wiFiNetwork = WiFiNetwork.from(scanResult);
                if (bssidFilters.contains(wiFiNetwork.getBSSID())) {
                  wifiInfos.add(new WiFiInfo(wiFiNetwork.getSSID(), wiFiNetwork.getBSSID(), wiFiNetwork.getRssi()));
                }
              }
              waitForScan.dismiss();


              adapter.setItems(wifiInfos);
              adapter.notifyDataSetChanged();
            },
            throwable -> Log.e("RX-WIFI-SingleScan", throwable.getMessage())
        );

  }

  private void initView() {
    btnScan = findViewById(R.id.btn_scan);
    btnUpload = findViewById(R.id.btn_upload);
    btnPickPosition = findViewById(R.id.btn_pick_position);
    tvPickedPosition = findViewById(R.id.tv_picked_position);
    wifiList = findViewById(R.id.wifi_list);
    toolbar = findViewById(R.id.toolbar);

    wifiList.setLayoutManager(new LinearLayoutManager(this));
    adapter = new MultiTypeAdapter();
    adapter.register(WiFiInfo.class, new WifiNetworkViewBinder());
    wifiList.setAdapter(adapter);
    toolbar.inflateMenu(R.menu.menu_upload);
    toolbar.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {
        case R.id.action_done:
          setResult(RESULT_OK);
          finish();
          break;

        case R.id.action_settings:
          Intent intent = new Intent(UploadActivity.this, SettingActivity.class);
          startActivity(intent);
          break;
      }

      return true;
    });

    waitForScan = new ProgressDialog(this);
    waitForScan.setMessage("正在扫描中...");
    waitForScan.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        if (disposable != null) {
          disposable.dispose();
        }
      }
    });
    waitForScan.setCancelable(false);
  }

  private void initListener() {
    btnScan.setOnClickListener(v -> startScanning());
    btnUpload.setOnClickListener(v -> uploadWifiData());
    btnPickPosition.setOnClickListener(v -> pickUploadPosition());
  }

  private void pickUploadPosition() {
    Intent intent = new Intent(this, SelectLocateActivity.class);
    intent.putExtra("room", room);
    startActivityForResult(intent, REQUEST_PICK_POSITION);
  }

  private void uploadWifiData() {
    if (wifiInfos.isEmpty()) {
      Toast.makeText(this, "请扫描Wifi", Toast.LENGTH_SHORT).show();
      return;
    }


    if (pickedX == -1 || pickedY == -1) {
      Toast.makeText(this, "请选取坐标", Toast.LENGTH_SHORT).show();
      return;
    }


    WifiData wifiData = new WifiData(pickedX, pickedY, new ArrayList<>(wifiInfos));

    viewModel.uploadWifi(room.getId(), wifiData)
        .observe(this, result -> {
          if (result != null) {
            result.fold(
                responseTemplate -> {
                  if (responseTemplate.getStatus() == 0) {
                    room.getPositions().add(new RoomPosition(pickedX, pickedY));
                    Toast.makeText(UploadActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                  } else {
                    Toast.makeText(UploadActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                  }
                  return null;
                },
                throwable -> {
                  Toast.makeText(UploadActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                  return null;
                }
            );
          }
        });

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_PICK_POSITION && resultCode == RESULT_OK) {
      pickedX = data.getDoubleExtra("pickedX", -1);
      pickedY = data.getDoubleExtra("pickedY", -1);
      displayPickedPosition();
    }

  }

  private void displayPickedPosition() {
    tvPickedPosition.setText(getString(R.string.template_position, pickedX, pickedY));
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }
}
