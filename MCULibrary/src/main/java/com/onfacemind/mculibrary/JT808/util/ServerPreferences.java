package com.onfacemind.mculibrary.JT808.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 记录一些必要数据
 *
 * @author Created by 刘承. on 2018/8/13
 * business@onfacemind.com
 */
public class ServerPreferences {
    static ServerPreferences serverPreferences;
    SharedPreferences preferences;
    Context mContext;

    private static final String SERVER_PREFERENCES = "SERVER_PREFERENCES";
    private static final String LAST_SERVERREBOOT = "LAST_SERVERREBOOT";

    public ServerPreferences(Context context) {
        mContext = context;
        preferences = context.getSharedPreferences(SERVER_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static ServerPreferences getInstance(Context context) {
        if (serverPreferences == null) {
            serverPreferences = new ServerPreferences(context);
        }
        return serverPreferences;
    }

    //上次启动时间
    public long getLastServerreboot() {
        return preferences.getLong(LAST_SERVERREBOOT, 0);
    }

    //记录本次时间
    public void setLastServerreboot(long l) {
        preferences.edit().putLong(LAST_SERVERREBOOT, l).apply();
    }

}
