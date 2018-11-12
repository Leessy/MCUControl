package com.onfacemind.mculibrary.JT808.Server;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.onfacemind.mculibrary.JT808.util.CMDUtil;
import com.onfacemind.mculibrary.JT808.util.RecordStartTime;
import com.onfacemind.mculibrary.JT808.util.ServerPreferences;
import com.onfacemind.mculibrary.Log;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onfacemind.mculibrary.BuildConfig;
import com.onfacemind.mculibrary.IMCUAidlInterface;
import com.onfacemind.mculibrary.IMCUNotification;
import com.onfacemind.mculibrary.JT808.LauncherUtil.SelectLauncherUtil;
import com.onfacemind.mculibrary.JT808.Msg.MsgDecoder;
import com.onfacemind.mculibrary.JT808.Msg.MsgEncoder;
import com.onfacemind.mculibrary.JT808.util.HexStringUtils;
import com.onfacemind.mculibrary.JT808.util.JT808ProtocolUtils;
import com.onfacemind.mculibrary.JT808.vo.SendMsg.Mcu_SendMsg_Type;
import com.onfacemind.mculibrary.PackageData;
import com.onfacemind.mculibrary.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SeriaProtController;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MCUDataServer extends Service {
    private static final String TAG = "MCUDataServer--MCU服务";
    private InputStream inputStream;

    boolean isDestroy;
    SeriaProtController controller;
    MsgDecoder msgDecoder;
    MsgEncoder msgEncoder;
    //    ExecutorService executorService;//用于发送的单线程池
    HandlerThread handlerThread;//代替单线程池模式
    Handler HandlerSengMSGThread;//代替单线程池模式
    private byte[] mReadBuffer;
    JT808ProtocolUtils jt808ProtocolUtils;
    IMCUNotification mNotification;
    PublishSubject AppHeartbeat;

    private boolean DEBUG_LIB;
    //定义浮动窗口布局
    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;
    TextView text;
    Context mContext;
    boolean isSendHeartMsg;
    Disposable Heartbeatsubscribe;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        DEBUG_LIB = getPackageName().equals("com.onfacemind.mculib");//是否为调试应用
        SelectLauncherUtil.shellInputKey(mContext);
        mReadBuffer = new byte[1024];
        msgDecoder = new MsgDecoder();
        msgEncoder = new MsgEncoder();
        jt808ProtocolUtils = new JT808ProtocolUtils();
        controller = SeriaProtController.instance();
        controller.create("/dev/ttySAC1", 115200);//默认串口  "/dev/ttySAC1"    115200 ---  ttySAC3=F902
        inputStream = controller.getmInputStream();

        startReceiveWork();
        initHandlerThread();
        showNotification();
//        createFloatView();
        initHeartbeat();//心跳包发送
        RecordStartTime.recordStartTime(mContext);
    }


    /**
     * 初始化发送线程
     */
    private void initHandlerThread() {
        handlerThread = new HandlerThread("HandlerSengMSGThread");
        handlerThread.start();

        HandlerSengMSGThread = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                controller.sendCommand((byte[]) msg.obj);

                //test
                if (BuildConfig.DEBUG) {
                    final byte[] bytes = (byte[]) msg.obj;
                    AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                        @Override
                        public void run() {
                            if (text != null && isShow) {
                                text.setText(text.getText() + "发送数据：" + HexStringUtils.toHexString(bytes) + "\n");
                            }
                        }
                    });
                }

            }
        };
    }

    /**
     * 初始化心跳发送 MCU 和 APP
     */
    private void initHeartbeat() {
        //开启心跳
        try {
            SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.heartbeat_switch, (byte) 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //心跳数据发送
        Heartbeatsubscribe = Observable.intervalRange(0, Long.MAX_VALUE, 0, 3, TimeUnit.SECONDS)
                .observeOn(Schedulers.io()).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
//                        if (mNotification != null)
                        if (!isSendHeartMsg)
                            SendCommandMsg(msgEncoder.HeartBeat());//发送心跳包
                    }
                });
        AppHeartbeat = PublishSubject.create();

        AppHeartbeat.debounce(17, TimeUnit.SECONDS)
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        System.out.println("***********APP心跳断开*********** i=" + o);

                        //点击提示崩溃窗口
                        if ((int) o == 1) {
                            SelectLauncherUtil.simulateClickCrash(mContext);//anr弹窗 或其他弹窗 点击确定
                            CMDUtil.restartApp(mContext);
                            return;
                        }
                        AppHeartbeat.onNext(1);//开始下一次判断计时
                        SelectLauncherUtil.simulateClickCrash(mContext);//anr弹窗 或其他弹窗 点击确定
