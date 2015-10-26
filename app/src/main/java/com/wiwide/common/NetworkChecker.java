package com.wiwide.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.wiwide.wifitool.ApplicationPlus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 统一处理wifi网络有效性检查
 * <p/>
 * Created by yueguang on 15-7-29.
 */
public class NetworkChecker implements Runnable, WifiConnectStateObserver {
    private static NetworkChecker mNetworkChecker;
    private List<WifiNetworkObserver> mWifiNetworkObservers;
    private Context mContext;
    private Future mNetworkCheckerFuture;
    public boolean mIsNet;
    public boolean mIsNetUseful;
    public boolean mIsFirst = true;


    public void registerObserver(WifiNetworkObserver observer) {
        if (mWifiNetworkObservers == null) {
            mWifiNetworkObservers = new ArrayList<>();
        }

        if (!mWifiNetworkObservers.contains(observer)) {
            mWifiNetworkObservers.add(observer);
        }
    }

    public void unregisterObserver(WifiNetworkObserver observer) {
        if (mWifiNetworkObservers != null && mWifiNetworkObservers.contains(observer)) {
            mWifiNetworkObservers.remove(observer);
        }
    }

    public NetworkChecker(Context context) {
        mContext = context;
        mNetworkChecker = this;
        //接收wifi打开与否广播
//        WifiConnectHandler.getInstance(context).registerWifiObserver(this);
//        if (!isWifi()) {
//            stopNetworkCheck();
//        }

    }
    protected boolean isWifi()
    {
        //得到网络连接信息
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        //判断是否为wifi
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.i("isWifi---", "--Yes!!");
            return true;
        }
        Log.i("isWifi---", "--No!!");
        return false;
    }
    public static synchronized NetworkChecker getInstance(Context context) {
        if (mNetworkChecker == null) {
            mNetworkChecker = new NetworkChecker(context);
        }
        return mNetworkChecker;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Thread.sleep(1000);
                try {
                    mIsNetUseful = Util.isNetworkUseful();
//                    mIsNet= isNetworkUseful;
                    if (mWifiNetworkObservers != null && mIsFirst) {
                        mIsFirst = false;
                        for (WifiNetworkObserver observer : mWifiNetworkObservers) {
                            observer.onFirstCheck(mIsNetUseful);
                        }
                    }
                    //网络发生变化
                    if (mIsNetUseful != mIsNet) {
                        if (mWifiNetworkObservers != null) {
                            for (WifiNetworkObserver observer : mWifiNetworkObservers) {
                                observer.onNetworkChanged(mIsNetUseful);
                            }
                        }
                    }
                    if (mIsNetUseful) {
                        if (mWifiNetworkObservers != null) {
                            for (WifiNetworkObserver observer : mWifiNetworkObservers) {
                                observer.onNetworkUseful();
                            }
                        }
                    } else {
                        if (mWifiNetworkObservers != null) {
                            for (WifiNetworkObserver observer : mWifiNetworkObservers) {
                                observer.onNetworkUseless();
                            }
                        }
                    }
                    mIsNet = mIsNetUseful;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(String ssid) {
        Log.i("----", "onCennected");
        startNetworkCheck();
    }

    @Override
    public void onDisconnected(String ssid) {
        Log.i("----", "ondisCennected");
        stopNetworkCheck();
    }

    void startNetworkCheck() {
        if (mNetworkChecker == null) {
            mNetworkChecker = new NetworkChecker(mContext);
        } else {
            stopNetworkCheck();
        }

        mNetworkCheckerFuture = ApplicationPlus.getInstance().submitTask(mNetworkChecker);
    }

    void stopNetworkCheck() {
        if (mNetworkCheckerFuture != null) {
            mNetworkCheckerFuture.cancel(true);
            mNetworkCheckerFuture = null;
        }
    }
}
