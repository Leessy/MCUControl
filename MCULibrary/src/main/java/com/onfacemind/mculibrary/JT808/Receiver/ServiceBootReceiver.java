package com.onfacemind.mculibrary.JT808.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.onfacemind.mculibrary.Log;

import com.onfacemind.mculibrary.JT808.Server.MCUDataServer;

public class ServiceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("!!!!!!!!!!!!", "onReceive: 收到广播！！" + intent.getAction().toString());
        if (intent.getAction().equals("android.media.AUDIO_BECOMING_NOISY")) {
            /* 服务开机自启动 */
            Intent service = new Intent(context, MCUDataServer.class);
            context.startService(service);

            /* 应用开机自启动 */
//            Intent intent_n = new Intent(context, MainActivity.class);
//            intent_n.setAction("android.intent.action.MAIN");
//            intent_n.addCategory("android.intent.category.LAUNCHER");
//            intent_n.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent_n);

        }
    }
}
