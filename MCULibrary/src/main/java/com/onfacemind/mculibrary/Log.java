package com.onfacemind.mculibrary;


/**
 * Created by 刘承. on 2018/1/29.
 */

public class Log {
    static final boolean log = BuildConfig.DEBUG;

    public static void v(String tag, String msg) {
        if (log) android.util.Log.v(tag, msg);
    }


    public static void d(String tag, String msg) {
        if (log) android.util.Log.d(tag, msg);
    }


    public static void i(String tag, String msg) {
        if (log) android.util.Log.i(tag, msg);
    }


    public static void w(String tag, String msg) {
        if (log) android.util.Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (log) android.util.Log.w(tag, msg);
    }
}
