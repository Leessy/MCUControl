package com.onfacemind.mculibrary;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.onfacemind.mculibrary.JT808.util.CMDUtil;

public class ANRWatchDog extends Thread {
    public static final int MESSAGE_WATCHDOG_TIME_TICK = 0;
    /**
     * 判定Activity发生了ANR的时间，必须要小于5秒，否则等弹出ANR，可能就被用户立即杀死了。
     */
    public static final int ACTIVITY_ANR_TIMEOUT = 6000;


    private static int lastTimeTick = -1;
    private static int timeTick = 0;

    Context mContext;

//    static ANRWatchDog anrWatchDog;

//    public static ANRWatchDog getInstance(Context context) {
//        if (anrWatchDog == null) {
//            anrWatchDog = new ANRWatchDog(context);
//        }
//        return anrWatchDog;
//    }
//
//    @Override
//    public synchronized void start() {
//        super.start();
//    }

    public ANRWatchDog(Context context) {
        System.out.println("----------------*************ANR ss");
        this.mContext = context;
    }

    private Handler watchDogHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            timeTick++;
            timeTick = timeTick % Integer.MAX_VALUE;
        }
    };

    @Override
    public void run() {
        while (true) {
            watchDogHandler.sendEmptyMessage(MESSAGE_WATCHDOG_TIME_TICK);
            try {
                Thread.sleep(ACTIVITY_ANR_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("----------------*************ANR==lastTimeTick=" + lastTimeTick);
            //如果相等，说明过了ACTIVITY_ANR_TIMEOUT的时间后watchDogHandler仍没有处理消息，已经ANR了
            if (timeTick == lastTimeTick) {
                throw new ANRException();
            } else {
                lastTimeTick = timeTick;
            }
        }
    }

    public class ANRException extends RuntimeException {
        public ANRException() {
            System.out.println("----------------*************ANR");
//            android.os.Process.killProcess(android.os.Process.myPid());//再此之前可以做些退出等操作
//            System.exit(1);

//            CMDUtil.restartApp(mContext);


        }
    }

}
