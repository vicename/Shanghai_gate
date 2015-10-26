package com.wiwide.common;

/**
 * Created by DC-ADMIN on 15-9-25.
 */
public interface WifiConnectStateObserver {
    public void onConnected(String ssid);

    public void onDisconnected(String ssid);
}
