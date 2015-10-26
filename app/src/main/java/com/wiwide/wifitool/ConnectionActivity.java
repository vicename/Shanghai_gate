package com.wiwide.wifitool;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wiwide.common.CommonDefine;
import com.wiwide.common.Logger;
import com.wiwide.common.PreferencesTool;
import com.wiwide.customProg.ProgressBarCircularIndeterminate;
import com.wiwide.service.CheckinService;
import com.wiwide.service.IsConnectedService;
import com.wiwide.service.ReceiveMsgService;

import java.util.Timer;
import java.util.TimerTask;


public class ConnectionActivity extends BaseActivity
{
    private IsConnectedService isConnectedService;
    // 记录当前连接状态
    private boolean conncetState = true;
    private int connectivityFlag;
    private int mTip;
    private ReceiveMsgService receiveMsgService;
    private Button mBtnCommand;
    private Button mBtnTestSetting;
    private Button mBtnTestLocation;
    private Button mBtnTestGetWiwideMac;
    private ImageView mBtnReconnect;
    private ImageView mIvTestRotate;
    private PreferencesTool pref;
    private String mUid;
    private String mAcode;
    private String mAndroidVertion;
    private TextView mTvIsConnected, mTvConnectionTip, mTvModel;
    private LinearLayout mBg;
    private ProgressBarCircularIndeterminate prog;
    private boolean isExite = false;
    private static final int UI_CONNECTING = 0;
    private static final int UI_CHECKIN_SUCSSES = 1;
    private static final int UI_CONNECTED_SUCSSES = 2;
    private static final int UI_CONNECTED_FAILD = 10;
    private static final int UI_CONNECTED_UNBOUND = 11;
    public static ConnectionActivity mActivity;
    private ProgressDialog progressDialog;
    String i;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conection);
        //initConnectionLocalReciver();//开启应用内广播接收->后台连接成功与否
        //startCheckinService();//开启服务
        initView();//初始化
        initData();
        bind();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
