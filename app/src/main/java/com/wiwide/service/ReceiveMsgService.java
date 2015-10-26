package com.wiwide.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.wiwide.common.CommonDefine;
import com.wiwide.common.Logger;
import com.wiwide.common.NetworkChecker;
import com.wiwide.common.PreferencesTool;
import com.wiwide.common.SyncResponse;
import com.wiwide.common.Util;
import com.wiwide.common.WifiNetworkObserver;
import com.wiwide.ewifi.util.Sha;
import com.wiwide.ewifi.util.WiWideLogin;
import com.wiwide.http.HttpHandlerDC;
import com.wiwide.wifitool.ApplicationPlus;
import com.wiwide.wifitool.BindPhoneActivity;
import com.wiwide.wifitool.BoundListActivity;
import com.wiwide.wifitool.ConnectionActivity;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by DC-ADMIN on 15-9-21.
 */
public class ReceiveMsgService extends Service implements WifiNetworkObserver {
    private MyBinder myBinder = new MyBinder();
    private ConnectionActivity mActivity;
    private int mTip;
    private String mUid;
    private String mAndroidVertion;
    private String mMac;
    private String mMaccc;
    private String mSSID;
    private String mPhoneNum;
    private String mWanLanMac;
    private String mLocationCode;
    private String mLocationName;
    private boolean mIsGetLocationCode;
    private int mWiWiDeLogin;
    private int mStatusCode;
    private boolean mIsWifiOpen;
    private boolean mIsWiWiDe;
    private PreferencesTool pref;
    private WiWideLogin wiwideLogin;
    private static final int NO_NET = 0;
    private static final int GOT_NET = 1;
    private static final int UI_CONNECTING = 600;
    private static final int UI_CHECKIN_SUCSSES = 601;
    private static final int UI_CONNECTED_SUCSSES = 602;
    private static final int UI_CONNECTED_FAILD = 610;
    private static final int UI_CONNECTED_UNBOUND = 611;
    private static final int UI_WIFI_OFF = 612;
    private static final int UI_NOT_WIWIDE = 613;
    private static final int UI_FREE_WIFI = 614;
    private static final int TIP_LOCATION_NOT_SAFE = 621;
    private static final int TIP_LOCATION_DONT_NEED_CHECK = 622;
    private static final int TIP_WIWIDE_ALREADY_LOGINED = 623;
    private RotateAnimation ra;
    private boolean mIsNetUsefull;
    private int mConnectionSwitcher;
    private ProgressDialog progressDialog;
    private boolean mIsFirstChecked;
    private boolean mIsActivitySeted;

    //    WifiConnectStateObserver watched;


    @Override
    public void onCreate() {
        super.onCreate();
        pref = new PreferencesTool(this);
        Log.i("onCreate", "--------");
        initNetworkChangeReciver();//启动网络变化情况接收器
        getBindedLocalReciver();//当绑定成功时重新连接
        getUnboundLocalReciver();//当解绑所有设备时删除UID并重新连接
        initData();
    }

    private void initData() {
        mAndroidVertion = Build.VERSION.RELEASE;//手机系统版本号
        mIsWifiOpen = isWifi();
        //        mIsWifiOpen = false;
        Logger.i("iswifi");
        wiwideLogin = new WiWideLogin();
        mUid = pref.getPrefString(CommonDefine.UID, "-1");
        mPhoneNum = pref.getPrefString(CommonDefine.PHONE_NUM, "-1");
        mTip = 1;
        mIsNetUsefull = false;
        mConnectionSwitcher = 0;
        NetworkChecker networkChecker = new NetworkChecker(this);
        ApplicationPlus.getInstance().submitTask(networkChecker);//为网络检查类新起一个线程
        //注册观察者
        networkChecker.registerObserver(this);
    }

