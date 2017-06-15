package com.etfos.bpeserovic.runforestrun;


/**
 * Created by Bobo on 13.6.2017..
 */

public class Times {
    int id;
    String time;

    public Times(int id, String time)
    {
        super();
        this.id = id;
        this.time = time;
    }

    public Times(String time)
    {
        super();
        this.time = time;
    }

    public int getId()
    {
        return id;
    }

    public String getTime()
    {
        return time;
    }

    @Override
    public String toString()
    {
        return  time;
    }
}
