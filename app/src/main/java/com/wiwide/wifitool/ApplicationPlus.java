package com.wiwide.wifitool;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.wiwide.common.CommonDefine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 初始化操作，提供线程池
 * Created by yueguang on 15-3-23.
 */
public class ApplicationPlus extends Application {
    private static final String IS_FIRST_RUN = "is_first_run";
    private static ApplicationPlus mInstance;
    private ExecutorService mThreadPool;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        init();
    }

    public static ApplicationPlus getInstance() {
        return mInstance;
    }

    private void init() {
        mThreadPool = Executors.newCachedThreadPool();

        CommonDefine.initPath(this);

        SharedPreferences config = getSharedPreferences(CommonDefine.CONFIG, Context.MODE_PRIVATE);

        if (config.getBoolean(IS_FIRST_RUN, true)) {
            SharedPreferences.Editor edit = config.edit();

            edit.putBoolean(IS_FIRST_RUN, false);
            edit.apply();
        }
    }

    public Future submitTask(Runnable task) {
        return mThreadPool.submit(task);
    }
}
