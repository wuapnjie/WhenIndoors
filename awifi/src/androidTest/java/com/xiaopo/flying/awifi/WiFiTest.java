package com.xiaopo.flying.awifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.functions.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class WiFiTest {
  @Test
  public void testScanWiFi() throws Exception {
    // Context of the app under test.
    Context appContext = InstrumentationRegistry.getTargetContext();

    AWifi.from(appContext)
        .subscribe(new Consumer<List<ScanResult>>() {
          @Override
          public void accept(List<ScanResult> scanResults) throws Exception {
            assert scanResults == null || scanResults.isEmpty();
          }
        }, new Consumer<Throwable>() {
          @Override
          public void accept(Throwable throwable) throws Exception {
            assert throwable!=null;
          }
        });

  }
}
