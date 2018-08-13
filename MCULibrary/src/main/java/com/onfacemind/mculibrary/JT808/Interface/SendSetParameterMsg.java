package com.onfacemind.mculibrary.JT808.Interface;

/**
 * MCU 参数修改 设置
 */
public interface SendSetParameterMsg {
    public static final int Light_sensitive_resistance_signal_AD = 0x0003;
    public static final int white_light_parameters_PWM = 0x0005;
    public static final int red_light_parameters_PWM = 0x0007;
//                 * //0x0003	光敏电阻信号	DWORD[16]	16级亮度的AD值表
//                 * //0x0005	白光参数	DWORD[16]	16级亮度的PWM值表
//                 * //0x0007	红外参数	DWORD[16]	16级亮度的PWM值表
//user_defined_value = 0x000C;


    //设置光敏电阻值
    void setLight_sensitive_resistance_signal_AD(int[] ret);

    //设置白光亮度
    void setWhite_light_parameters_PWM(int[] ret);

    //设置红光亮度
    void setRed_light_parameters_PWM(int[] ret);

    //16 byte
    void setUser_defined_value(byte[] ret);
}
