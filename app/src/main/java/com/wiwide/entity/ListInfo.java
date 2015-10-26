package com.wiwide.entity;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by DC-ADMIN on 15-9-14.
 */
public class ListInfo
{
    public List<BindedEntity> list = new ArrayList<BindedEntity>();
    public List<BindedEntity> getList()
    {
        return list;
    }
    public void initList(){
        list.add(new BindedEntity("1", "华为P8-U00", "2015-07-09"));
        list.add(new BindedEntity("2", "小米智能牙刷", "2015-07-13"));
        list.add(new BindedEntity("3", "金立语音王-玫瑰金", "2015-07-15"));
        list.add(new BindedEntity("4", "iPhone6 plus", "2015-07-17"));
        list.add(new BindedEntity("5", "游侠智能滑板鞋", "2015-07-22"));
    }
}
