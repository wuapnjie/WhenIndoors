package com.xiaopo.flying.awifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

@SuppressLint("MissingPermission")
class MultipleScanReceiver {

  private BroadcastReceiver receiver;

  Flowable<List<ScanResult>> scan(final Context context, final int times) {
    final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    if (wifiManager == null) {
      return Flowable.empty();
    }
    return Flowable.create(new FlowableOnSubscribe<List<ScanResult>>() {
      private long startTime;
      private int scanTimes;

      @Override
      public void subscribe(final FlowableEmitter<List<ScanResult>> emitter) throws Exception {
        if (receiver == null) {
          receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              Log.d("AWifi", "time usage on receiving wifi stats: " + (System.currentTimeMillis() - startTime) + " ms");
              emitter.onNext(wifiManager.getScanResults());

              if (scanTimes >= times) {
                emitter.onComplete();
                return;
              }

              startTime = System.currentTimeMillis();
              wifiManager.startScan();
              scanTimes++;
            }
          };
          context.registerReceiver(receiver, AWifi.filter);
        }

        startTime = System.currentTimeMillis();
        wifiManager.startScan();
        scanTimes++;
      }
    }, BackpressureStrategy.LATEST)
        .doFinally(getUnsubscribeAction(context))
        .doOnCancel(getUnsubscribeAction(context))
        .take(times)
        .subscribeOn(Schedulers.io());

  }

  @NonNull
  private Action getUnsubscribeAction(final Context context) {
    return new Action() {
      @Override
      public void run() throws Exception {
        if (receiver !=null) {
          context.unregisterReceiver(receiver);
          receiver = null;
        }
      }
    };
  }
}
