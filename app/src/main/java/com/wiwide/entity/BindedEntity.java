package com.wiwide.entity;

import java.util.Date;

/**
 * Created by DC-ADMIN on 15-9-10.
 */
public class BindedEntity
{
    private String id;
    private String name;
    private String time;

    public BindedEntity(String id, String name, String time)
    {
        this.id = id;
        this.name = name;
        this.time = time;
    }

    public BindedEntity()
    {
        super();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }
}
