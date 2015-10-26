package com.wiwide.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.wiwide.common.CommonDefine;
import com.wiwide.common.Logger;
import com.wiwide.common.PreferencesTool;
import com.wiwide.http.HttpHandler;
import com.wiwide.http.HttpHandlerDC;
import com.wiwide.wifitool.ApplicationPlus;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.concurrent.Future;

/**
 * Created by DC-ADMIN on 15-9-12.
 */
public class CheckinService extends Service
{
    public static final String TAG = "CheckinService";
    private Future future;
    private String msg;
    private String mUid;
    private String mUidNew;
    private String mAcode = "-1";
    private int flag;
    private int uidFlag;
    private PreferencesTool pref;

    @Override
    public void onCreate()
    {
        super.onCreate();
        pref = new PreferencesTool(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //接收来自activity的消息
        if ("com.wiwide.service.CHECKIN".equals(intent.getAction())) {
            this.msg = intent.getStringExtra("msg");
            //从Activity取得uid
            mUid = intent.getStringExtra("UID");
        }
        future = ApplicationPlus.getInstance().submitTask(new SecurityTestThread());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //结束线程
        future.cancel(true);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    //使用Local BroadCast Message发送应用内广播
    private void speedExceedMessageToActivity()
    {
        Intent intent = new Intent("com.wiwide.service.CHECKIN");
        sendLocationBroadcast(intent);
    }

    private void sendLocationBroadcast(Intent intent)
    {
        intent.putExtra("ACODE", mAcode);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }




    //安全审计子线程
    class SecurityTestThread implements Runnable
    {
        public void run()
        {
            try {
                Thread thread = Thread.currentThread();
                while (!thread.isInterrupted()) {
                    Thread.sleep(1000 * 20);

                    HttpHandlerDC.login(CheckinService.this, mUid, new JsonHttpResponseHandler()
                    {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                        {
                            super.onSuccess(statusCode, headers, response);
                            int result = response.optInt("error", -1);
                            String mobile = response.optString("MOBILE", "-1");
                            switch (result) {
                                case 0:
                                    if (uidFlag == 0) {
                                        //验证成功后解析Acode并保存
                                        mUid = response.optString("UID", "-1");
                                        pref.saveIn(CommonDefine.UID,mUid);
                                        //表示已经认证过
                                        uidFlag = 1;
                                    }
                                    break;
                                default:
                                    break;
                            }
                            Logger.i("onSuccess_checkin" + response);
                        }
                    });

                    HttpHandler.securityTest(CheckinService.this, mAcode, new JsonHttpResponseHandler()
                    {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                        {
                            super.onSuccess(statusCode, headers, response);
                            Log.i("thread-----------", "--------");
                        }
                    });
                    CheckinService.this.speedExceedMessageToActivity();

                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
}
