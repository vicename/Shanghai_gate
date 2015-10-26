package com.example.dc_admin.mywatcher;

/**
 * Created by DC-ADMIN on 15-9-22.
 */
public interface Watched
{
    public void add(Watcher watcher);

    public void remove(Watcher watcher);

    public void notifyWatcher(Content content);
}
