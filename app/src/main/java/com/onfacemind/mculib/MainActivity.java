package com.onfacemind.mculib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onfacemind.mculibrary.JT808.Msg.MsgDecoder;
import com.onfacemind.mculibrary.JT808.Msg.TPMSConsts;
import com.onfacemind.mculibrary.JT808.util.HexStringUtils;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Common;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Notification;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_query_response;
import com.onfacemind.mculibrary.JT808.vo.SendMsg.Mcu_SendMsg_Type;
import com.onfacemind.mculibrary.MCU;
import com.onfacemind.mculibrary.PackageData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    TextView text;
    EditText edit_1, edit_2, edit_3, edit_4, edit_5, edit_6, edit_7, edit_8, edit_9, edit_10, edit_11, edit_12, edit_13, edit_14, edit_15, edit_16;
    EditText[] editTexts = {edit_1, edit_2, edit_3, edit_4, edit_5, edit_6, edit_7, edit_8, edit_9, edit_10, edit_11, edit_12, edit_13, edit_14, edit_15, edit_16};
    List<EditText> editTextsList = new ArrayList<>();

    MsgDecoder msgDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ??---" + Arrays.toString(("0".getBytes())));
        byte[] bytes = new byte[16];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 48;
        }

        Log.d(TAG, "onCreate: ??/=" + new String(bytes));
        text = findViewById(R.id.textView);
        MCU.instance().startServer(MainActivity.this);//绑定服务

        edit_1 = findViewById(R.id.edit_1);
        edit_2 = findViewById(R.id.edit_2);
        edit_3 = findViewById(R.id.edit_3);
        edit_4 = findViewById(R.id.edit_4);
        edit_5 = findViewById(R.id.edit_5);
        edit_6 = findViewById(R.id.edit_6);
        edit_7 = findViewById(R.id.edit_7);
        edit_8 = findViewById(R.id.edit_8);
        edit_9 = findViewById(R.id.edit_9);
        edit_10 = findViewById(R.id.edit_10);
        edit_11 = findViewById(R.id.edit_11);
        edit_12 = findViewById(R.id.edit_12);
        edit_13 = findViewById(R.id.edit_13);
        edit_14 = findViewById(R.id.edit_14);
        edit_15 = findViewById(R.id.edit_15);
        edit_16 = findViewById(R.id.edit_16);
        editTextsList.add(edit_1);
        editTextsList.add(edit_2);
        editTextsList.add(edit_3);
        editTextsList.add(edit_4);
        editTextsList.add(edit_5);
        editTextsList.add(edit_6);
        editTextsList.add(edit_7);
        editTextsList.add(edit_8);
        editTextsList.add(edit_9);
        editTextsList.add(edit_10);
        editTextsList.add(edit_11);
        editTextsList.add(edit_12);
        editTextsList.add(edit_13);
        editTextsList.add(edit_14);
        editTextsList.add(edit_15);
        editTextsList.add(edit_16);
        text.setMovementMethod(ScrollingMovementMethod.getInstance());

        msgDecoder = new MsgDecoder();
        MCU.instance().getMCUDatas().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<byte[]>() {
            @Override
            public void accept(byte[] bytes) throws Exception {
                Log.d(TAG, "accept: App接收数据=" + HexStringUtils.toHexString(bytes));
                refreshLogView("接收数据=" + HexStringUtils.toHexString(bytes) + "\n");
                int scrollAmount = text.getLayout().getLineTop(text.getLineCount())
                        - text.getHeight();
                if (scrollAmount > 0)
                    text.scrollTo(0, scrollAmount);
                else
                    text.scrollTo(0, 0);
            }
        });


