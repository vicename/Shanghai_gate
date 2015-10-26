package com.example.dc_admin.mywatcher;

import android.util.Log;

/**
 * Created by DC-ADMIN on 15-9-22.
 */
public class ConcreteWatcher implements Watcher
{

    @Override
    public void updateNotify(Content content)
    {
        int id=content.getId();
        String name = content.getName();
        String adress = content.getAddress();
        Log.i("updateNotify", "dsdsdfsfdf");
    }
}