//        Intent intent = getIntent();
//        Bundle extra= intent.getExtras();
//        i = extra.getString("binds");
//        Logger.i("----intent:" + i);
//        if (i!=null && i.equals("1")) {
//            methodInServiceCallBack.callReconnectMethod();
//            Logger.i("methodgo");
//        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbind();
    }

    private void initView()
    {
        mBtnCommand = (Button) findViewById(R.id.btn_command_phones);
        mBtnTestSetting = (Button) findViewById(R.id.btn_test_setting);
        mBtnTestLocation = (Button) findViewById(R.id.btn_test_location_code);
        mBtnTestGetWiwideMac = (Button) findViewById(R.id.btn_test_wiwide_mac);
        mTvIsConnected = (TextView) findViewById(R.id.tv_isconnected);
        mTvConnectionTip = (TextView) findViewById(R.id.tv_connection_tip);
        mTvModel = (TextView) findViewById(R.id.tv_android_model);
        mBg = (LinearLayout) findViewById(R.id.bg_connection_activity);
        mBtnReconnect = (ImageView) findViewById(R.id.btn_reconnect);
        prog= (ProgressBarCircularIndeterminate) findViewById(R.id.progress);
        prog.setVisibility(View.INVISIBLE);
        mBtnCommand.setOnClickListener(intentToBoundList);
        mTvIsConnected.setOnClickListener(intentToBind);
        mBtnTestSetting.setOnClickListener(intentToSetting);
        mTvModel.setText(CommonDefine.PHONE_MODEL);//手机型号
    }

    private void initData()
    {
        pref = new PreferencesTool(this);
        mUid = pref.getPrefString(CommonDefine.UID, "-1");
        mAndroidVertion = Build.VERSION.RELEASE;//手机系统版本号

    }

    public void initProg()
    {
        ProgressBarCircularIndeterminate newProg;
        newProg = (ProgressBarCircularIndeterminate) findViewById(R.id.progress);
        newProg.setVisibility(View.VISIBLE);
    }
    public void hideProg(){

    }

    public TextView getTvIsConnected()
    {
        return mTvIsConnected;
    }

    public TextView getTvConnectionTip()
    {
        return mTvConnectionTip;
    }

    public Button getBtnCommand()
    {
        return mBtnCommand;
    }

    public ImageView getmBtnReconnect()
    {
        return mBtnReconnect;
    }

    public ProgressBarCircularIndeterminate getProg(){
        return prog;
    }

    public Button getBtnTestLocation()
    {
        return mBtnTestLocation;
    }

    public Button getBtnTestGetWiwideMac() {
        return mBtnTestGetWiwideMac;
    }


    //连接或者没绑定
    //    private void connecting() {
    //        Log.i("------muid", mUid);
    ////        if (connectivityFlag == 0) {
    ////            return;
    ////        }
    ////        Log.i("-----act_tip", String.valueOf(receiveMsgService.getTip()));
    ////        if (receiveMsgService.getTip() == 0) {
    ////            return;
    ////        }
    //        //如果uid为空则提示未绑定
    //        if (mUid == null || mUid.equals("-1")) {
    //            UIControler(UI_CONNECTED_UNBOUND);
    //            alertCreate();
    //            return;
    //        }
    //        //否则进入登录连接
    //        UIControler(UI_CONNECTING);
    //    }

    //    //登录并链接
    //    private void asyncLogin() {
    //        HttpHandlerDC.asyncLogin(ConnectionActivity.this, mUid, new JsonHttpResponseHandler() {
    //            //            CustomRetryHandler retry = new CustomRetryHandler(1,5000);
    //            @Override
    //            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
    //                super.onSuccess(statusCode, headers, response);
    //                int result = response.optInt("error", -1);
    //                String mobile = response.optString("MOBILE", "-1");
    //                Log.i("-----sys", "result:" + String.valueOf(result));
    //                switch (result) {
    //                    case 0:
    //                        UIControler(UI_CHECKIN_SUCSSES);
    //                        //首次登录则获取新的uid并保存
    //                        if (pref.getPrefBoolean(CommonDefine.IS_FIRST, false)) {
    //                            mUid = response.optString("UID", "-1");
    //                            pref.saveIn(CommonDefine.UID, mUid);
    //                            //表示已经认证过
    //                            pref.saveIn(CommonDefine.IS_FIRST, false);
    //                        }
    //                        wiwideGo();
    //                        break;
    //                    default:
    //                        UIControler(UI_CONNECTED_FAILD);
    //                        break;
    //                }
    //            }
    //        });
    //    }


    //跳转到已绑定页面
    private View.OnClickListener intentToBoundList = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            startActivity(new Intent().setClass(ConnectionActivity.this, BoundListActivity.class));
        }
    };

    //跳转到绑定手机页面
    private View.OnClickListener intentToBind = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            startActivity(new Intent().setClass(ConnectionActivity.this, BindPhoneActivity.class));
        }
    };
    private View.OnClickListener intentToSetting=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent().setClass(ConnectionActivity.this,TestSettingActivity.class));
        }
    };
    //    //重新连接
    //    private View.OnClickListener reConnect = new View.OnClickListener() {
    //        @Override
    //        public void onClick(View v) {
    //            connecting();
    //        }
    //    };
    //
    //    public View.OnClickListener getReConnect() {
    //        return reConnect;
    //    }


    //绑定网络情况服务
    private void bind()
    {
        Intent intent = new Intent(ConnectionActivity.this, ReceiveMsgService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            receiveMsgService = ((ReceiveMsgService.MyBinder) service).getService();
            receiveMsgService.setActivity(ConnectionActivity.this);
            Log.i("--toSer_conn", "------");
            receiveMsgService.connecting();
        }
    };

    //解除绑定
    private void unbind()
    {
        if (receiveMsgService != null) {
            unbindService(serviceConnection);
            Log.i("mylog", "解除绑定");
        }
    }

    //使用LocalReciver接收应用内网络连接情况广播
    protected void initConnectionLocalReciver()
    {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // TODO Handle the received local broadcast
                mAcode = intent.getStringExtra("ACODE");
                Log.i("----LocalReceiver", mAcode);
            }
        }, new IntentFilter("com.wiwide.service.CHECKIN"));//IntentFilter里面用来写action
    }

    //开启验证服务
    public void startCheckinService()
    {
        Intent startIntent = new Intent(this, CheckinService.class);
        startIntent.setAction("com.wiwide.service.CHECKIN");
        //向服务发送数据
        startIntent.putExtra("UID", mUid);
        startService(startIntent);
    }

    //停止验证服务
    public void stopCheckinService()
    {
        Intent stopIntent = new Intent(this, CheckinService.class);
        stopService(stopIntent);
    }

    public void progressDialogCreate()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("连接中");
        progressDialog.show();
    }

    //标准对话框
    private void alertCreate()
    {
        //添加对话框内容
        AlertDialog.Builder ab = new AlertDialog.Builder(ConnectionActivity.this);
        ab.setTitle("您还没有绑定或绑定已失效!");//添加标题
        ab.setMessage("现在去绑定?");//添加内容
        ab.setPositiveButton("走着", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                startActivity(new Intent().setClass(ConnectionActivity.this, BindPhoneActivity.class));
            }
        });
        ab.setNegativeButton("我不的", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });
        //创建对话框
        AlertDialog alertDiaLog = ab.create();
        alertDiaLog.show();
    }

    //双击返回退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
        }
        return false;
    }

    private void exitBy2Click()
    {
        Timer tExit;
        if (!isExite) {
            isExite = true;
            //            Toast.makeText(ConnectionActivity.this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
            toastGo("再按一次返回键退出");
            tExit = new Timer();
            tExit.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    isExite = false;
                }
            }, 2000);
        } else {
            stopCheckinService();
            finish();
        }
    }

}
