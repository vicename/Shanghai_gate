package com.wiwide.wifitool;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.wiwide.common.CommonDefine;
import com.wiwide.common.Logger;
import com.wiwide.common.PreferencesTool;
import com.wiwide.common.Util;
import com.wiwide.customProg.ProgressBarCircularIndeterminate;
import com.wiwide.http.HttpHandler;
import com.wiwide.http.HttpHandlerDC;

import org.apache.http.Header;
import org.json.JSONObject;

import java.lang.ref.SoftReference;

/**
 * Created by DC-ADMIN on 15-9-10.
 */
public class BindPhoneActivity extends BaseActivity implements View.OnClickListener
{
    public static final int FLAG_CODE_GET_SUCCESS = 0;
    public static final int FLAG_CODE_GET_FAIL = 1;

    private EditText mEdtPhone;
    private EditText mAuthenticationCode;
    private Handler mMsgHandler;
    private PreferencesTool pref;
    private String mUid = "-1";
    private String mAcode = "-1";
    private int countryFlag;
    private TextView mTvUserPermi;
    private TextView mTvSelectContry;
    private TextView mMainText;
    private AlertDialog mAlertDialog, mCountyDialog;
    private Button mBtnGetCode;
    private Button mBtnSubmit;
    private CheckBox mCBox;
    private RadioGroup mRadCountry;
    private ProgressBarCircularIndeterminate prog;
    private boolean isFirst;
    private String mCheckCode;
    private String mAndroidModel;
    private String mPhoneNum;

    static class MsgHandler extends Handler
    {
        private SoftReference<BindPhoneActivity> mActivityReference;