    //接收信息
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    UIControler(UI_CONNECTED_SUCSSES);//迈外迪验证通过
                    Logger.i("WWD验证通过");
                    break;
                case 11:
                    UIControler(UI_CONNECTED_SUCSSES);//没有跳转页
                    break;
                case -1:
                    UIControler(UI_CONNECTED_FAILD);//迈外迪验证未通过
                    Logger.i("WWD验证未通过");
                    break;
                case 100:
                    mIsNetUsefull = true;
                    break;
                case 101:
                    mIsNetUsefull = false;
                    break;
                case 108:
                    mIsNetUsefull = true;
                    connecting();
                    break;
                case 109:
                    mIsNetUsefull = false;
                    connecting();
                case 200:
                    mStatusCode = 200;
                    break;
                case 201:
                    mStatusCode = 201;
                    break;
                case TIP_LOCATION_NOT_SAFE:
                    UIControler(TIP_LOCATION_NOT_SAFE);
                    break;
                case TIP_LOCATION_DONT_NEED_CHECK:
                    UIControler(TIP_LOCATION_DONT_NEED_CHECK);
                    break;
                case TIP_WIWIDE_ALREADY_LOGINED:
                    UIControler(TIP_WIWIDE_ALREADY_LOGINED);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void setActivity(ConnectionActivity activity) {
        this.mActivity = activity;
        mIsActivitySeted = true;
        mActivity.getBtnTestLocation().setOnClickListener(getLocationcode);
        mActivity.getBtnTestGetWiwideMac().setOnClickListener(clickGetWiwidemac);
    }

    //验证逻辑判断

    /**
     * 网络连接,在绑定服务且开启网络检查时被调用
     */
    public void connecting() {
        Logger.i("--con_first:" + String.valueOf(mIsFirstChecked) + "-activi_check" + String.valueOf(mIsActivitySeted));
        if (!mIsFirstChecked) {
            return;
        }
        if (!mIsActivitySeted) {
            return;
        }
        Log.i("------muid", mUid);
        UIControler(UI_CONNECTING);
        //如果无wifi则终止
        if (!mIsWifiOpen) {
            Log.i("------mTip", String.valueOf(mTip));
            Log.i("--con-flag", String.valueOf(mIsWifiOpen));
            UIControler(UI_WIFI_OFF);
            return;
        }
        mMac = Util.getBSSID(this);
        //获取wan口mac地址
        getWiwideWanLanMac();
        //获取公安场所代码
        getSaftyLocationCode();
        //一次性获取网络状态
        //        mIsNetUsefull = NetworkChecker.getInstance(this).mIsNet;
        Logger.i("----网络状态-----" + String.valueOf(mIsNetUsefull));
        mIsWiWiDe = isWiWiDe();
        if (mIsNetUsefull) {
            if (mIsWiWiDe) {
                toastGo("已经认证过,您可以直接连接");
                UIControler(UI_FREE_WIFI);
                return;
            }
            toastGo("此热点不需要认证,你可以直接连接");
            UIControler(UI_FREE_WIFI);
            return;
        }
        if (!mIsGetLocationCode) {
            toastGo("这是未经认证的热点");
            UIControler(UI_CONNECTED_FAILD);
            UIControler(TIP_LOCATION_NOT_SAFE);
            return;
        }
        if (!mIsWiWiDe) {
            UIControler(UI_NOT_WIWIDE);
            return;
        }
        mUid = pref.getPrefString(CommonDefine.UID, "-1");
        //如果uid为空则提示未绑定
        if (mUid == null || mUid.equals("-1")) {
            UIControler(UI_CONNECTED_UNBOUND);
            alertCreate();
            return;
        }
        shanghaiCheckIn();
    }

