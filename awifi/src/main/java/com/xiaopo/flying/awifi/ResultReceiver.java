package com.xiaopo.flying.awifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.reactivestreams.Subscriber;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.subjects.BehaviorSubject;


/**
 * This object is an extension of a {@link BroadcastReceiver} that mixes the classic Android API
 * for scanning the list of available networks and return them back as an {@link Observable}.
 */
class ResultReceiver extends BroadcastReceiver {

  /**
   * This variable is the one that will emit the networks whenever a {@link Subscriber} is
   * subscribing to it
   */
  private BehaviorSubject<List<ScanResult>> subject;
  private long startScan;

  public ResultReceiver() {
    subject = BehaviorSubject.create();
    startScan = System.currentTimeMillis();
  }

  /**
   * This method implements a fluid API to start the scanning for new networks.
   */
  @SuppressLint("MissingPermission")
  public ResultReceiver startScanningFrom(@NonNull Context context) {
    Context appCtx = context.getApplicationContext();
    appCtx.registerReceiver(this, AWifi.filter);
    getWifiManager(appCtx).startScan();
    return this;
  }


  @SuppressLint("WifiManagerPotentialLeak")
  private static WifiManager getWifiManager(@NonNull Context context) {
    return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
  }

  @SuppressLint("MissingPermission")
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d("AWifi", "time usage on receiving wifi stats: " + (System.currentTimeMillis() - startScan) + " ms");

    if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
      Context appCtx = context.getApplicationContext();

      appCtx.unregisterReceiver(this);
      subject.onNext(getWifiManager(appCtx).getScanResults());
    }
  }

  public Observable<List<ScanResult>> getObservable() {
    return subject;
  }
}
