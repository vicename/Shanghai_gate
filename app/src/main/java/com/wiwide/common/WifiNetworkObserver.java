package com.wiwide.common;

/**
 * Created by DC-ADMIN on 15-9-25.
 */
public interface WifiNetworkObserver {
    public void onNetworkUseful();

    public void onNetworkUseless();

    public void onNetworkChanged(boolean currentState);

    public void onFirstCheck(boolean currentState);
}
