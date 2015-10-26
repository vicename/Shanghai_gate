package com.example.dc_admin.mywatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DC-ADMIN on 15-9-22.
 */
public class ConcreteWatched implements Watched
{
    private List<Watcher> list = new ArrayList<>();
    @Override
    public void add(Watcher watcher)
    {
        list.add(watcher);
    }

    @Override
    public void remove(Watcher watcher)
    {
        list.remove(watcher);
    }

    @Override
    public void notifyWatcher(Content content)
    {
        for (Watcher watcher : list) {
            watcher.updateNotify(content);
        }
    }
}
