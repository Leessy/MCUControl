package com.onfacemind.mculibrary.JT808.Interface;

/**
 * 发送 查询信息
 */
public interface SendQueryMsg {
    /**
     * 0x0001	人体红外信号	低电平代表有信号 高电平代表没信号
     * 0x0002	光敏电阻信号	环境照度等级（0-15级）
     * 0x0003	光敏电阻信号	16级亮度的AD值表
     * 0x0004	白光参数	亮度等级
     * 0x0005	白光参数	16级亮度的PWM值表
     * 0x0006	红外参数	亮度等级
     * 0x0007	红外参数	16级亮度的PWM值表
     * 0x0008	门磁传感器信号	状态查询（常开型门磁低电平为门关闭、高电平为门开启）
     * 0x0009	防拆传感器信号	状态查询（低电平为安装状态，高电平为拆离状态）
     * 0x000A	查询固件版本序号	（版本序号0为Debug，1~65535）
     * 0x000B	光敏电阻当前值	环境照度AD值
     */

//    public static final int Body_infrared_signal = 0x0001;
//    public static final int Light_sensitive_resistance_signal = 0x0002;
//    public static final int Light_sensitive_resistance_signal_AD = 0x0003;
//    public static final int white_light_parameters = 0x0004;
//    public static final int white_light_parameters_PWM = 0x0005;
//    public static final int red_light_parameters = 0x0006;
//    public static final int red_light_parameters_PWM = 0x0007;
//    public static final int Gate_magnetic_sensor_signal = 0x0008;
//    public static final int Antiknock_sensor_signal = 0x0009;
//    public static final int VersionCode = 0x000A;
//    public static final int Light_sensitive_ret = 0x000B;
//    public static final int user_defined_value = 0x000C;
//    public static final int mcu_id = 0x000F;

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

}
