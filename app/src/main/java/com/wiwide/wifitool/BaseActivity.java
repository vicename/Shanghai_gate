package com.wiwide.wifitool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * Created by DC-ADMIN on 15-9-8.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private String mTag;
    private ImageView back;
    private TextView titleText;
    private TextView topBar;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    /**
     * 初始化onCreate方法
     */
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            fitStatusBar();
        }
        overridePendingTransition(R.anim.abc_grow_fade_in_from_bottom, R.animator.hold_on_in);
        super.onCreate(savedInstanceState);
        preferences = this.getSharedPreferences("config", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    //用TextView填充StatusBar
    public void fitStatusBar() {
// 创建TextView
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        textView.setBackgroundColor(getResources().getColor(R.color.main_bg));
        textView.setLayoutParams(lParams);
// 获得根视图并把TextView加进去。
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);
    }

    //获取状态栏高度
    public int getStatusBarHeight() {
        Class<?> c;
        Object obj;
        Field field;
        int x, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 吐丝
     *
     * @param msg
     */
    public void toastGo(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 吐丝
     *
     * @param msg
     */
    public void toastGo(int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 关闭Activity
     */
    public void finishDcActivity() {
        this.finish();
        overridePendingTransition(R.animator.hold_on_out, R.animator.drown);
    }

    /**
     * 返回键
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            BaseActivity.this.finishDcActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @return
     */
    public int getProjectId() {
        return preferences.getInt("pid", 0);
    }

    /**
     * 获取Porject ID
     *
     * @return
     */
    public int getAppProjectId() {
        return preferences.getInt("appid", 0);
    }

    /**
     * 获取用户ID
     *
     * @return
     */
    public int getUid() {
        return preferences.getInt("uid", 0);
    }

    /**
     * 获取屏幕宽
     *
     * @return
     */
    public int getScreenWidth() {
        return preferences.getInt("screen_width", 0);
    }

    /**
     * 获取屏幕高
     *
     * @return
     */
    public int getScreenHeight() {
        return preferences.getInt("screen_height", 0);
    }

    /**
     * Int
     *
     * @param key
     * @param value
     */
    public void setIntPreferences(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * String
     *
     * @param key
     * @param value
     */
    public void setStringPreference(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Boolean
     *
     * @param key
     * @param value
     */
    public void setBooleanPreference(String key, Boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * long
     *
     * @param key
     * @param value
     */
    public void setLongPreference(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    /**
     * Float
     *
     * @param key
     * @param value
     */
    public void setFloatPreference(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    /**
     * int
     *
     * @param key
     * @return
     */
    public int getIntPreference(String key) {
        return preferences.getInt(key, 0);
    }

    /**
     * String
     *
     * @param key
     * @return
     */
    public String getStringPreference(String key) {
        return preferences.getString(key, null);
    }

    /**
     * float
     *
     * @param key
     * @return
     */
    public float getFloatPreference(String key) {
        return preferences.getFloat(key, 0.0f);
    }

    /**
     * boolean
     *
     * @param key
     * @return
     */
    public boolean getBooleanPreference(String key) {
        return preferences.getBoolean(key, false);
    }

    /**
     * long
     *
     * @param key
     * @return
     */
    public long getLongPreference(String key) {
        return preferences.getLong(key, 0L);
    }

    /**
     * clear all SharedPreferences
     */
    public void clearPreference() {
        editor.clear();
        editor.commit();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        toastGo("on low memory...");
    }
}
