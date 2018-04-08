package com.xiaopo.flying.awifi.internal;

import static com.xiaopo.flying.awifi.internal.Channel.CHANNEL_24GHZ;
import static com.xiaopo.flying.awifi.internal.Channel.CHANNEL_5GHZ;

public class WifiChannel {

  public final WifiFrequency frequency;
  public final int channel;

  public WifiChannel(WifiFrequency frequency, int channel) {
    this.frequency = frequency;
    this.channel = channel;
  }

  public static WifiChannel fromFrequency(int frequency) throws FrequencyOutOfRangeException {
    if (CHANNEL_24GHZ.isInRange(frequency)) {
      return new WifiChannel(WifiFrequency.LOW, CHANNEL_24GHZ.getChannelFrom(frequency));
    } else if (CHANNEL_5GHZ.isInRange(frequency)) {
      return new WifiChannel(WifiFrequency.HIGH, CHANNEL_5GHZ.getChannelFrom(frequency));
    } else {
      throw new FrequencyOutOfRangeException();
    }
  }
}
