// IMCUAidlInterface.aidl
package com.onfacemind.mculibrary;
import com.onfacemind.mculibrary.IMCUNotification;
// Declare any non-default types here with import statements

interface IMCUAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
          //传递回调对象
    void setNotification(in IMCUNotification Notification);

    //心跳包
    void onHeartbeat( );


    //////////////////////// SendControlMsg ////////////////////////////////
        //白光开
        void White_light_Open();

        //白光关
        void White_light_Close();

        //红光开
        void Red_light_Open();

        //红光关
        void Red_light_Close();

        //继电器 开启 吸合时间  1-99 秒
        void Relay_interval_Open(int time);

        // LED 按键灯   0：熄灭 1：常量 2：快闪 3：慢闪
        //关闭
        void Button_LED_close();

        //常亮
        void Button_LED_constant();

        //快闪
        void Button_LED_FastFlash();

        //慢闪
        void Button_LED_SlowFlash();

        // 蜂鸣器控制   0：关闭 1：常响 2：快响 3：慢响
        //关闭
        void HUMMER_LED_close();

        //常响
        void HUMMER_LED_constant();

        //快响
        void HUMMER_LED_FastFlash();

        //慢响
        void HUMMER_LED_SlowFlash();

        //心跳开
        void heartbeat_open();

        //心跳关
        void heartbeat_close();


            //////////////////////// SendQueryMsg ////////////////////////////////

    void Body_infrared_signal();

    void Light_sensitive_resistance_signal();

    void Light_sensitive_resistance_signal_AD();

    void white_light_parameters();

    void white_light_parameters_PWM();

    void red_light_parameters();

    void red_light_parameters_PWM();

    void Gate_magnetic_sensor_signal();

    void Antiknock_sensor_signal();

    void VersionCode();

    void Light_sensitive_ret();

    void user_defined_value();

    void mcu_id();

         //////////////////////// SendSetParameterMsg ////////////////////////////////

        //设置光敏电阻值
        void setLight_sensitive_resistance_signal_AD(in int[] ret);

        //设置白光亮度
        void setWhite_light_parameters_PWM(in int[] ret);

        //设置红光亮度
        void setRed_light_parameters_PWM(in int[] ret);

//        //16 byte
        void setUser_defined_value(in byte[] ret);

                //////////////////////// SendIO ////////////////////////////////
    //高电平
    void SendIO_1_up();

    //低电平
    void SendIO_1_low();

    //高电平
    void SendIO_2_up();

    //低电平
    void SendIO_2_low();

    //高电平
    void SendIO_3_up();

    //低电平
    void SendIO_3_low();

    //高电平
    void SendIO_4_up();

    //低电平
    void SendIO_4_low();

    //高电平
    void SendIO_5_up();

    //低电平
    void SendIO_5_low();

    //高电平
    void SendIO_6_up();

    //低电平
    void SendIO_6_low();

}