//                        RecordStartTime.recordStartTime_AppHeart(mContext);
                        //直接重启改为 ---->  检查界面后执行操作 重启
                        SelectLauncherUtil.shellCrashCheckRestart(mContext);
                    }
                });
    }

    /**
     * app 与服务心跳  在应用无响应时会断开心跳
     */
    private void AppWithServerHeartbeat() {
        AppHeartbeat.onNext(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return new LocalBinder();
    }

    //发送数据到mcu  或许不应该用启用线程发送
    synchronized public void SendCommandMsg(final byte[] bytes) {
        Message message = HandlerSengMSGThread.obtainMessage();
        message.obj = bytes;
        HandlerSengMSGThread.sendMessage(message);
    }


    /**
     * 启动工作线程
     */
    public void startReceiveWork() {
        new mThread().start();
        Log.d(TAG, "startWork: 开启服务 工作线程");
    }

    /**
     * 读取数据线程
     */
    private class mThread extends Thread {
        byte[] byteswait;

        @Override
        public void run() {
            super.run();
            while (!isDestroy) {
                try {
                    if (null == inputStream) {
                        return;
                    }
                    int size = inputStream.read(mReadBuffer);
                    if (0 >= size) {
                        continue;
                    }
                    byte[] readBytes = new byte[size];
                    System.arraycopy(mReadBuffer, 0, readBytes, 0, size);
//                    Log.i(TAG, "run: 读取MCU数据 = " + HexStringUtils.toHexString(readBytes));
                    //分割数据
                    DelimiterBasedFrameDecoder(readBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        /**
         * 分割数据  并回调
         * 标识位 = 0x7e
         */
        protected void DelimiterBasedFrameDecoder(byte[] bss) {
            try {
                byte[] bs;
                if (byteswait != null) {//连接数据
                    bs = msgEncoder.bitOperator.concatAll(byteswait, bss);
                } else {
                    bs = bss;
                }
                int start = -1;
                for (int i = 0; i < bs.length; i++) {
                    if (bs[i] == 0x7e) {
                        if (start >= 0) {//有前一个标识位  截取数据
                            int length = i - start + 1;//两个标识位 间隔数据长度
                            if (length < 10) {
                                start = i;// 更新 记录标记符号位置
                                continue;//长度小于10 无效数据  过滤首尾相邻的0x7e数据
                            }
                            byte[] readBytes = new byte[length];
                            System.arraycopy(bs, start, readBytes, 0, readBytes.length);
//                            Log.d(TAG, "DelimiterBasedFrameDecoder: 截取成功--数据==" + HexStringUtils.toHexString(readBytes));
                            if (mNotification != null && DEBUG_LIB) {
                                mNotification.MCUDatas(readBytes);//发送纯数据用于应用层解析  现只在调试应用发送
                            }

                            byte[] data = jt808ProtocolUtils.doEscape4Receive(readBytes, 1, readBytes.length - 1);
                            Log.d(TAG, "DelimiterBasedFrameDecoder: 截取成功--转义====" + HexStringUtils.toHexString(data));
                            PackageData packageData = msgDecoder.bytes2PackageData(data);
//                            Log.d(TAG, "DelimiterBasedFrameDecoder: pacg=" + packageData);
                            if (mNotification != null) {
                                mNotification.MCUDataWhole(packageData);
                            }
                        }
                        start = i;//记录标记符号位置
                    }
                }
                if (start != bs.length - 1) {//0x7e不是最后一个数据 截取缓存
                    byteswait = new byte[bs.length - start];
                    System.arraycopy(bs, start, byteswait, 0, byteswait.length);
                    Log.d(TAG, "DelimiterBasedFrameDecoder: 缓存--数据==" + HexStringUtils.toHexString(byteswait));
                } else {
                    byteswait = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "DelimiterBasedFrameDecoder: 错误！！！=" + e.toString());
            }
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        mNotification = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        Heartbeatsubscribe.dispose();
        if (handlerThread != null) {
            handlerThread.quit();
        }
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
        try {
            controller.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onDestroy: ");
    }

    /**
     * 创建Binder对象，返回给客户端即Activity使用，提供数据交换的接口
     */
    public class LocalBinder extends IMCUAidlInterface.Stub {
        @Override
        public void setNotification(com.onfacemind.mculibrary.IMCUNotification Notification) throws RemoteException {
            mNotification = Notification;
            //点击一次屏幕中央   在应用程序崩溃重启时点击提示窗
            SelectLauncherUtil.simulateClickCrash(mContext);
        }

        @Override
        public void onHeartbeat() throws RemoteException {
            AppWithServerHeartbeat();
//            try {
//                SendCommandMsg(msgEncoder.HeartBeat());//发送心跳包
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        /***************************MCU控制 start  *********************************/
        @Override
        public void White_light_Open() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.White_light, (byte) 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void White_light_Close() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.White_light, (byte) 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Red_light_Open() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.red_light, (byte) 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Red_light_Close() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.red_light, (byte) 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Relay_interval_Open(int time) {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.Relay_absorption_interval, (byte) time));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Button_LED_close() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.Buttun_LED, (byte) 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Button_LED_constant() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.Buttun_LED, (byte) 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Button_LED_FastFlash() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.Buttun_LED, (byte) 2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Button_LED_SlowFlash() {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.Buttun_LED, (byte) 3));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void HUMMER_LED_close() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.HUMMER, (byte) 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void HUMMER_LED_constant() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.HUMMER, (byte) 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void HUMMER_LED_FastFlash() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.HUMMER, (byte) 2));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void HUMMER_LED_SlowFlash() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.HUMMER, (byte) 3));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void heartbeat_open() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.heartbeat_switch, (byte) 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void heartbeat_close() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.control_Mcu(Mcu_SendMsg_Type.Controls.heartbeat_switch, (byte) 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
/***************************MCU控制  end  *********************************/


        /***************************MCU查询  start *********************************/
        @Override
        public void Body_infrared_signal() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.Body_infrared_signal));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Light_sensitive_resistance_signal() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_resistance_signal));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Light_sensitive_resistance_signal_AD() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_resistance_signal_AD));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void white_light_parameters() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.white_light_parameters));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void white_light_parameters_PWM() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.white_light_parameters_PWM));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void red_light_parameters() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.red_light_parameters));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void red_light_parameters_PWM() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.red_light_parameters_PWM));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Gate_magnetic_sensor_signal() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.Gate_magnetic_sensor_signal));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Antiknock_sensor_signal() {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.Antiknock_sensor_signal));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void VersionCode() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.VersionCode));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void Light_sensitive_ret() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_ret));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void user_defined_value() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.user_defined_value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void mcu_id() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Query_Parameter(Mcu_SendMsg_Type.MCU_QueryParameter.mcu_id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        /***************************MCU查询  end *********************************/


        /***************************MCU参数修改  start *********************************/
        @Override
        public void setLight_sensitive_resistance_signal_AD(int[] ret) {
            try {
                SendCommandMsg(msgEncoder.set_Parameter(Mcu_SendMsg_Type.MCU_set_Parameter.Light_sensitive_resistance_signal_AD, ret));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setWhite_light_parameters_PWM(int[] ret) {
            try {
                SendCommandMsg(msgEncoder.set_Parameter(Mcu_SendMsg_Type.MCU_set_Parameter.white_light_parameters_PWM, ret));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setRed_light_parameters_PWM(int[] ret) {
            try {
                SendCommandMsg(msgEncoder.set_Parameter(Mcu_SendMsg_Type.MCU_set_Parameter.red_light_parameters_PWM, ret));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void setUser_defined_value(byte[] ret) {
            try {
                // TODO: 2018/7/27 待修改重载方法
//                SendCommandMsg(msgEncoder.set_Parameter(Mcu_SendMsg_Type.MCU_set_Parameter.red_light_parameters_PWM, ret));
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*********测试用代码***********/
            if (mWindowManager == null && mFloatLayout == null && wmParams == null) {
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        createFloatView();
                        isShow = true;
                    }
                });
                return;
            }

            if (!isShow) {
                isShow = true;
                //暂时做显示窗口用
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        mWindowManager.addView(mFloatLayout, wmParams);
                    }
                });
            }
        }


        /***************************MCU参数修改  end *********************************/


        /***************************MCU  IO发送 *********************************/

        @Override
        public void SendIO_1_up() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(1, 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_1_low() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(1, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_2_up() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(2, 1));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void SendIO_2_low() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(2, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_3_up() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(3, 1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: 2018/8/1 暂时做测试接口用 心跳开
            isSendHeartMsg = false;
        }

        @Override
        public void SendIO_3_low() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(3, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO: 2018/8/1 暂时做测试接口用 心跳关
            isSendHeartMsg = true;

        }

        @Override
        public void SendIO_4_up() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(4, 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_4_low() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(4, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_5_up() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(5, 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_5_low() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(5, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_6_up() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(6, 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void SendIO_6_low() throws RemoteException {
            try {
                SendCommandMsg(msgEncoder.Send_IO_Data(6, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    boolean isShow = false;//默认

    private void createFloatView() {
        wmParams = new WindowManager.LayoutParams();
        //获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        Log.i(TAG, "mWindowManager--->" + mWindowManager);
        //设置window type
//        wmParams.type = LayoutParams.TYPE_PHONE;//4.4及以前添加悬浮窗需要设置成TYPE_PHONE
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;//4.4之后，悬浮窗设置成TYPE_TOAST,不需要向系统申请权限
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
//        wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

         /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_layout, null);
        //添加mFloatLayout
        mWindowManager.addView(mFloatLayout, wmParams);
        //浮动窗口按钮
        Button close = mFloatLayout.findViewById(R.id.button);
        Button button2 = mFloatLayout.findViewById(R.id.button2);
        text = mFloatLayout.findViewById(R.id.text);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mWindowManager.removeView(mFloatLayout);
                text.setText("");
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFloatLayout != null) {
                    mWindowManager.removeView(mFloatLayout);
                    isShow = false;
                }
            }
        });

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        Log.i(TAG, "Width/2--->" + mFloatView.getMeasuredWidth() / 2);
//        Log.i(TAG, "Height/2--->" + mFloatView.getMeasuredHeight() / 2);
        //设置监听浮动窗口的触摸移动
        mFloatLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {//移动时
                    //getRawX是触摸位置相对于屏幕的坐标，getX是相对于按钮的坐标
                    wmParams.x = (int) event.getRawX() - mFloatLayout.getMeasuredWidth() / 2;
//                    Log.i(TAG, "RawX" + event.getRawX());
//                    Log.i(TAG, "X" + event.getX());
                    //减25为状态栏的高度
                    wmParams.y = (int) event.getRawY() - mFloatLayout.getMeasuredHeight() / 2;
//                    Log.i(TAG, "RawY" + event.getRawY());
//                    Log.i(TAG, "Y" + event.getY());
                    //刷新
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                }
                return false;  //此处必须返回false，否则OnClickListener获取不到监听
            }
        });

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
////                Toast.makeText(FxService.this, "onClick", Toast.LENGTH_SHORT).show();
////                Intent intent = new Intent(MCUDataServer.this, MainActivity.class);
////                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                startActivity(intent);
//            }
//        });
    }

    //显示同时 前台服务
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    void showNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(), 0);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.face_notification);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.face_notification));//下拉下拉列表里面的图标（大图标）
        builder.setTicker("  MCU服务启动.....");
        builder.setContentTitle("MCU服务运行中");
        builder.setContentText(getPackageName());
        Notification notification = builder.build();
        startForeground(1, notification);
    }
}
