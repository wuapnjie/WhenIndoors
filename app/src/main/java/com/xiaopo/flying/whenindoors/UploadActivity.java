package com.xiaopo.flying.whenindoors;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import me.drakeet.multitype.MultiTypeAdapter;

public class UploadActivity extends AppCompatActivity {
  private static final String TAG = "UploadActivity";
  private static final int PERMISSION_CODE = 1124;
  private static final int REQUEST_PICK_POSITION = 3073;

  private boolean started = false;

  private Button btnControl;
  //  private Button btnUpload;
  private Button btnPickPosition;
  private TextView tvPickedPosition;
  private Toolbar toolbar;
  private EditText etPositionX;
  private EditText etPositionY;
  private TextView tvLog;

  private final ArrayList<WiFiInfo> wifiInfos = new ArrayList<>();
  private ArrayList<String> bssidFilters;
  private RecyclerView wifiList;
  private MultiTypeAdapter adapter;

  private RoomViewModel viewModel;
  private SharedPreferences sharedPreferences;

  private Room room;
  private ProgressDialog waitForScan;
  private Disposable disposable;

  private int maxScanCount;
  private int currentScanCount;

  private MediaPlayer mediaPlayer = new MediaPlayer();

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
//    scanning();
  }

  @PermissionNo(PERMISSION_CODE)
  private void getPermissionNo(List<String> deniedPermissions) {
    Toast.makeText(this, "必须要权限呀", Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onResume() {
    super.onResume();
    String scanCount = sharedPreferences.getString("pref_scan_count", "10");
    maxScanCount = Integer.parseInt(scanCount);
  }

  private void scanning() {
    if (!started) return;

//    waitForScan.show();
    Log.d(TAG, "开始扫描");

    disposable = AWifi.from(getApplicationContext())
        .subscribe(scanResults -> {
              wifiInfos.clear();
              for (ScanResult scanResult : scanResults) {
                WiFiNetwork wiFiNetwork = WiFiNetwork.from(scanResult);
                if (!bssidFilters.contains(wiFiNetwork.getBSSID())) {
                  wifiInfos.add(new WiFiInfo(wiFiNetwork.getSSID(), wiFiNetwork.getBSSID(), wiFiNetwork.getRssi()));
                }
              }
//              waitForScan.dismiss();

              adapter.setItems(wifiInfos);
              adapter.notifyDataSetChanged();
              Log.d(TAG, "扫描结束，开始上传");
              uploadWifiData();
            },
            throwable -> Log.e("RX-WIFI-SingleScan", throwable.getMessage())
        );

  }

  private void initView() {
    btnControl = findViewById(R.id.btn_scan);
//    btnUpload = findViewById(R.id.btn_upload);
    btnPickPosition = findViewById(R.id.btn_pick_position);
    tvPickedPosition = findViewById(R.id.tv_picked_position);
    wifiList = findViewById(R.id.wifi_list);
    toolbar = findViewById(R.id.toolbar);
    etPositionX = findViewById(R.id.et_position_x);
    etPositionY = findViewById(R.id.et_position_y);
    tvLog = findViewById(R.id.tv_log);

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
    waitForScan.setButton(
        DialogInterface.BUTTON_NEGATIVE,
        "取消",
        (dialog, which) -> {
          dialog.dismiss();
          if (disposable != null) {
            disposable.dispose();
          }
        });
    waitForScan.setCancelable(false);
  }

  private void initListener() {
    btnControl.setOnClickListener(v -> {
      if (started) {
        started = false;
        setLog("");
        btnControl.setText("开始采集");
      } else {
        if (TextUtils.isEmpty(etPositionX.getText().toString().trim()) ||
            TextUtils.isEmpty(etPositionY.getText().toString().trim())) {
          Toast.makeText(this, "请选取或输入坐标", Toast.LENGTH_SHORT).show();
          return;
        }
        started = true;
        btnControl.setText("暂停采集");
        scanning();
      }
    });
//    btnUpload.setOnClickListener(v -> uploadWifiData());
    btnPickPosition.setOnClickListener(v -> pickUploadPosition());
  }

  private void pickUploadPosition() {
    Intent intent = new Intent(this, SelectLocateActivity.class);
    intent.putExtra("room", room);
    startActivityForResult(intent, REQUEST_PICK_POSITION);
  }

  private void uploadWifiData() {
    if (!started) return;

    if (wifiInfos.isEmpty()) {
      Toast.makeText(this, "请扫描Wifi", Toast.LENGTH_SHORT).show();
      return;
    }
    final double uploadX =
        Double.valueOf(etPositionX.getText().toString().trim());
    final double uploadY =
        Double.valueOf(etPositionY.getText().toString().trim());

    Log.d(TAG, "上传点坐标为 (" + uploadX + "," + uploadY + ")");

    WifiData wifiData = new WifiData(uploadX, uploadY, new ArrayList<>(wifiInfos));

    viewModel.uploadWifi(room.getId(), wifiData)
        .observe(this, result -> {
          if (result != null) {
            result.fold(
                responseTemplate -> {
                  if (responseTemplate.getStatus() == 0) {

                    room.getPositions().add(new RoomPosition(uploadX, uploadY));
                    currentScanCount++;
                    setLog("第 " + currentScanCount + " 次上传成功，坐标:" + "(" + uploadX + "," + uploadY + ")");
                    Log.d(TAG, "上传成功");
                  } else {
                    setLog("上传失败");
                    Log.d(TAG, "上传失败");
                  }

                  if (currentScanCount >= maxScanCount - 1) {
                    currentScanCount = 0;
                    started = false;
                    setLog("采集 " + maxScanCount + " 次已完成");
                    loadAllDataFinish();
                    btnControl.setText("开始采集");
                  } else {
                    scanning();
                  }
                  return null;
                },
                throwable -> {
                  setLog("上传失败");
                  Log.d(TAG, "上传失败");
                  scanning();
                  return null;
                }
            );
          }
        });

  }

  private void setLog(String log) {
    tvLog.setText(log);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_PICK_POSITION && resultCode == RESULT_OK) {
      double pickedX = data.getDoubleExtra("pickedX", -1);
      double pickedY = data.getDoubleExtra("pickedY", -1);
      displayPickedPosition(pickedX, pickedY);
    }

  }

  private void displayPickedPosition(double pickedX, double pickedY) {
    etPositionX.setText(pickedX + "");
    etPositionY.setText(pickedY + "");
    tvPickedPosition.setText(getString(R.string.template_position, pickedX, pickedY));
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  private void loadAllDataFinish() {
    AssetFileDescriptor afd = null ;
    try {
      afd = getApplicationContext().getAssets().openFd("success.mp3");
      mediaPlayer.reset();
      mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
      mediaPlayer.prepareAsync();
      mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          mp.start();
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
