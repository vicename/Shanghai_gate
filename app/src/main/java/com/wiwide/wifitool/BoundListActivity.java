package com.wiwide.wifitool;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.wiwide.adapter.BindedListAdapter;
import com.wiwide.common.CommonDefine;
import com.wiwide.common.Logger;

import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.wiwide.common.PreferencesTool;
import com.wiwide.common.Util;
import com.wiwide.entity.BindedEntity;
import com.wiwide.entity.ListInfo;
import com.wiwide.http.HttpHandlerDC;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class BoundListActivity extends BaseActivity {
    private List<BindedEntity> list = new ArrayList<BindedEntity>();
    private BindedListAdapter adapter;
    private ListView mLv;
    public static BoundListActivity mActivity;
    private Button mBtnDelete;
    private ListInfo listInfo;
    private PreferencesTool pref;
    private String mCheckCode;
    private String mPhoneNum;
    private AlertDialog mMyAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binded);
        //设置ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("已绑定设备");
        //将ToolBar作为actionbar使用
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //设置返回键可用
        initView();
        CheckCodeDialog();
        //getBoundList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //overridePendingTransition(R.animator.in_right_in, R.animator.in_hold_out);
    }

    public void initView() {
        mActivity = this;
        listInfo = new ListInfo();
        listInfo.initList();
        pref = new PreferencesTool(this);
        mPhoneNum = pref.getPrefString(CommonDefine.PHONE_NUM, "-1");
        if (mPhoneNum.equals("-1") || mPhoneNum == null) {
            mPhoneNum = Util.getPhone(this);
        }
        mBtnDelete = (Button) findViewById(R.id.btn_delete);
        mLv = (ListView) findViewById(R.id.lv_binded_phone);
        adapter = new BindedListAdapter(this, list);
        mLv.setAdapter(adapter);
    }

    //验证码的自定义对话框
    private void CheckCodeDialog() {
        //获取xml中布局
        View view = View.inflate(BoundListActivity.this, R.layout.dialog_bound_list_check_code, null);
        final EditText edt = (EditText) view.findViewById(R.id.edt_bound_check_code);
        final EditText edtPhone = (EditText) view.findViewById(R.id.edt_bound_phone);
        Button mBtnGetCheckCode = (Button) view.findViewById(R.id.btn_bound_get_check_code);
        if (mPhoneNum != null) {
            edtPhone.setText(mPhoneNum);
        }
        mBtnGetCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCheckCode();
            }
        });
        AlertDialog.Builder ab = new AlertDialog.Builder(BoundListActivity.this);
        //把自定义的布局添加给对话框
        ab.setView(view);
        ab.setTitle("请验证后继续操作");
        ab.setCancelable(false);
        //此处不设置监听,为了防止用户点击以后对话框自动关闭
        ab.setPositiveButton("提 交", null);
        //获取返回键,如果点击则退出当前页面
        ab.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    BoundListActivity.this.finishDcActivity();
                    return true;
                }
                return false;
            }
        });
        mMyAlert = ab.create();
        mMyAlert.show();
        //在此处获取按钮添加监听事件
        mMyAlert.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPhoneNum = edtPhone.getText().toString();
                mCheckCode = edt.getText().toString().trim();
                Log.i("----mCheckCode", mCheckCode);
                getBoundList(mCheckCode);
//                mMyAlert.dismiss();
            }
        });
    }

    //获取验证码
    public void getCheckCode() {
        Log.i("----phone", mPhoneNum);
        HttpHandlerDC.auth(BoundListActivity.this, mPhoneNum, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int result = response.optInt("error", -1);
                switch (result) {
                    case 0:
                        isSuccessToast("获取验证码", result);
                        break;
                    case 3:
                        isSuccessToast("点太多次了,获取验证码", result);
                        break;
                    default:
                        isSuccessToast("获取验证码", result);
                        break;
                }
                Logger.i("onSuccess:" + response);
            }
        });
    }

    //获取列表
    private void getBoundList(String code) {
        HttpHandlerDC.getBondedList(BoundListActivity.this, code, mPhoneNum, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int result = response.optInt("error", -1);
                long time;//服务器返回的时间戳,单位为秒
                String formattedTime;
                JSONArray jArray;
                if (result != 0) {
                    isSuccessToast("获取数据", result);
                    return;
                }
                mMyAlert.dismiss();
                jArray = response.optJSONArray("CLIENTS");
                try {
                    for (int i = 0; i < jArray.length(); i++) {
                        BindedEntity bindE = new BindedEntity();
                        //jArray.opt(i);
                        //String str=(String)
                        JSONObject jObj = (JSONObject) jArray.opt(i);
                        time = jObj.getLong("BINDING_TIME");
                        Log.i("----timeGap", String.valueOf(time));
                        formattedTime = Util.formatTime(time * 1000);
                        bindE.setId(jObj.getString("CLIENT_CODE"));
                        bindE.setTime(formattedTime);
                        bindE.setName(jObj.getString("MAIN_TYPE"));
                        String str = jObj.getString("BINDING_TIME");
                        Log.i("---jArray", jObj.getString("MAIN_TYPE"));
                        list.add(bindE);
                    }
                    //将list和解除绑定需要的参数发送给adapter
                    adapter.setList(list, mPhoneNum, mCheckCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("-------bondlist", "erro" + String.valueOf(result));
            }
        });
    }

    /**
     * 自定义toast方法用于测试时判断是否成功
     *
     * @param type
     * @param result
     */
    public void isSuccessToast(String type, int result) {
        if (result == 0) {
            Toast.makeText(BoundListActivity.this, type + "成功!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BoundListActivity.this, type + "失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_binded, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Log.i("------------", "home");
            finishDcActivity();
        }
        return super.onOptionsItemSelected(item);
    }

}