    /**
     * 网络连接,在绑定服务且开启网络检查时被调用
     */
    public void connecting1() {
        if (!mIsWifiOpen) {
            Logger.i("---------no-open");
            Log.i("------mTip", String.valueOf(mTip));
            Log.i("--con-flag", String.valueOf(mIsWifiOpen));
            UIControler(UI_WIFI_OFF);
            return;
        }
        mMac = Util.getBSSID(this);
        //此处为方法开关,只有集齐两次赋值为1的调用才会执行方法体.
        Logger.i("--con_first:" + String.valueOf(mIsFirstChecked) + "-activi_check" + String.valueOf(mIsActivitySeted));
        if (!mIsFirstChecked) {
            return;
        }
        if (!mIsActivitySeted) {
            return;
        }
        //        Logger.i(mLocationCode);
        Log.i("------muid", mUid);
        //如果无wifi则终止
        UIControler(UI_CONNECTING);
        Log.i("isNet--", String.valueOf(mIsNetUsefull));
        //一次性获取网络状态
        //        mIsNetUsefull = NetworkChecker.getInstance(this).mIsNet;
        //                if (isWifiFreeTest()) {
        //                    return;
        //                }
        //        if (!testIsGetLocationCode()) {
        //            return;
        //        }
//        testIsGetLocationCode();
        //                if (testIsWiWiDe()) {
        //                    return;
        //                }
        testIsWiWiDe();
        mUid = pref.getPrefString(CommonDefine.UID, "-1");
        //如果uid为空则提示未绑定
        if (mUid == null || mUid.equals("-1")) {
            UIControler(UI_CONNECTED_UNBOUND);
            alertCreate();
            return;
        }
        shanghaiCheckIn();
    }

    //公安场所认证
    private boolean testIsGetLocationCode() {
        getSaftyLocationCode();
        if (!mIsGetLocationCode) {
            toastGo("这是未经认证的Wifi");
            UIControler(UI_CONNECTED_FAILD);
            UIControler(TIP_LOCATION_NOT_SAFE);
//            handler.obtainMessage(TIP_LOCATION_NOT_SAFE).sendToTarget();
//            mActivity.getBtnCommand().setVisibility(View.INVISIBLE);
            return false;
        }
        return true;
    }

    private boolean isWifiFreeTest() {
        Logger.i("----网络状态-----" + String.valueOf(mIsNetUsefull));
        mIsWiWiDe = isWiWiDe();
        if (mIsNetUsefull) {
            if (mIsWiWiDe) {
                toastGo("已认证过,为您直接连接");
                UIControler(UI_FREE_WIFI);
                return true;
            }
            toastGo("此热点不需要认证,你可以直接连接");
            UIControler(UI_FREE_WIFI);
            return true;
        }
        return false;
    }

    private boolean testIsWiWiDe() {
        if (!isWiWiDe()) {
//            toastGo("这不是迈外迪提供的热点");
//            UIControler(UI_NOT_WIWIDE);
            return true;
        }
        return false;
    }

