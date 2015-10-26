package com.wiwide.wifitool;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class TestSettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("测试选项设置");
        setSupportActionBar(toolbar);
        initView();
    }

    private void initView() {

    }
}