        public MsgHandler(BindPhoneActivity activity)
        {
            mActivityReference = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            BindPhoneActivity activity = mActivityReference.get();
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
        setContentView(R.layout.activiey_bind_phone);
        initView();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //                TransitionDrawable transition = (TransitionDrawable) mMainText.getBackground();
                //                transition.startTransition(4000);
            }
        }).start();
    }

    private void initView()
    {
        mAndroidModel = android.os.Build.MODEL;
        mEdtPhone = (EditText) findViewById(R.id.edt_phone);
        mAuthenticationCode = (EditText) findViewById(R.id.edt_check_code);
        mBtnSubmit = (Button) this.findViewById(R.id.btn_submit);
        mBtnGetCode = (Button) this.findViewById(R.id.btn_get_check_code);
        mCBox = ((CheckBox) findViewById(R.id.cbox_agree_permi));
        mTvSelectContry = (TextView) findViewById(R.id.tv_select_country);
        mTvUserPermi = (TextView) findViewById(R.id.user_permi);
        mMainText = (TextView) findViewById(R.id.main_text);
        prog = (ProgressBarCircularIndeterminate) findViewById(R.id.progress);
        prog.setVisibility(View.GONE);

        mCBox.setChecked(true);
        mBtnSubmit.setOnClickListener(this);
        mBtnGetCode.setOnClickListener(this);
        mTvSelectContry.setOnClickListener(this);
        mTvUserPermi.setOnClickListener(this);

        mEdtPhone.setText(Util.getPhone(this));

        mMsgHandler = new MsgHandler(this);
        pref = new PreferencesTool(this);
    }

    /**
     * 自定义toast方法用于测试时判断是否成功
     *
     * @param type
     * @param result
     */
    public void isSuccessToast(String type, int result)
    {
        if (result == 0) {
            Toast.makeText(BindPhoneActivity.this, type + "成功!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BindPhoneActivity.this, type + "失败", Toast.LENGTH_SHORT).show();
        }
    }

    //获取验证码
    public void getCheckCode()
    {
        prog.setVisibility(View.VISIBLE);
        HttpHandlerDC.auth(BindPhoneActivity.this, mEdtPhone.getText().toString(), new JsonHttpResponseHandler()
        {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
            {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                toastGo("网络信号差,连接失败!");
            }

            @Override
            public void onFinish()
            {
                super.onFinish();
                prog.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                int result = response.optInt("error", -1);
                switch (result) {
                    case 0:
                        mMsgHandler.obtainMessage(FLAG_CODE_GET_SUCCESS).sendToTarget();
                        break;
                    case 3:
                        isSuccessToast("点太多次了,获取验证码", result);
                        break;
                    default:
                        mMsgHandler.obtainMessage(FLAG_CODE_GET_FAIL).sendToTarget();
                        break;
                }
                Logger.i("onSuccess:" + response);
            }
        });
    }

    private void intentTest()
    {
        Intent intent = new Intent();
        intent.putExtra("binds", "1");
        startActivity(intent.setClass(BindPhoneActivity.this, ConnectionActivity.class));
    }

    //确认提交并绑定
    public void submit()
    {
        if (!mCBox.isChecked()) {
            Toast.makeText(BindPhoneActivity.this, "请同意用户协议", Toast.LENGTH_SHORT).show();
            return;
        }
        prog.setVisibility(View.VISIBLE);
        mPhoneNum = mEdtPhone.getText().toString().trim();
        mCheckCode = mAuthenticationCode.getText().toString().trim();
        HttpHandlerDC.bind(BindPhoneActivity.this, mPhoneNum, mCheckCode, mAndroidModel, new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                super.onSuccess(statusCode, headers, response);
                int result = response.optInt("error", -1);
                Log.i("onSuuccess_-----mUid", mUid);
                Logger.i("onSuccess_code:" + response);
                switch (result) {
                    case 0:
                        //验证成功后解析UID并保存
                        mUid = response.optString("UID", "-1");
                        if (!mUid.equals("-1")) {
                            pref.saveIn(CommonDefine.UID, mUid);
                            pref.saveIn(CommonDefine.IS_FIRST, true);//设置首次登录标签
                            isSuccessToast("绑定", result);
                        }
                        pref.saveIn(CommonDefine.CHECK_CODE, mCheckCode);
                        pref.saveIn(CommonDefine.PHONE_NUM, mPhoneNum);
                        //checkIn();
                        speedExceedMessageToActivity();
                        Intent intent = new Intent();
                        intent.putExtra("bind", "1");
                        startActivity(intent.setClass(BindPhoneActivity.this, ConnectionActivity.class));
                        break;
                    case 3:
                        isSuccessToast("验证码或手机不正确,绑定", result);
                        pref.saveIn(CommonDefine.IS_FIRST, false);
                        break;
                    case 4:
                        isSuccessToast("你绑定的手机超过了五个,绑定", result);
                        pref.saveIn(CommonDefine.IS_FIRST, false);//取消首次登录标签
                        startActivity(new Intent().setClass(BindPhoneActivity.this, BoundListActivity.class));
                        break;
                    default:
                        isSuccessToast("由于某些原因,绑定", result);
                        pref.saveIn(CommonDefine.IS_FIRST, false);
                        break;
                }
            }

            @Override
            public void onFinish()
            {
                super.onFinish();
                prog.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
            {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                toastGo("网络信号差,连接失败!");
            }
        });
    }

    //使用Local BroadCast Message发送应用内广播
    private void speedExceedMessageToActivity()
    {
        Intent intent = new Intent("com.wiwide.service.HAVE_BINDED");
        sendLocationBroadcast(intent);
    }

    private void sendLocationBroadcast(Intent intent)
    {
        intent.putExtra("binded", 1);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //绑定手机--此方法暂时闲置
    public void checkIn()
    {
        HttpHandler.checkIn(BindPhoneActivity.this, mUid, new JsonHttpResponseHandler()
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
                        pref.saveIn(CommonDefine.ACODE, mAcode);
                    }
                }
                isSuccessToast("绑定手机", Integer.parseInt(result));
                Logger.i("onSuccess_checkin" + response);
            }
        });
    }

    //选择国家_自定义弹窗
    private void countryDialog()
    {
        //获取xml中布局
        final View countryDialogView = View.inflate(BindPhoneActivity.this, R.layout.layout_country_dialog, null);
        //实例化的时候前面要加view!被这个错折腾了一晚上
        mRadCountry = (RadioGroup) countryDialogView.findViewById(R.id.radgroup_country);
        final RadioButton radioChina = (RadioButton) countryDialogView.findViewById(R.id.radio_china);
        RadioButton radioOther = (RadioButton) countryDialogView.findViewById(R.id.radio_other);

        if (countryFlag == 0) {
            radioChina.setChecked(true);
        } else {
            radioOther.setChecked(true);
        }
        mRadCountry.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                mCountyDialog.dismiss();
            }
        });

        AlertDialog.Builder ab = new AlertDialog.Builder(BindPhoneActivity.this);
        //把自定义的布局添加给对话框
        ab.setView(countryDialogView);
        mCountyDialog = ab.create();
        mCountyDialog.show();
        mCountyDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                int checkedid = mRadCountry.getCheckedRadioButtonId();
                countryFlag = (checkedid == R.id.radio_china) ? 0 : 1;
                RadioButton radioButton = (RadioButton) countryDialogView.findViewById(checkedid);
                mTvSelectContry.setText(radioButton.getText());
            }
        });
    }

    public void userPermiDialog()
    {
        View view = View.inflate(BindPhoneActivity.this, R.layout.layout_user_permi_dialog, null);
        AlertDialog.Builder ab = new AlertDialog.Builder(BindPhoneActivity.this);
        ab.setTitle("这是测试用的用户协议");
        ab.setView(view);
        mAlertDialog = ab.create();
        mAlertDialog.show();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.btn_get_check_code:
                getCheckCode();
                break;
            case R.id.btn_submit:
                submit();
                //                intentgo();
                break;
            case R.id.user_permi:
                userPermiDialog();
                break;
            case R.id.tv_select_country:
                countryDialog();
                break;
            default:
                break;
        }
    }
}