    private void getWiwideWanLanMac() {
        HttpHandlerDC.getWiwideMac(this, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                toastGo("无法获取mac");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                String mac = response.optString("device_mac", "-1");
                if (mac != null && !mac.equals("-1")) {
                    mWanLanMac = mac;
                    Logger.i("mWanLanmac:"+mac);
                } else {
                    mWanLanMac=null;
                }
            }
        });
    }

    private void getSaftyLocationCode() {
        String mac = CommonDefine.TEST_MAC;
        String stamp = Util.getTime();
        StringBuffer sb = new StringBuffer();
        sb.append(mac);
        sb.append(stamp);
        sb.append("9r]<\\Z,3cP}H!aC_");
        String sigs = sb.toString();
        String sig = Util.getMD5(sigs);
        Logger.i(sig);
        Logger.i(mac + "," + stamp + "," + sig);
/**
 * "00-1F-7A-A3-68-20", "1445591028", "60baa02c7daf1acd1514fd3036da72e6"
 */
        HttpHandlerDC.getSCode(this, mac, stamp, sig, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int result = response.optInt("code", -1);
                Logger.i("location result" + String.valueOf(result));
                switch (result) {
                    case 0:
                        JSONObject jObj = response.optJSONObject("data");
                        mLocationCode = jObj.optString("location_code", "-1");
                        mIsGetLocationCode=true;
                        Logger.i("--locationCode:" + mLocationCode);
                        break;
                    case -1:
                        toastGo("服务器也提了一个问题!");
                        UIControler(UI_CONNECTED_FAILD);
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        UIControler(UI_CONNECTED_FAILD);
                        break;
                    default:
                        UIControler(UI_CONNECTED_FAILD);
                        break;
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Logger.i("123123131233123123");
            }
        });
    }


    //上海网关验证
    private void shanghaiCheckIn() {
        HttpHandlerDC.asyncLogin(this, mUid, mLocationCode, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                toastGo("网络信号差,连接失败!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int result = response.optInt("error", -1);
                String mobile = response.optString("MOBILE", "-1");
                Log.i("-----sys", "result:" + String.valueOf(result));
                switch (result) {
                    case 0:
                        //                        UIControler(UI_CHECKIN_SUCSSES);
                        //首次登录则获取新的uid并保存
                        if (pref.getPrefBoolean(CommonDefine.IS_FIRST, false)) {
                            mUid = response.optString("UID", "-1");
                            pref.saveIn(CommonDefine.UID, mUid);
                            //表示已经认证过
                            pref.saveIn(CommonDefine.IS_FIRST, false);
                        }
                        Logger.i("----网关认证成功-----");
                        asyncWiWiDeLogin();
                        break;
                    default:
                        Logger.i("----网关认证失败-----");
                        UIControler(UI_CONNECTED_FAILD);
                        break;
                }
            }
        });
    }

    /**
     * 判断是否为WiWiDe的ap
     *
     * @return isWiWideAp
     */
    protected boolean isWiWiDe() {
//        mMac = Util.getBSSID(this);
        mSSID = Util.getSSID(this);
        Log.i("wiwide----mMac", mMac);
        Log.i("wiwide----mSSID", mSSID);
        mIsWiWiDe = wiwideLogin.isWiWideAp(mMac, mSSID);
        Log.i("wiwide----", String.valueOf(mIsWiWiDe));
        return mIsWiWiDe;
    }

    //异步登录迈外迪
    protected void asyncWiWiDeLogin() {
        ApplicationPlus.getInstance().submitTask(new Runnable() {
            @Override
            public void run() {
                if (haveStatus() == 200) {
                    //                    Logger.i("--是否有跳转页--" + String.valueOf(mStatusCode));
                    Logger.i("没有跳转页");
                    handler.obtainMessage(11).sendToTarget();
                    return;
                }
                int i = wiwideGo();
                handler.obtainMessage(i).sendToTarget();
            }
        });
    }

    private int wiwideGo() {
//        mMac = Util.getBSSID(this);
        long time = System.currentTimeMillis();
        String hashContent = mPhoneNum + ";" + mMac + ";" + "" + ";" + mSSID + ";" + "" + ";" + time;
        String hash = Sha.hash_mac("HmacSHA1", hashContent, CommonDefine.WIWIDE_SEC);
        int i = wiwideLogin.autoLogin(CommonDefine.WIWIDE_PID, mPhoneNum, mMac, "", mSSID, "", time + "", hash);
        Log.i("-----login", CommonDefine.WIWIDE_PID + "-" + mPhoneNum + "-" + mMac + "-" + "" + "-" + mSSID + "-" + "" + "-" + System.currentTimeMillis() + "" + "-" + hash);
        Logger.i("登录歪歪迪结果:" + String.valueOf(i));
        return i;
    }


    //检查有无跳转验证页,以此判断是否已经验证过迈外迪登录
    private int haveStatus() {
        SyncResponse response = new SyncResponse();
        response = Util.checkNeedAuthentication(ReceiveMsgService.this);
        Log.i("statuscode--", String.valueOf(response.getStatusCode()));
        return response.getStatusCode();
    }

    //网络状态观察者回调
    @Override
    public void onNetworkUseful() {
        handler.obtainMessage(100).sendToTarget();
    }

    @Override
    public void onNetworkUseless() {
        handler.obtainMessage(101).sendToTarget();
    }

    @Override
    public void onNetworkChanged(boolean currentState) {
        if (currentState) {
            handler.obtainMessage(100).sendToTarget();
        } else {
            handler.obtainMessage(101).sendToTarget();
        }
    }

    //首次检查回调
    @Override
    public void onFirstCheck(boolean isNet) {
        mIsFirstChecked = true;
        if (isNet) {
            handler.obtainMessage(108).sendToTarget();
        } else {
            handler.obtainMessage(109).sendToTarget();
        }
        Logger.i("---首次连接观察者");
        connecting();
    }

    /**
     * 判断wifi是否连接
     *
     * @return
     */
    protected boolean isWifi() {
        //得到网络连接信息
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        //判断是否为wifi
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.i("isWifi---", "--Yes!!");
            return true;
        }
        Log.i("isWifi---", "--No!!");
        return false;
    }

    //注册有无网络接收器
    protected void initNetworkChangeReciver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        NetWorkChangeReceiver netWorkChangeReceiver = new NetWorkChangeReceiver();
        //注册
        registerReceiver(netWorkChangeReceiver, intentFilter);
    }


    //接收网络情况广播
    private class NetWorkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mIsWifiOpen = isWifi();
            if (mActivity == null) {
                return;
            }
            if (!mIsWifiOpen) {
                UIControler(UI_WIFI_OFF);
            }
        }
    }

    //使用LocalReciver接收应用内绑定情况广播,用于绑定后重新连接
    protected void getBindedLocalReciver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Handle the received local broadcast
                int i = intent.getIntExtra("binded", -1);
                if (i == 1) {
                    //接到广播即再次连接
                    connecting();
                }
            }
        }, new IntentFilter("com.wiwide.service.HAVE_BINDED"));//IntentFilter里面用来写action
    }

    //使用LocalReciver接收应用内绑定情况广播,当解绑所有设备时删除UID并重新连接
    protected void getUnboundLocalReciver() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Handle the received local broadcast
                if (intent.getIntExtra("deleteUID", -1) == 1) {
                    pref.removeByKey("UID");
                }
                if (intent.getIntExtra("reconnect", -1) == 1) {
                    connecting();
                }
            }
        }, new IntentFilter("com.wiwide.service.LAST_PHONE_UNBOUND"));//IntentFilter里面用来写action
    }

    //根据连接情况控制UI
    private void UIControler(int i) {
//        i=123123;

        if (i != UI_CONNECTING) {
            //            mBtnClearAnim();
            //            progressDialog.dismiss();
            mActivity.getProg().setVisibility(View.INVISIBLE);
        }
        switch (i) {
            case UI_CONNECTING:
                //                progressDialogCreate();
                mActivity.getProg().setVisibility(View.VISIBLE);
                mActivity.getProg().refreshDrawableState();
                //                mActivity.initProg();
                mActivity.getBtnCommand().setVisibility(View.INVISIBLE);
                mActivity.getmBtnReconnect().setOnClickListener(reConnect);
                mActivity.getTvIsConnected().setText("连接中...");
                mActivity.getTvIsConnected().setOnClickListener(intentToBind);
                //                trans();
                break;
            case UI_CHECKIN_SUCSSES:
                mActivity.getTvIsConnected().setText("验证成功");
                mActivity.getTvConnectionTip().setText("正在为您连接网络");
                break;
            case UI_CONNECTED_SUCSSES:
                mActivity.getBtnCommand().setVisibility(View.VISIBLE);

                mActivity.getTvIsConnected().setText("连接成功!");
                mActivity.getTvConnectionTip().setText("为了保证你能与本地网络无缝连接\n请在上网时保持应用开启!");
                mActivity.getBtnCommand().setText("管理已绑定手机");
                mActivity.getBtnCommand().setOnClickListener(intentToBoundList);
                mActivity.getTvIsConnected().setOnClickListener(intentToBind);
                break;
            case UI_CONNECTED_FAILD:
                mActivity.getBtnCommand().setVisibility(View.VISIBLE);
                mActivity.getTvIsConnected().setText("连接失败...");
                mActivity.getTvConnectionTip().setText("请检查网络或重新绑定");
                mActivity.getBtnCommand().setText("绑定手机");
                mActivity.getBtnCommand().setOnClickListener(intentToBind);
                mActivity.getTvIsConnected().setOnClickListener(intentToBoundList);
                break;
            case UI_CONNECTED_UNBOUND:
                mActivity.getBtnCommand().setVisibility(View.VISIBLE);
                mActivity.getTvIsConnected().setText("手机没有绑定...");
                mActivity.getTvConnectionTip().setText("绑定手机即可享受便捷的上网服务!");
                mActivity.getBtnCommand().setText("绑定手机");
                mActivity.getBtnCommand().setOnClickListener(intentToBind);
                mActivity.getTvIsConnected().setOnClickListener(intentToBoundList);
                break;
            case UI_FREE_WIFI:
                mActivity.getTvIsConnected().setText("你现在已经可以上网了");
                mActivity.getTvConnectionTip().setText("别问我是怎么知道的,那不重要");
                mActivity.getBtnCommand().setText("管理已绑定手机");
                mActivity.getBtnCommand().setOnClickListener(intentToBoundList);
                break;
            case 11111:
                mActivity.getTvConnectionTip().setText(mMac);
                break;
            case UI_WIFI_OFF:
                mActivity.getTvIsConnected().setText("Wi-Fi未开启!");
                mActivity.getTvConnectionTip().setText("请打开您的Wi-Fi");
                mActivity.getmBtnReconnect().setOnClickListener(reConnect);
                break;
            case UI_NOT_WIWIDE:
                mActivity.getBtnCommand().setVisibility(View.VISIBLE);
                mActivity.getTvIsConnected().setText("这不是迈外迪的热点");
                mActivity.getTvConnectionTip().setText("抱歉无法为您连接\n请您尝试其他方法");
                break;
            case TIP_LOCATION_NOT_SAFE:
                mActivity.getTvConnectionTip().setText("您连接的Wi-Fi没有经过认证,不能帮您连接");
                break;
            case TIP_LOCATION_DONT_NEED_CHECK:
                mActivity.getTvConnectionTip().setText("这是一个不需要认证的热点,请注意安全");
                break;
            case TIP_WIWIDE_ALREADY_LOGINED:
                mActivity.getTvConnectionTip().setText("这是迈外迪的Wi-Fi,且您已经认证过");
                break;
            default:
                break;
        }
    }

    private void trans() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(10);
        int repreatCount = rotateAnimation.getRepeatCount();
        //                rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());//平滑运动
        rotateAnimation.setInterpolator(new LinearInterpolator());//线性运动
        mActivity.getmBtnReconnect().startAnimation(rotateAnimation);
    }

    private void mBtnStop() {
        //        mActivity.getmBtnReconnect().
    }

    private void mBtnClearAnim() {
        mActivity.getmBtnReconnect().clearAnimation();
        //        ValueAnimator va=new ValueAnimator();
        //        va.reverse();
        //        ValueAnimator.reverse();
        //        mActivity.getmBtnReconnect()
    }

    //跳转到已绑定页面
    private View.OnClickListener intentToBoundList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setClass(getApplicationContext(), BoundListActivity.class));
        }
    };

    //跳转到绑定手机页面
    private View.OnClickListener intentToBind = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setClass(getApplicationContext(), BindPhoneActivity.class));
        }
    };

    //点击重新连接
    private View.OnClickListener reConnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            connecting();
        }
    };
    private View.OnClickListener getLocationcode = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getSaftyLocationCode();
        }
    };

    private View.OnClickListener clickGetWiwidemac = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getWiwideWanLanMac();
        }
    };


    public void progressDialogCreate() {
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("连接中");
        progressDialog.show();
    }

    //标准对话框
    private void alertCreate() {
        //添加对话框内容
        AlertDialog.Builder ab = new AlertDialog.Builder(mActivity);
        ab.setTitle("您还没有绑定或绑定已失效!");//添加标题
        ab.setMessage("现在去绑定?");//添加内容
        ab.setPositiveButton("去绑定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //需要加入FLAG_ACTIVITY_NEW_TASK标签才能启动activity
                startActivity(new Intent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setClass(getApplicationContext(), BindPhoneActivity.class));
            }
        });
        ab.setNegativeButton("算了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        //创建对话框
        AlertDialog alertDiaLog = ab.create();
        alertDiaLog.show();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public ReceiveMsgService getService() {
            Log.i("----service", "-----getService");
            return ReceiveMsgService.this;
        }

        //        @Override
        //        public void callReconnectMethod()
        //        {
        //            Logger.i("----binder");
        //        }
    }

    private void toastGo(String text) {
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }
}
