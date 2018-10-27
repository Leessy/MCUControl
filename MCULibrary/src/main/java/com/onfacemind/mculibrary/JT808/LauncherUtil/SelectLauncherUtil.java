package com.onfacemind.mculibrary.JT808.LauncherUtil;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;

import com.onfacemind.mculibrary.JT808.util.CMDUtil;
import com.onfacemind.mculibrary.Log;

import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Created by 刘承. on 2018/7/28
 * business@onfacemind.com
 */
public class SelectLauncherUtil {
    private static final String TAG = "SelectLauncherUtil";

    /**
     * 判断是否启动了 默认程序选择界面 ，模拟点击位置启动apk
     */
    public static void shellInputKey(final Context context) {
        Observable.intervalRange(0, 30, 10, 200, TimeUnit.MILLISECONDS)
                .map(new Function<Long, Boolean>() {
                    @Override
                    public Boolean apply(Long aLong) throws Exception {
                        //com.android.internal.app.ResolverActivity  //判断在当前activity界面 执行点击事件
                        return isForeground(context, "com.android.internal.app.ResolverActivity");
                    }
                })
                .filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(Boolean aBoolean) throws Exception {
                        return aBoolean;
                    }
                })
                .single(false)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        //执行点击事件
                        if (aBoolean) {
                            simulateClick(context);
                        } else {
                            //.没有显示选择界面，判断是否在主屏幕界面
                            //.判断没有启动本应该程序的activity时

                            //1.停留在系统桌面
//                            if (isForeground(context, "com.android.launcher2.Launcher")) {
//                                Intent intent = new Intent();
//                                intent.setAction(context.getPackageName() + ".LAUNCHER");
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                context.startActivity(intent);
//                            }
                            //2.当前显示界面 不是本应用程序的
                            if (!isForegroundContainsPackage(context)) {
                                //方式 1.需要在启动activity配置filter
//                                Intent intent = new Intent();
//                                intent.setAction(context.getPackageName() + ".LAUNCHER");
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                context.startActivity(intent);

                                //方式 2.获取本context的启动activity    需系统权限
                                CMDUtil.startApp_MainActivity(context);
                            } else {
                                //点击提示崩溃窗口  如果当前界面已自动启动
                                simulateClickCrash(context);
                            }
                        }
                    }
                });

    }

    /**
     * app心跳断开 检查界面 3次
     */
    public static void shellCrashCheckRestart(final Context context) {
        Observable.intervalRange(0, 3, 100, 1000, TimeUnit.MILLISECONDS)
                .map(new Function<Long, Boolean>() {
                    @Override
                    public Boolean apply(Long aLong) throws Exception {
                        //判断   不在在当前activity界面 执行点击事件
                        simulateClickCrash(context);//点击一次屏幕
                        return !isForegroundContainsPackage(context);
                    }
                })
//                .filter(new Predicate<Boolean>() {
//                    @Override
//                    public boolean test(Boolean aBoolean) throws Exception {
//                        return aBoolean;
//                    }
//                })
//                .single(false)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        simulateClickCrash(context);//点击一次屏幕
                        System.out.println("***********APP心跳断开  检查界面  =***********" + aBoolean);
                        if (aBoolean) {
                            //不在本APP界面  并且是在选择启动器界面    执行选择点击事件
                            if (isForeground(context, "com.android.internal.app.ResolverActivity")) {
                                System.out.println("***********APP心跳断开  检查界面  选择启动器=***********");
                                simulateClick(context);
                            } else {
                                //不在选择器界面 直接启动APP   并点击崩溃弹窗确定位置
                                System.out.println("***********APP心跳断开  检查界面  启动APP=***********");
                                CMDUtil.restartApp(context);
                                simulateClickCrash(context);
                            }
                        } else {
                            System.out.println("***********APP心跳断开 还在当前界面=***********");
                        }
                    }
                });

    }


    /**
     * 点击屏幕的崩溃提示 如果有的话
     */
    public static void simulateClickCrash(Context mcontext) {
        Observable.just(mcontext)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Context>() {
                    @Override
                    public void accept(Context context) throws Exception {
                        Point size = getSize(context);
                        Log.d(TAG, "simulateClick: size=" + size.toString());
                        Point point1 = calculatePointCrash(size);
                        DataOutputStream dataOutputStream = null;
                        try {
                            // 申请su权限
                            Process process = Runtime.getRuntime().exec("su");
                            dataOutputStream = new DataOutputStream(process.getOutputStream());
                            // 执行pm install命令
                            String command2 = "input tap " + point1.x + " " + point1.y + "\n"; //force-stop;  680   750
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
     * 点击屏幕上对应位置选择启动程序
     */
    private static void simulateClick(Context context) {
        //            com.android.internal.app.ResolverActivity  //判断在当前activity界面 执行点击事件
        //F701  x 130   y1 474   y2 550
        //F902  x 220   y1 680   y2 750

        //默认点击位置，计算屏幕大小 适配机型
        //获取屏幕像素的 不包含导航栏高度
        Point size = getSize(context);
        Log.d(TAG, "simulateClick: size=" + size.toString());
        Point point1 = calculatePoint1(size);
        Point point2 = calculatePoint2(size);

        DataOutputStream dataOutputStream = null;
        try {
            // 申请su权限
            Process process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            // 执行pm install命令
            String command2 = "input tap " + point1.x + " " + point1.y + "\n"; //force-stop;  680   750
            String command3 = "input tap " + point2.x + " " + point2.y + "\n"; //force-stop;  680   750
            dataOutputStream.write(command2.getBytes(Charset.forName("utf-8")));
            dataOutputStream.write(command3.getBytes(Charset.forName("utf-8")));

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

    /**
     * 计算适配位置1
     */
    private static Point calculatePoint1(Point point) {
        int x = 0;
        int y = 0;
        /***F701**/
        if (point.x > 450 && point.x <= 500) {
            x = (int) (point.x * 0.27);
        }

        if (point.y > 780 && point.y <= 840) {
            y = (int) (point.y * 0.6);
        }

        /***F902**/
        if (point.x > 780 && point.x <= 850) {
            x = (int) (point.x * 0.27);
        }

        if (point.y > 1100 && point.y <= 1300) {
            y = (int) (point.y * 0.57);
        }
        return new Point(x, y);
    }

    /**
     * 计算适配位置2
     */
    private static Point calculatePoint2(Point point) {
        int x = 0;
        int y = 0;
        /***F701**/
        if (point.x > 450 && point.x <= 500) {
            x = (int) (point.x * 0.27);
        }

        if (point.y > 780 && point.y <= 840) {
            y = (int) (point.y * 0.7);
        }

        /***F902**/
        if (point.x > 780 && point.x <= 850) {
            x = (int) (point.x * 0.27);
        }

        if (point.y > 1100 && point.y <= 1300) {
            y = (int) (point.y * 0.63);
        }
        return new Point(x, y);
    }

    /**
     * 计算适配崩溃提示框框
     */
    private static Point calculatePointCrash(Point point) {
        int x = 0;
        int y = 0;
        /***F701**/
        if (point.x > 450 && point.x <= 500) {
            x = (int) (point.x * 0.5);
        }

        if (point.y > 780 && point.y <= 840) {
            y = (int) (point.y * 0.57);
        }

        /***F902**/
        if (point.x > 780 && point.x <= 850) {
            x = (int) (point.x * 0.6);
        }

        if (point.y > 1100 && point.y <= 1300) {
            y = (int) (point.y * 0.59);
        }
        return new Point(x, y);
    }


    /**
     * 该方法获取屏幕分辨率的值
     *
     * @param context 调用此方法的上下文对象
     * @return Point 存有屏幕分辨率的对象
     */
    public static Point getSize(Context context) {
        // 通过上下文的getSystemService()方法获取系统服务来获取WindowManager对象
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // 通过WindowManager对象的getDefaultDisplay()方法获取Display对象
        Display display = windowManager.getDefaultDisplay();
        // 使用Point来保存屏幕宽、高两个数据
        Point outSize = new Point();
        // 通过Display对象获取屏幕宽、高数据并保存到Point对象中
        display.getSize(outSize);
        return outSize;
    }

    /**
     * 检查当前显示activity
     *
     * @param className
     * @return
     */
    private static boolean isForeground(Context context, String className) {
//        if (context == null || TextUtils.isEmpty(className)) {
//            return false;
//        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            Log.d(TAG, "isForeground: 当前显示的主界面=" + cpn.getClassName());
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前显示activity 是否为本应用程序的
     *
     * @return
     */
    private static boolean isForegroundContainsPackage(Context context) {
//        if (context == null || TextUtils.isEmpty(className)) {
//            return false;
//        }
        String packageName = context.getPackageName();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
//            Log.d(TAG, "isForeground: 当前显示的主界面=" + cpn.getClassName());
            System.out.println("***********APP心跳断开   检查界面 当前显示的主界面 =***********" + cpn.getClassName());
            String className = cpn.getClassName();
            if (!TextUtils.isEmpty(className)) {
                if (className.contains(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
