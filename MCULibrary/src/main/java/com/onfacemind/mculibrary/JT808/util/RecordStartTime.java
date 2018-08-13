package com.onfacemind.mculibrary.JT808.util;

import android.content.Context;

/**
 * @author Created by 刘承. on 2018/8/13
 * business@onfacemind.com
 */
public class RecordStartTime {
    /**
     * 记录启动时间，APP连续重启时关机重启
     */
    public static void recordStartTime(Context context) {
        ServerPreferences sp = ServerPreferences.getInstance(context);
        long lastServerreboot = sp.getLastServerreboot();
        long l = System.currentTimeMillis();
        sp.setLastServerreboot(l);
        long interval = l - lastServerreboot;
        //上一次启动在20秒内  关机重启
        if (interval > 0 && interval < 25 * 1000) {
            CMDUtil.reboot();
        }
    }

    /**
     * 记录启动时间，APP断开心跳连接时重启,
     * <p>
     * 重启时间 90s  内断开心跳
     */
    public static void recordStartTime_AppHeart(Context context) {
        ServerPreferences sp = ServerPreferences.getInstance(context);
        long lastServerreboot = sp.getLastServerreboot();
        long l = System.currentTimeMillis();
        long interval = l - lastServerreboot;
        //上一次启动在20秒内  关机重启
        if (interval > 0 && interval < 90 * 1000) {
            CMDUtil.reboot();
        } else {
            CMDUtil.restartApp(context);
        }
    }
}
