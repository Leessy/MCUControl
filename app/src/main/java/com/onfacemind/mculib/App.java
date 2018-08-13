package com.onfacemind.mculib;

import android.app.Application;
import android.util.Log;

import com.onfacemind.mculibrary.MCU;

public class App extends Application {
    private static final String TAG = "Apps";
    static App app;

    int init = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
//        MCU.instance().startServer(app);
//        Log.d(TAG, "onCreate: ????????????????" + init++);
    }

    public static App getApp() {
        return app;
    }

}
