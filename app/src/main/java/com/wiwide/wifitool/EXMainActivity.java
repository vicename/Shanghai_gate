package com.wiwide.wifitool;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.wiwide.common.CommonDefine;
import com.wiwide.common.Logger;
import com.wiwide.common.PreferencesTool;
import com.wiwide.common.Util;
import com.wiwide.http.HttpHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.concurrent.Future;


public class EXMainActivity extends BaseActivity
{
    public static final int FLAG_CODE_GET_SUCCESS = 0;
    public static final int FLAG_CODE_GET_FAIL = 1;

    private EditText mPhone;
    private EditText mAuthenticationCode;
    private Handler mMsgHandler;
    private String mUid = "-1";
    private String mAcode = "-1";
    private Button mBtnSecurity;
    private Boolean isRunning = false;
    private SharedPreferences pref;
    private PreferencesTool prefService;
    private Future future;
//    private mGetCode

    static class MsgHandler extends Handler
    {
        private SoftReference<EXMainActivity> mActivityReference;

        public MsgHandler(EXMainActivity activity)
        {
            mActivityReference = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            EXMainActivity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case FLAG_CODE_GET_SUCCESS:
                        Toast.makeText(activity, R.string.code_get_success, Toast.LENGTH_SHORT).show();
                        break;
                    case FLAG_CODE_GET_FAIL:
                        Toast.makeText(activity, R.string.code_get_fail, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //系统版本号大于4.4(KitKat)则透明状态栏+透明导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main);

        mMsgHandler = new MsgHandler(this);
        prefService = new PreferencesTool(this);

        mPhone = (EditText) findViewById(R.id.phone);
        mPhone.setText(Util.getPhone(this));
        findViewById(R.id.get_authentication_code).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                HttpHandler.getCode(EXMainActivity.this, mPhone.getText().toString(), new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                    {
                        super.onSuccess(statusCode, headers, response);
                        String result = response.optString("status", "-1");
                        if (Integer.parseInt(result) == 1) {
                            mMsgHandler.obtainMessage(FLAG_CODE_GET_SUCCESS).sendToTarget();
                        } else {
                            mMsgHandler.obtainMessage(FLAG_CODE_GET_FAIL).sendToTarget();
                        }
                        Logger.i("onSuccess:" + response);
                    }
                });
            }
        });

        mAuthenticationCode = (EditText) findViewById(R.id.authentication_code);
        findViewById(R.id.phone).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });

        /**
         * 验证手机并绑定
         */
        findViewById(R.id.bind).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                HttpHandler.bindMoble(EXMainActivity.this, mPhone.getText().toString().trim(), mAuthenticationCode.getText().toString().trim(), new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                    {
                        super.onSuccess(statusCode, headers, response);
                        String result = response.optString("status", "-1");
                        if (Integer.parseInt(result) == 1) {
                            //验证成功后解析UID并保存
                            mUid = response.optString("UID", "-1");
                            if (!mUid.equals("-1")) {
//                            putPrefUid(mUid);
                                prefService.saveIn(CommonDefine.UID,mUid);
                            }
//                          当验证成功时绑定手机
                            bindPhone();
                        } else {
                            isSuccessToast("手机验证码错误,绑定", Integer.parseInt(result));
                        }
                        Log.i("onSuuccess_-----mUid", mUid);
                        Logger.i("onSuccess_code:" + response);
                    }
                });
            }
        });
        /**
         * 解除绑定
         */
        findViewById(R.id.un_bind).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mUid.equals("-1")) {
//                    mAcode = getPrefAcode();
                    mUid = prefService.getStringByMao().get("UID");
                }
                if (mUid.equals("-1")) {
                    isSuccessToast("你好像没绑定过啊,解除绑定", -1);
                    return;
                }
                HttpHandler.checkOut(EXMainActivity.this, mUid, new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                    {
                        super.onSuccess(statusCode, headers, response);
                        String result = response.optString("status", "-1");
                        mUid = "-1";
                        prefService.saveIn(CommonDefine.UID,mUid);
                        isSuccessToast("解除绑定", Integer.parseInt(result));
                        Logger.i("onSuccess_checkout" + response);
                    }
                });
            }
        });
        /**
         * 审计设备提交认证中心
         */
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mUid.equals("-1")) {
//                    mAcode = getPrefAcode();
                    mUid = prefService.getStringByMao().get("UID");
                }
                if (mUid.equals("-1")) {
                    isSuccessToast("你好像没绑定过啊,解除绑定", -1);
                    return;
                }
                HttpHandler.checkOut(EXMainActivity.this, mUid, new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                    {
                        super.onSuccess(statusCode, headers, response);
                        String result = response.optString("status", "-1");
                        mUid = "-1";
                        prefService.saveIn(CommonDefine.UID,mUid);
                        isSuccessToast("解除绑定", Integer.parseInt(result));
                        Logger.i("onSuccess_checkout" + response);
                    }
                });
            }
        });

        /**
         * 安全审计
         */
        mBtnSecurity = (Button) findViewById(R.id.btn_security_test);
        mBtnSecurity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //如果为默认值则从pref取数据,如果仍为空则提示并终止
                if (mAcode.equals("-1")) {
//                    mAcode = getPrefAcode();
                    mAcode = prefService.getStringByMao().get("ACODE");
                }
                if (mAcode.equals("-1")) {
                    isSuccessToast("你好像不能审计了,审计", -1);
                    return;
                }
                Log.i("pred-------", mAcode);
                //根据isRunning判断是否启动线程
                if (!isRunning) {
                    isRunning = true;
                    mBtnSecurity.setText("停止安全审计");
                    future = ApplicationPlus.getInstance().submitTask(new SecurityTestThread());
                } else {
                    isRunning = false;
                    mBtnSecurity.setText("开始安全审计");
                    //挂起线程并设置为空
                    future.cancel(true);
                }
            }
        });
    }


    //绑定手机
    public void bindPhone()
    {
        HttpHandler.checkIn(EXMainActivity.this, mUid, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                String result = response.optString("status", "-1");
                if (Integer.parseInt(result) == 1) {
                    //验证成功后解析Acode并保存
                    mAcode = response.optString("acode", "-1");
                    if (!mAcode.equals("-1")) {
                        prefService.saveIn(CommonDefine.ACODE,mAcode);
                    }
                }
                isSuccessToast("绑定手机", Integer.parseInt(result));
                Logger.i("onSuccess_checkin" + response);
            }
        });
    }

    /**
     * 安全审计线程
     */
    class SecurityTestThread implements Runnable
    {
        public void run()
        {
            try {
                Thread thread = Thread.currentThread();
                while (!thread.isInterrupted()) {
                    Thread.sleep(1000);
                            Log.i("thread-----------", "--------");
                    HttpHandler.securityTest(EXMainActivity.this, mAcode, new JsonHttpResponseHandler()
                    {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                        {
                            super.onSuccess(statusCode, headers, response);
                        }
                    });

                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 自定义toast方法用于测试时判断是否成功
     *
     * @param type
     * @param result
     */
    public void isSuccessToast(String type, int result)
    {
        if (result == 1) {
            Toast.makeText(EXMainActivity.this, type + "成功!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(EXMainActivity.this, type + "失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }
}
