package com.onfacemind.mculibrary.JT808.util;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import io.reactivex.internal.schedulers.RxThreadFactory;
import io.reactivex.schedulers.Schedulers;

/**
 * 此工具类  对应 F902 包名使
 */
public class CMDUtil {

    /**
     * 杀死进程
     *
     * @param pkg
     */
    public static void killProgress(final String pkg) {
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                DataOutputStream dataOutputStream = null;
                try {
                    // 申请su权限
                    Process process = Runtime.getRuntime().exec("su");
                    dataOutputStream = new DataOutputStream(process.getOutputStream());
                    // 执行pm install命令
                    String command1 = "am force-stop " + pkg + "\n"; //force-stop;
                    dataOutputStream.write(command1.getBytes(Charset.forName("utf-8")));
                    dataOutputStream.flush();
//                    System.exit(1);//退出重启界面
                    dataOutputStream.writeBytes("exit\n");
                    dataOutputStream.flush();
                    process.waitFor();
                } catch (Exception e) {
                } finally {
                    try {
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    /**
     * 重新启动--包括服务
     * <p>
     * 程序连续断开 重启机器
     * <p>
     * com.onfacemind.aiface902/com.onfacemind.aiface902.Activity.Home.StartActivity
     */
    public static void restartApp(Context context) {
        final String pkg = context.getPackageName();
        final String atv = getActivities(context, pkg);
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                DataOutputStream dataOutputStream = null;
                try {
                    // 申请su权限
                    Process process = Runtime.getRuntime().exec("su");
                    dataOutputStream = new DataOutputStream(process.getOutputStream());
                    // 执行pm install命令
                    String command1 = "am force-stop " + pkg + "\n"; //force-stop;
                    String command2 = "am start -n " + pkg + "/" + atv + "\n"; //am start -n 包名/包名.第一个Activity的名称";
//                    String command1 = "am force-stop com.onfacemind.aiface902\n"; //force-stop;
//                    String command2 = "am start -n com.onfacemind.aiface902/com.onfacemind.aiface902.Activity.Home.StartActivity\n"; //am start -n 包名/包名.第一个Activity的名称";
                    dataOutputStream.write(command1.getBytes(Charset.forName("utf-8")));
                    dataOutputStream.write(command2.getBytes(Charset.forName("utf-8")));
                    dataOutputStream.flush();
                    dataOutputStream.writeBytes("exit\n");
                    dataOutputStream.flush();
                    process.waitFor();
                } catch (Exception e) {
                } finally {
                    try {
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    /**
     * 启动 APP  MainActivity
     * <p>
     * 程序连续断开 重启机器
     * <p>
     * com.onfacemind.aiface902/com.onfacemind.aiface902.Activity.Home.StartActivity
     */
    public static void startApp_MainActivity(Context context) {
        final String pkg = context.getPackageName();
        final String atv = getActivities(context, pkg);
        Schedulers.io().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                DataOutputStream dataOutputStream = null;
                try {
                    // 申请su权限
                    Process process = Runtime.getRuntime().exec("su");
                    dataOutputStream = new DataOutputStream(process.getOutputStream());
                    // 执行pm install命令
                    String command2 = "am start -n " + pkg + "/" + atv + "\n"; //am start -n 包名/包名.第一个Activity的名称";
                    dataOutputStream.write(command2.getBytes(Charset.forName("utf-8")));
                    dataOutputStream.flush();
                    dataOutputStream.writeBytes("exit\n");
                    dataOutputStream.flush();
                    process.waitFor();
                } catch (Exception e) {
                } finally {
                    try {
                        if (dataOutputStream != null) {
                            dataOutputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        });
    }


    /**
     * 获取APP的启动入口
     *
     * @param context
     * @param packageName
     * @return
     */
    private static String getActivities(Context context, String packageName) {
        Intent localIntent = new Intent("android.intent.action.MAIN", null);
        localIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> appList = context.getPackageManager().queryIntentActivities(localIntent, 0);
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo resolveInfo = appList.get(i);
            String packageStr = resolveInfo.activityInfo.packageName;
            if (packageStr.equals(packageName)) {
                //这个就是你想要的那个Activity
//                Log.d(TAG, "packageName: ===" + packageName);
//                Log.d(TAG, "getActivities: ===" + resolveInfo.activityInfo.name);
                return resolveInfo.activityInfo.name;
            }
        }
        return "";
    }


//    /**
//     * 重启设备
//     * 备用逻辑 1 在关闭设备之前 先关闭mcu心跳 防止在系统启动过程中再次断电重启
//     */
//    public static void reboot() {
//
//        try {
//            Process process = Runtime.getRuntime().exec("reboot");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
