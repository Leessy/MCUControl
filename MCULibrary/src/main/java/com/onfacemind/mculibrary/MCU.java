package com.onfacemind.mculibrary;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.onfacemind.mculibrary.JT808.Interface.MCUbase;
import com.onfacemind.mculibrary.JT808.Interface.MCUcontrol;
import com.onfacemind.mculibrary.JT808.Msg.TPMSConsts;
import com.onfacemind.mculibrary.JT808.Server.MCUDataServer;
import com.onfacemind.mculibrary.JT808.util.CMDUtil;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * mcu 消息控制中心
 */
public class MCU extends MCUbase {
    private static final String TAG = "MCU_LC";
    private static MCU mcu;
    private Context context;
    private boolean DEBUG_LIB;
    PublishSubject<byte[]> SubjectData = PublishSubject.create();
    PublishSubject<PackageData> SubjectPackageData = PublishSubject.create();

    private IMCUAidlInterface imcuAidlInterface;//mcu服务接口

    static {
        mcu = new MCU();
    }

    public static MCUcontrol instance() {
        return mcu != null ? mcu : new MCU();
    }


    //启动 MCU服务
    @Override
    public void startServer(Context context) {
        this.context = context;
        DEBUG_LIB = context.getPackageName().equals("com.onfacemind.mculib");
//        关闭测试demo，防止占用串口,不是本身程序 就关闭
        if (!DEBUG_LIB) {
            CMDUtil.killProgress("com.onfacemind.mculib");
        }

        Intent intent = new Intent(context, MCUDataServer.class);
        context.startService(intent);
        context.bindService(intent, conn, Service.BIND_AUTO_CREATE);

//        Intent intent = new Intent();
//        intent.setAction("com.onfacemind.mculibrary");//绑定远程服务
//        context.startService(intent);
//        context.bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    //应用停止 解绑服务
    @Override
    public void stopServer() {
        context.unbindService(conn);
    }

    /**
     * 希望获取mcu原始数据 但是原始数据与对象 只能使用一种
     *
     * @return
     */
    @Override
    public Observable<byte[]> getMCUDatas() {
        return SubjectData;
    }

    /**
     * 希望获取mcu 解析对象数据 通常情况使用
     * 如果希望获取纯byte[] 需修改服务配置
     * <p>
     * 使用具体方法对象
     * getMCUNotification
     * getMCUQuery_paramenter
     * getMcu_Common
     * getNotification_Livings
     *
     * @return
     */
    @Deprecated
    @Override
    public Observable<PackageData> getPackageData() {
        return SubjectPackageData;
    }

    //接收回调
    IMCUNotification notification = new IMCUNotification.Stub() {
        @Override
        public void MCUDatas(byte[] bs) throws RemoteException {
            if (DEBUG_LIB) {
                if (SubjectData != null && bs != null && bs.length > 0) {
                    SubjectData.onNext(bs);
                }
            }
        }

        @Override
        public void MCUDataWhole(PackageData data) throws RemoteException {
//            Log.d(TAG, "MCUDataWhole: 收到MCU对象数据=" + data);
            switch (data.msgHeader.msgId) {
                case TPMSConsts.msg_id_terminal_common_resp://MCU回复通用应答(考虑在服务自动回复通用应答)
                    Log.d(TAG, "MCUDataWhole: 收到 通用应答--");
                    Receive_common_respData(data);
                    break;
                case TPMSConsts.msg_id_terminal_notification://MCU通知
                    Log.d(TAG, "MCUDataWhole: 收到 通知-");
                    Receive_notification_Data(data);
                    break;
                case TPMSConsts.msg_id_terminal_query://MCU查询应答
                    Log.d(TAG, "MCUDataWhole: 收到 查询应答-");
                    Receive_query_Data(data);
                    break;
            }
        }
    };

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "绑定成功调用：onServiceConnected");
            imcuAidlInterface = IMCUAidlInterface.Stub.asInterface(service);
            try {
                imcuAidlInterface.setNotification(notification);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
//            启动应用与服务心跳
            if (DEBUG_LIB) {
                return;
            }
            if (null != AppheartBeatsubscribe && !AppheartBeatsubscribe.isDisposed()) {
                AppheartBeatsubscribe.dispose();
            }
            AppheartBeat();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mcu.startServer(mcu.context);//重新连接
        }
    };
    Disposable AppheartBeatsubscribe;

    /**
     * 应用心跳从main线程发出，避免anr
     */
    private void AppheartBeat() {
        Heartbeat();
        AppheartBeatsubscribe = Observable.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Heartbeat();
                        AppheartBeat();
                    }
                });
    }


    //App 回复MCU 通用应答
    @Override
    public void CommanRreply(PackageData packageData) {
        //暂不回复消息
    }

    @Override
    public void Heartbeat() {
        try {
            imcuAidlInterface.onHeartbeat();//发送心跳测试
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***************************MCU控制 start  *********************************/
    @Override
    public void White_light_Open() {
        try {
            imcuAidlInterface.White_light_Open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void White_light_Close() {
        try {
            imcuAidlInterface.White_light_Close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Red_light_Open() {
        try {
            imcuAidlInterface.Red_light_Open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Red_light_Close() {
        try {
            imcuAidlInterface.Red_light_Close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Relay_interval_Open(int time) {
        try {
            imcuAidlInterface.Relay_interval_Open(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Button_LED_close() {
        try {
            imcuAidlInterface.Button_LED_close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Button_LED_constant() {
        try {
            imcuAidlInterface.Button_LED_constant();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Button_LED_FastFlash() {
        try {
            imcuAidlInterface.Button_LED_FastFlash();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Button_LED_SlowFlash() {
        try {
            imcuAidlInterface.Button_LED_SlowFlash();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void HUMMER_LED_close() {
        try {
            imcuAidlInterface.HUMMER_LED_close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void HUMMER_LED_constant() {
        try {
            imcuAidlInterface.HUMMER_LED_constant();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void HUMMER_LED_FastFlash() {
        try {
            imcuAidlInterface.HUMMER_LED_FastFlash();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void HUMMER_LED_SlowFlash() {
        try {
            imcuAidlInterface.HUMMER_LED_SlowFlash();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void heartbeat_open() {
        try {
            imcuAidlInterface.heartbeat_open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void heartbeat_close() {
        try {
            imcuAidlInterface.heartbeat_close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /***************************MCU控制  end  *********************************/


    /***************************MCU查询  start *********************************/
    @Override
    public void Body_infrared_signal() {
        try {
            imcuAidlInterface.Body_infrared_signal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Light_sensitive_resistance_signal() {
        try {
            imcuAidlInterface.Light_sensitive_resistance_signal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Light_sensitive_resistance_signal_AD() {
        try {
            imcuAidlInterface.Light_sensitive_resistance_signal_AD();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void white_light_parameters() {
        try {
            imcuAidlInterface.white_light_parameters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void white_light_parameters_PWM() {
        try {
            imcuAidlInterface.white_light_parameters_PWM();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void red_light_parameters() {
        try {
            imcuAidlInterface.red_light_parameters();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void red_light_parameters_PWM() {
        try {
            imcuAidlInterface.red_light_parameters_PWM();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Gate_magnetic_sensor_signal() {
        try {
            imcuAidlInterface.Gate_magnetic_sensor_signal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Antiknock_sensor_signal() {
        try {
            imcuAidlInterface.Antiknock_sensor_signal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void VersionCode() {
        try {
            imcuAidlInterface.VersionCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Light_sensitive_ret() {
        try {
            imcuAidlInterface.Light_sensitive_ret();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void user_defined_value() {
        try {
            imcuAidlInterface.user_defined_value();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mcu_id() {
        try {
            imcuAidlInterface.mcu_id();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /***************************MCU查询  end *********************************/


    /***************************MCU参数修改  start *********************************/
    @Override
    public void setLight_sensitive_resistance_signal_AD(int[] ret) {
        try {
            imcuAidlInterface.setLight_sensitive_resistance_signal_AD(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setWhite_light_parameters_PWM(int[] ret) {
        try {
            imcuAidlInterface.setWhite_light_parameters_PWM(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setRed_light_parameters_PWM(int[] ret) {
        try {
            imcuAidlInterface.setRed_light_parameters_PWM(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUser_defined_value(byte[] ret) {
        try {
            imcuAidlInterface.setUser_defined_value(ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /***************************MCU参数修改  end *********************************/


    /***************************IO数据  start *********************************/

    @Override
    public void SendIO_1_up() {
        try {
            imcuAidlInterface.SendIO_1_up();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_1_low() {
        try {
            imcuAidlInterface.SendIO_1_low();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_2_up() {
        try {
            imcuAidlInterface.SendIO_2_up();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_2_low() {
        try {
            imcuAidlInterface.SendIO_2_low();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_3_up() {
        try {
            imcuAidlInterface.SendIO_3_up();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_3_low() {
        try {
            imcuAidlInterface.SendIO_3_low();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_4_up() {
        try {
            imcuAidlInterface.SendIO_4_up();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_4_low() {
        try {
            imcuAidlInterface.SendIO_4_low();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_5_up() {
        try {
            imcuAidlInterface.SendIO_5_up();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_5_low() {
        try {
            imcuAidlInterface.SendIO_5_low();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_6_up() {
        try {
            imcuAidlInterface.SendIO_6_up();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SendIO_6_low() {
        try {
            imcuAidlInterface.SendIO_6_low();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /***************************IO数据  end *********************************/

}
