package com.wiwide.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.wiwide.common.Logger;
import com.wiwide.common.PreferencesTool;
import com.wiwide.entity.BindedEntity;
import com.wiwide.http.HttpHandlerDC;
import com.wiwide.wifitool.BoundListActivity;
import com.wiwide.wifitool.ConnectionActivity;
import com.wiwide.wifitool.R;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DC-ADMIN on 15-9-10.
 */
public class BindedListAdapter extends BaseAdapter {
    private Context context;
    private List<BindedEntity> mList = new ArrayList<BindedEntity>();
    private String mCheckCode;
    private String mPhone;
    private String mClientCode;

    public BindedListAdapter() {
        super();
    }

    public BindedListAdapter(Context context, List<BindedEntity> mList) {
        this.context = context;
        this.mList = mList;
    }

    public void setList(List<BindedEntity> mList, String mPhone, String mCheckCode) {
        this.mList = mList;
        this.mCheckCode = mCheckCode;
        this.mPhone = mPhone;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_bound_list, null);
            viewHolder.idTextView = (TextView) convertView.findViewById(R.id.inlv_id);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.inlv_phone_name);
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.inlv_time);
            viewHolder.mBtnDelete = (Button) convertView.findViewById(R.id.btn_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BindedEntity bindE = mList.get(position);
        viewHolder.idTextView.setText(bindE.getId());
        if (bindE.getName() == null) {
            viewHolder.nameTextView.setText("你的手机好萌呀");
        }
        viewHolder.nameTextView.setText(bindE.getName());
        viewHolder.dateTextView.setText(bindE.getTime());
        final int i = position;
        final String client = bindE.getId();
        viewHolder.mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("buoooooo", String.valueOf(i));
                makeUpAlertCreate(i, client);

            }
        });
        return convertView;
    }

    //标准对话框
    private void makeUpAlertCreate(final int position, final String client) {
        //添加对话框内容
        AlertDialog.Builder ab = new AlertDialog.Builder(BoundListActivity.mActivity);
        ab.setTitle("解除绑定手机");//添加标题
        ab.setMessage("确定要解除绑定此手机吗");//添加内容
        final int i = position;
        final PreferencesTool pref = new PreferencesTool(BoundListActivity.mActivity);
        ab.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //                mClientCode = String.valueOf(i + 1);
                HttpHandlerDC.unbind(BoundListActivity.mActivity, mPhone, mCheckCode, client, new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                    {
                        super.onSuccess(statusCode, headers, response);
                        int result = response.optInt("error", -1);
                        String clientCode = response.optString("CLIENT_CODE", "-1");
                        Logger.i("onSuccess:" + response);
                        if (result == 0) {
                            Toast.makeText(BoundListActivity.mActivity, "此设备已解除绑定", Toast.LENGTH_SHORT).show();
                            mList.remove(i);
                            BindedListAdapter.this.notifyDataSetChanged();
                            if (mList.size() == 0) {
//                                pref.removeByKey("UID");
                                speedExceedMessageToActivity();
                                BoundListActivity.mActivity.startActivity(new Intent().setClass(BoundListActivity.mActivity, ConnectionActivity.class));
                            }
                        }
                        if (result != 0) {
                            Toast.makeText(BoundListActivity.mActivity, "解除绑定失败!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        ab.setNegativeButton("取消", new DialogInterface.OnClickListener()
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
    //使用Local BroadCast Message发送应用内广播
    private void speedExceedMessageToActivity() {
        Intent intent = new Intent("com.wiwide.service.LAST_PHONE_UNBOUND");
        sendLocationBroadcast(intent);
    }
    private void sendLocationBroadcast(Intent intent) {
        intent.putExtra("deleteUID", 1);
        intent.putExtra("reconnect", 1);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class ViewHolder {
        TextView idTextView;
        TextView nameTextView;
        TextView dateTextView;
        Button mBtnDelete;
    }
}