//        Observable.merge(MCU.instance().getMCUNotification(), MCU.instance().getMcu_Common(), MCU.instance().getMCUQuery_paramenter())
//                .subscribe(new Consumer<Object>() {
//                })

        //通用回复
        MCU.instance().getMcu_Common().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Mcu_Common>() {
                    @Override
                    public void accept(Mcu_Common mcu_common) throws Exception {
                        Log.d(TAG, "MCUDataWhole: 收到 通用应答--");
                        int replyFlowId = mcu_common.getReplyFlowId();
                        int replyId = mcu_common.getReplyId();
                        byte replyCode = mcu_common.getReplyCode();
                        byte[] bytes = {(byte) ((replyId >> 8) & 0xFF), (byte) ((replyId) & 0xFF)};
                        if (replyId == 0x8002) {
                            refreshLogView("心跳-应答 =" + HexStringUtils.toHexString(bytes) + "---结果 ：" + replyCode + "---心跳状态：" + mcu_common.heartbeatStatus + "\n");
                        } else
                            refreshLogView("通用应答 =" + HexStringUtils.toHexString(bytes) + "---结果 ：" + replyCode + "\n");
                    }
                });
        //通知
        MCU.instance().getMCUNotification()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Mcu_Notification>() {
                    @Override
                    public void accept(Mcu_Notification mcu_notification) throws Exception {
                        Receive_notification_Data(mcu_notification);

                    }
                });
        //查询
        MCU.instance().getMCUQuery_paramenter().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Mcu_query_response>() {
                    @Override
                    public void accept(Mcu_query_response mcu_query_response) throws Exception {
                        Log.d(TAG, "MCUDataWhole: 收到 查询应答-");
                        Receive_query_Data(mcu_query_response);
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
//        finish();
    }

    //显示text
    void refreshLogView(String msg) {
        if (text.getLineCount() > 300) {
            text.setText("");
        }
        text.append(msg);
    }

    //通知
    private void Receive_notification_Data(Mcu_Notification mcu_notification) {
        Log.d(TAG, "MCUDataWhole: 收到 通知-");
        int type = mcu_notification.getNotification_type();
        int rets = mcu_notification.getRet();//通知结果
        switch (type) {
            case Mcu_SendMsg_Type.MCU_Notification.infrared_reaction://人体红外信号	0：触发通知（下降沿）1：撤除通知（上升沿）
                Log.d("MCU_Common_MSG 通知消息", "type---人体红外信号    结果=" + rets);
                refreshLogView("通知消息 ---人体红外信号    结果=" + rets + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_Notification.fluxgate_sensor://门磁传感 触发	0：下升沿通知（常开型门磁的关门事件）1：上升沿通知（常开型门磁的开门事件）
                Log.d("MCU_Common_MSG 通知消息", "type---门磁传感触发    结果=" + rets);
                refreshLogView("通知消息 ---门磁传感触发    结果=" + rets + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_Notification.Shock_Sensor://震动传感器信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
                Log.d("MCU_Common_MSG 通知消息", "type---震动传感器信号    结果=" + rets);
                refreshLogView("通知消息 ---震动传感器信号    结果=" + rets + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_Notification.urgency_signal://紧急按钮信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
                Log.d("MCU_Common_MSG 通知消息", "type---紧急按钮信号    结果=" + rets);
                refreshLogView("通知消息 ---紧急按钮信号    结果=" + rets + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_Notification.dismantle_Sensor://防拆传感器 触发	0：解除通知（稳定3秒以上的低电平）
                Log.d("MCU_Common_MSG 通知消息", "type---防拆传感器    结果=" + rets);
                refreshLogView("通知消息 ---防拆传感器    结果=" + rets + "\n");
                break;
        }
    }

    protected void Receive_query_Data(Mcu_query_response mcu_query_response) {
        int type = mcu_query_response.getQuery_type();//查询类型
//        int rets = mcu_query_response.getRet();//查询结果
        switch (type) {
            case Mcu_SendMsg_Type.MCU_QueryParameter.Body_infrared_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---人体红外信号    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---人体红外信号    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_resistance_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---光敏电阻信号等级    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---光敏电阻信号    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_resistance_signal_AD://DWORD[16]	16级亮度的AD值表
                Log.d("MCU_Common_MSG 查询结果", "type---光敏电阻信号    16级亮度=" + mcu_query_response.getLevels().toString());
                refreshLogView("查询结果 ---光敏电阻信号16级    结果=" + mcu_query_response.getLevels().toString() + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.white_light_parameters:
                Log.d("MCU_Common_MSG 查询结果", "type---白光参数亮度    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---白光参数亮度    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.white_light_parameters_PWM://DWORD[16]	16级亮度的PWM值表
                Log.d("MCU_Common_MSG 查询结果", "type---白光参数      16级亮度=" + mcu_query_response.getLevels().toString());
                refreshLogView("查询结果 ---白光参数16级亮度    结果=" + mcu_query_response.getLevels().toString() + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.red_light_parameters://BYTE	亮度等级 0-15
                Log.d("MCU_Common_MSG 查询结果", "type---红光参数亮度    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---红光参数亮度    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.red_light_parameters_PWM://DWORD[16]	16级亮度的PWM值表
                Log.d("MCU_Common_MSG 查询结果", "type---红光参数      16级亮度=" + mcu_query_response.getLevels().toString());
                refreshLogView("查询结果 ---红光参数16级亮度    结果=" + mcu_query_response.getLevels().toString() + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Gate_magnetic_sensor_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---门磁传感器信号    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---门磁传感器信号    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Antiknock_sensor_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---防拆传感器信号    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---防拆传感器信号    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.VersionCode:
                Log.d("MCU_Common_MSG 查询结果", "type---版本号    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---版本号    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_ret:
                Log.d("MCU_Common_MSG 查询结果", "type---光敏电阻信号值    结果=" + mcu_query_response.ret);
                refreshLogView("查询结果 ---光敏电阻信号值    结果=" + mcu_query_response.ret + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.user_defined_value:
                Log.d("MCU_Common_MSG 查询结果", "type---自定义值    结果=" + Arrays.toString(mcu_query_response.user_defined_value));
                refreshLogView("查询结果 ---自定义值    结果=" + Arrays.toString(mcu_query_response.user_defined_value) + "\n");
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.mcu_id:
                Log.d("MCU_Common_MSG 查询结果", "type---MUC_ID    结果=" + Arrays.toString(mcu_query_response.mcu_id));
                refreshLogView("查询结果 ---MUC_ID    结果=" + Arrays.toString(mcu_query_response.mcu_id) + "\n");
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MCU.instance().stopServer();
//        showBar();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    //心跳开
    public void heartOpen(View view) {
        MCU.instance().getMCU_IO_MSG().SendIO_3_up();
    }

    //心跳关
    public void heartClose(View view) {
        MCU.instance().getMCU_IO_MSG().SendIO_3_low();
    }

    //发送数据
    public void heat(View view) {
        MCU.instance().getAppRreply().Heartbeat();
    }

    public void test2(View view) {
        MCU.instance().getMCU_Control().White_light_Close();
    }

    public void test1(View view) {
        MCU.instance().getMCU_Control().White_light_Open();
    }


    //HUMMER1 蜂鸣器
    public void HUMMER2(View view) {
        MCU.instance().getMCU_Control().HUMMER_LED_FastFlash();
    }

    //HUMMER1 蜂鸣器
    public void HUMMER0(View view) {
        MCU.instance().getMCU_Control().HUMMER_LED_close();
    }

    //心跳开
    public void heartbeat(View view) {
        MCU.instance().getMCU_Control().heartbeat_open();
    }

    //心跳关
    public void heartbeatStop(View view) {
        MCU.instance().getMCU_Control().heartbeat_close();
    }

    //红光开关
    public void red1(View view) {
        MCU.instance().getMCU_Control().Red_light_Open();
    }

    public void red0(View view) {
        MCU.instance().getMCU_Control().Red_light_Close();
    }

    //继电器
    public void jidianqi(View view) {
        MCU.instance().getMCU_Control().Relay_interval_Open(10);
    }

    //按键
    public void bt0(View view) {
        MCU.instance().getMCU_Control().Button_LED_close();
    }

    //按键
    public void bt1(View view) {
        MCU.instance().getMCU_Control().Button_LED_constant();
    }

    //按键
    public void bt2(View view) {
        MCU.instance().getMCU_Control().Button_LED_FastFlash();
    }

    //按键
    public void bt3(View view) {
        MCU.instance().getMCU_Control().Button_LED_SlowFlash();
    }


    public void test3(View view) {
        text.setText("");
    }
    //查询类 1-9

    public void q1(View view) {
        MCU.instance().getMCU_query().Body_infrared_signal();
    }

    public void q2(View view) {
        MCU.instance().getMCU_query().Light_sensitive_resistance_signal();
    }

    public void q3(View view) {
        MCU.instance().getMCU_query().Light_sensitive_resistance_signal_AD();
    }

    public void q4(View view) {
        MCU.instance().getMCU_query().white_light_parameters();
    }

    public void q5(View view) {
        MCU.instance().getMCU_query().white_light_parameters_PWM();
    }

    public void q6(View view) {
        MCU.instance().getMCU_query().red_light_parameters();
    }

    public void q7(View view) {
        MCU.instance().getMCU_query().red_light_parameters_PWM();
    }

    public void q8(View view) {
        MCU.instance().getMCU_query().Gate_magnetic_sensor_signal();
    }

    public void q9(View view) {
        MCU.instance().getMCU_query().Antiknock_sensor_signal();
    }

    public void qA(View view) {
        MCU.instance().getMCU_query().VersionCode();
    }

    public void qB(View view) {
        MCU.instance().getMCU_query().Light_sensitive_ret();
    }

    public void qC(View view) {
        MCU.instance().getMCU_query().user_defined_value();
    }

    public void qF(View view) {
        MCU.instance().getMCU_query().mcu_id();
    }


    public void set03(View view) {
        int[] allarray = getAllarray();
        if (allarray[0] > allarray[allarray.length - 1])
            MCU.instance().getMCU_Set_Parameter().setLight_sensitive_resistance_signal_AD(allarray);
        else Toast.makeText(this, " 顺序错误, 请调整！！！", Toast.LENGTH_LONG).show();
    }

    public void set05(View view) {
        int[] allarray = getAllarray();
        if (allarray[0] < allarray[allarray.length - 1])
            MCU.instance().getMCU_Set_Parameter().setWhite_light_parameters_PWM(allarray);
        else Toast.makeText(this, " 顺序错误, 请调整！！！", Toast.LENGTH_LONG).show();
    }

    public void set07(View view) {
        int[] allarray = getAllarray();
        if (allarray[0] < allarray[allarray.length - 1])
            MCU.instance().getMCU_Set_Parameter().setRed_light_parameters_PWM(allarray);
        else Toast.makeText(this, " 顺序错误, 请调整！！！", Toast.LENGTH_LONG).show();
    }

    public void IO1(View view) {
        MCU.instance().getMCU_IO_MSG().SendIO_1_low();
    }

    public void IO2(View view) {
        MCU.instance().getMCU_IO_MSG().SendIO_1_up();
    }

    //显示服务窗口
    public void floatView(View view) {
        MCU.instance().getMCU_IO_MSG().SendIO_2_up();
    }

    public void textOrde(View view) {
        int[] ints = new int[16];
        for (int i = 0; i < editTextsList.size(); i++) {
            ints[i] = EditTextGetInt(editTextsList.get(i));
        }
        for (int i = 0; i < editTextsList.size(); i++) {
            editTextsList.get(i).setText("" + ints[15 - i]);
        }
    }


    private int EditTextGetInt(EditText edit) {
        String s = edit.getText().toString();
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        return Integer.parseInt(s);
    }

    //获取输入的16级亮度
    private int[] getAllarray() {
        int[] ints = new int[16];
        for (int i = 0; i < editTextsList.size(); i++) {
            ints[i] = EditTextGetInt(editTextsList.get(i));
        }
        return ints;
    }

    /**
     * 开始自动发送 led
     *
     * @param view
     */
    public void testOpenLed(View view) {
        if (!isTest) {
            testLeds();
            isTest = true;
        }
    }

    private void testLeds() {
        int random = getRandom(4, 8);
        Log.d(TAG, "testLeds: ="+random);
        Observable.timer(random, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                if (!openLed) {
                    MCU.instance().getMCU_Control().White_light_Open();
                    MCU.instance().getMCU_Control().Red_light_Open();
                    MCU.instance().getMCU_Control().Button_LED_FastFlash();
                    openLed = true;
                } else {
                    MCU.instance().getMCU_Control().White_light_Close();
                    MCU.instance().getMCU_Control().Red_light_Close();
                    MCU.instance().getMCU_Control().Button_LED_SlowFlash();
                    openLed = false;
                }
                if (isTest) {
                    testLeds();
                }
            }
        });
    }

    boolean openLed;
    boolean isTest;

    /**
     * 关闭测试
     *
     * @param view
     */
    public void testcloseLed(View view) {
        isTest = false;
    }

    public int getRandom(int min, int max) {
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;

    }
//    int[] ints = {250,
//            500,
//            750,
//            1000,
//            1250,
//            1500,
//            1750,
//            2000,
//            2250,
//            2500,
//            2750,
//            3000,
//            3250,
//            3500,
//            3750,
//            4000,
//    };
}
