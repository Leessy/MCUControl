package com.onfacemind.mculibrary.JT808.Interface;

/**
 * 发送MCU控制信息
 */
public interface SendControlMsg {
    /**
     * 0	类型	WORD	控制消息类型 （见表1）
     * 2	数据	BYTE	控制消息数据 （见表1）
     * 表1：
     * 类型	功能		数据值与描述
     * 0x0001	白光开关	1：开启 0：关闭
     * 0x0002	红外开关	1：开启 0：关闭
     * 0x0003	继电器吸合间隔	1-99，小于1时为1，大于99时为99
     * 0x0004	按键灯控制	0：熄灭 1：常量 2：快闪 3：慢闪
     */
    public static final int White_light = 0x0001;
    public static final int red_light = 0x0002;
    public static final int Relay_absorption_interval = 0x0003;
    public static final int Buttun_LED = 0x0004;

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
}
