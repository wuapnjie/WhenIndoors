/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaopo.flying.whenindoors.ui.page.bluetooth.bluetoothchat;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaopo.flying.awifi.AWifi;
import com.xiaopo.flying.awifi.WiFiNetwork;
import com.xiaopo.flying.whenindoors.R;
import com.xiaopo.flying.whenindoors.RoomViewModel;
import com.xiaopo.flying.whenindoors.model.Position;
import com.xiaopo.flying.whenindoors.model.WiFiInfo;
import com.xiaopo.flying.whenindoors.model.WifiData;
import com.xiaopo.flying.whenindoors.ui.page.bluetooth.common.logger.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {

  private static final String TAG = "BluetoothChatFragment";

  // Intent request codes
  private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
  private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
  private static final int REQUEST_ENABLE_BT = 3;

  // Layout Views
  private ListView conversationView;
  private EditText outEditText;
  private Button sendButton;

  /**
   * Name of the connected device
   */
  private String connectedDeviceName = null;

  /**
   * Array adapter for the conversation thread
   */
  private ArrayAdapter<String> conversationArrayAdapter;

  /**
   * String buffer for outgoing messages
   */
  private StringBuffer outStringBuffer;

  /**
   * Local Bluetooth adapter
   */
  private BluetoothAdapter bluetoothAdapter = null;

  /**
   * Member object for the chat services
   */
  private BluetoothChatService chatService = null;

  /**
   * The Handler that gets information back from the BluetoothChatService
   */
  private Handler handler;

  private RoomViewModel roomViewModel;

  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private Subject<Position> positionSubject = PublishSubject.create();
  private Disposable positionDisposable;

  private String roomId;

  private Position currentPosition;

  private void uploadData(String readMessage) {
    try {
      String[] positionInfos = readMessage.split(" ");
      double x = Double.parseDouble(positionInfos[1]);
      double y = Double.parseDouble(positionInfos[2]);

      currentPosition = new Position(x, y);
      positionSubject.onNext(currentPosition);
    } catch (Exception e) {
      Log.e(TAG, "Upload Error", e);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    roomId = getArguments().getString("room_id");
    handler = new BluetoothHandler(this);
    // Get local Bluetooth adapter
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    roomViewModel = ViewModelProviders.of(this).get(RoomViewModel.class);

    // If the adapter is null, then Bluetooth is not supported
    if (bluetoothAdapter == null) {
      FragmentActivity activity = getActivity();
      Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
      activity.finish();
    }

    positionDisposable = positionSubject.debounce(3, TimeUnit.SECONDS).subscribe(this::scanWifiFingerprint);
  }

  private void scanWifiFingerprint(Position position) {
    if (currentPosition.getX() != position.getX() || currentPosition.getY() != position.getY()){
      Log.d(TAG, "position changed, stop scan");
      return;
    }

    Log.d(TAG, "START SCAN WIFI");
    Disposable disposable = AWifi.from(getContext())
        .subscribe(scanResults -> {
          List<WiFiInfo> wiFiInfos = new ArrayList<>(scanResults.size());
          for (ScanResult scanResult : scanResults) {
            WiFiNetwork wiFiNetwork = WiFiNetwork.from(scanResult);
            wiFiInfos.add(new WiFiInfo(wiFiNetwork.getSSID(), wiFiNetwork.getBSSID(), wiFiNetwork.getRssi()));
          }

          final WifiData wifiData = new WifiData(position.getX(), position.getY(), wiFiInfos);
          uplaodWifiFingerprint(wifiData);
        });

    compositeDisposable.add(disposable);
  }

  private void uplaodWifiFingerprint(WifiData wifiData) {
    if (currentPosition.getX() != wifiData.getX() || currentPosition.getY() != wifiData.getY()){
      Log.d(TAG, "position changed, drop data");
      return;
    }

    Log.d(TAG, "UPLOAD WIFI FINGERPRINT");
    roomViewModel.uploadWifi(roomId, wifiData)
        .observe(this, result -> {
          if (result != null) {
            result.fold(
                responseTemplate -> {
                  if (responseTemplate.getStatus() == 0) {
                    Log.d(TAG, "上传成功:" + "(" + wifiData.getX() + "," + wifiData.getY() + ")");
                  } else {
                    Log.d(TAG, "上传失败" + "(" + wifiData.getX() + "," + wifiData.getY() + ")");
                  }

                  scanWifiFingerprint(currentPosition);

                  return null;
                },
                throwable -> {
                  Log.e(TAG, "上传失败", throwable);
                  return null;
                }
            );
          }
        });
  }

  @Override
  public void onStart() {
    super.onStart();
    // If BT is not on, request that it be enabled.
    // setupChat() will then be called during onActivityResult
    if (!bluetoothAdapter.isEnabled()) {
      Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
      // Otherwise, setup the chat session
    } else if (chatService == null) {
      setupChat();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (chatService != null) {
      chatService.stop();
    }

    compositeDisposable.dispose();
    positionDisposable.dispose();
  }

  @Override
  public void onResume() {
    super.onResume();

    // Performing this check in onResume() covers the case in which BT was
    // not enabled during onStart(), so we were paused to enable it...
    // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
    if (chatService != null) {
      // Only if the state is STATE_NONE, do we know that we haven't started already
      if (chatService.getState() == BluetoothChatService.STATE_NONE) {
        // Start the Bluetooth chat services
        chatService.start();
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    conversationView = view.findViewById(R.id.in);
    outEditText = view.findViewById(R.id.edit_text_out);
    sendButton = view.findViewById(R.id.button_send);
  }

  /**
   * Set up the UI and background operations for chat.
   */
  private void setupChat() {
    Log.d(TAG, "setupChat()");

    // Initialize the array adapter for the conversation thread
    conversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

    conversationView.setAdapter(conversationArrayAdapter);

    // Initialize the compose field with a listener for the return key
    outEditText.setOnEditorActionListener(writeListener);

    // Initialize the send button with a listener that for click events
    sendButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // Send a message using content of the edit text widget
        View view = getView();
        if (null != view) {
          TextView textView = view.findViewById(R.id.edit_text_out);
          String message = textView.getText().toString();
          sendMessage(message);
        }
      }
    });

    // Initialize the BluetoothChatService to perform bluetooth connections
    chatService = new BluetoothChatService(getActivity(), handler);

    // Initialize the buffer for outgoing messages
    outStringBuffer = new StringBuffer("");
  }

  /**
   * Makes this device discoverable for 300 seconds (5 minutes).
   */
  private void ensureDiscoverable() {
    if (bluetoothAdapter.getScanMode() !=
        BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
      Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
      startActivity(discoverableIntent);
    }
  }

  /**
   * Sends a message.
   *
   * @param message A string of text to send.
   */
  private void sendMessage(String message) {
    // Check that we're actually connected before trying anything
    if (chatService.getState() != BluetoothChatService.STATE_CONNECTED) {
      Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
      return;
    }

    // Check that there's actually something to send
    if (message.length() > 0) {
      // Get the message bytes and tell the BluetoothChatService to write
      byte[] send = message.getBytes();
      chatService.write(send);

      // Reset out string buffer to zero and clear the edit text field
      outStringBuffer.setLength(0);
      outEditText.setText(outStringBuffer);
    }
  }

  /**
   * The action listener for the EditText widget, to listen for the return key
   */
  private TextView.OnEditorActionListener writeListener
      = (view, actionId, event) -> {
    // If the action is a key-up event on the return key, send the message
    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
      String message = view.getText().toString();
      sendMessage(message);
    }
    return true;
  };

  /**
   * Updates the status on the action bar.
   *
   * @param resId a string resource ID
   */
  private void setStatus(int resId) {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (null == activity) {
      return;
    }
    final ActionBar actionBar = activity.getSupportActionBar();
    if (null == actionBar) {
      return;
    }
    actionBar.setSubtitle(resId);
  }

  /**
   * Updates the status on the action bar.
   *
   * @param subTitle status
   */
  private void setStatus(CharSequence subTitle) {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (null == activity) {
      return;
    }
    final ActionBar actionBar = activity.getSupportActionBar();
    if (null == actionBar) {
      return;
    }
    actionBar.setSubtitle(subTitle);
  }


  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CONNECT_DEVICE_SECURE:
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
          connectDevice(data, true);
        }
        break;
      case REQUEST_CONNECT_DEVICE_INSECURE:
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
          connectDevice(data, false);
        }
        break;
      case REQUEST_ENABLE_BT:
        // When the request to enable Bluetooth returns
        if (resultCode == Activity.RESULT_OK) {
          // Bluetooth is now enabled, so set up a chat session
          setupChat();
        } else {
          // User did not enable Bluetooth or an error occurred
          Log.d(TAG, "BT not enabled");
          Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
              Toast.LENGTH_SHORT).show();
          getActivity().finish();
        }
    }
  }

  /**
   * Establish connection with other device
   *
   * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
   * @param secure Socket Security type - Secure (true) , Insecure (false)
   */
  private void connectDevice(Intent data, boolean secure) {
    // Get the device MAC address
    String address = data.getExtras()
        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
    // Get the BluetoothDevice object
    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
    // Attempt to connect to the device
    chatService.connect(device, secure);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.bluetooth_chat, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.secure_connect_scan: {
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
        return true;
      }
      case R.id.insecure_connect_scan: {
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
        return true;
      }
      case R.id.discoverable: {
        // Ensure this device is discoverable by others
        ensureDiscoverable();
        return true;
      }
    }
    return false;
  }

  private static class BluetoothHandler extends Handler {
    private final WeakReference<BluetoothChatFragment> reference;

    private BluetoothHandler(BluetoothChatFragment instance) {
      this.reference = new WeakReference<>(instance);
    }

    @Override
    public void handleMessage(Message msg) {
      BluetoothChatFragment fragment = reference.get();

      if (fragment == null) {
        return;
      }

      FragmentActivity activity = fragment.getActivity();
      switch (msg.what) {
        case Constants.MESSAGE_STATE_CHANGE:
          switch (msg.arg1) {
            case BluetoothChatService.STATE_CONNECTED:
              fragment.setStatus(fragment.getString(R.string.title_connected_to, fragment.connectedDeviceName));
              fragment.conversationArrayAdapter.clear();
              break;
            case BluetoothChatService.STATE_CONNECTING:
              fragment.setStatus(R.string.title_connecting);
              break;
            case BluetoothChatService.STATE_LISTEN:
            case BluetoothChatService.STATE_NONE:
              fragment.setStatus(R.string.title_not_connected);
              break;
          }
          break;
        case Constants.MESSAGE_WRITE:
          byte[] writeBuf = (byte[]) msg.obj;
          // construct a string from the buffer
          String writeMessage = new String(writeBuf);
          fragment.conversationArrayAdapter.add("Me:  " + writeMessage);
          break;
        case Constants.MESSAGE_READ:
          byte[] readBuf = (byte[]) msg.obj;
          // construct a string from the valid bytes in the buffer
          String readMessage = new String(readBuf, 0, msg.arg1);
          fragment.conversationArrayAdapter.add(fragment.connectedDeviceName + ":  " + readMessage);

          fragment.uploadData(readMessage);
          break;
        case Constants.MESSAGE_DEVICE_NAME:
          // save the connected device's name
          fragment.connectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
          if (null != activity) {
            Toast.makeText(activity, "Connected to "
                + fragment.connectedDeviceName, Toast.LENGTH_SHORT).show();
          }
          break;
        case Constants.MESSAGE_TOAST:
          if (null != activity) {
            Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                Toast.LENGTH_SHORT).show();
          }
          break;
      }
    }
  }


}
