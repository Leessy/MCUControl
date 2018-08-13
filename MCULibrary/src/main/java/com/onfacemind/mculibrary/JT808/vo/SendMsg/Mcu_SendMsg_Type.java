package com.onfacemind.mculibrary.JT808.vo.SendMsg;

/**
 * Created by 刘承. on 2018/3/3.
 * 发送消息到 Mcu 工厂类
 */

public class Mcu_SendMsg_Type {

    //控制mcu
    public static class Controls {
        /**
         * 0	类型	WORD	控制消息类型 （见表1）
         * 2	数据	BYTE	控制消息数据 （见表1）
         * 表1：
         * 类型	功能		数据值与描述
         * 0x0001	白光开关	1：开启 0：关闭
         * 0x0002	红外开关	1：开启 0：关闭
         * 0x0003	继电器吸合间隔	1-99，小于1时为1，大于99时为99
         * 0x0004	按键灯控制	0：熄灭 1：常量 2：快闪 3：慢闪
         * <p>
         * 0x0005	蜂鸣器控制	0：关闭 1：常响 2：快响 3：慢响
         * 0x0006	APP心跳检测开关	1：开启 0：关闭
         */
        public static final int White_light = 0x0001;
        public static final int red_light = 0x0002;
        public static final int Relay_absorption_interval = 0x0003;
        public static final int Buttun_LED = 0x0004;
        public static final int HUMMER = 0x0005;
        public static final int heartbeat_switch = 0x0006;
    }

    //查询参数
    public static class MCU_QueryParameter {
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
         * <p>
         * 0x000C	查询自定义值	16字节二进制值
         * <p>
         * 0x000F	查询MCU 唯一ID	MCU CDID(96Bit Unique ID)
         */

        public static final int Body_infrared_signal = 0x0001;
        public static final int Light_sensitive_resistance_signal = 0x0002;
        public static final int Light_sensitive_resistance_signal_AD = 0x0003;
        public static final int white_light_parameters = 0x0004;
        public static final int white_light_parameters_PWM = 0x0005;
        public static final int red_light_parameters = 0x0006;
        public static final int red_light_parameters_PWM = 0x0007;
        public static final int Gate_magnetic_sensor_signal = 0x0008;
        public static final int Antiknock_sensor_signal = 0x0009;
        public static final int VersionCode = 0x000A;
        public static final int Light_sensitive_ret = 0x000B;
        public static final int user_defined_value = 0x000C;
        public static final int mcu_id = 0x000F;

    }

    //MCU通知类型
    public static class MCU_Notification {
        /**
         * 类型	功能		数据值与描述
         * 0x0001	红外 触发	0：触发通知（下降沿）1：撤除通知（上升沿）
         * 0x0002	门磁传感 触发	0：下升沿通知（常开型门磁的关门事件）
         * 1：上升沿通知（常开型门磁的开门事件）
         * 0x0003	震动传感器信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
         * 0x0004	紧急按钮信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
         * 0x0005	防拆传感器 触发	0：解除通知（稳定3秒以上的低电平）
         * 1：拆离事件触发通知（上升沿）
         */
        public static final int infrared_reaction = 0x0001;
        public static final int fluxgate_sensor = 0x0002;
        public static final int Shock_Sensor = 0x0003;
        public static final int urgency_signal = 0x0004;
        public static final int dismantle_Sensor = 0x0005;

    }

    //MCU 设置修改参数
    public static class MCU_set_Parameter {
        /**
         * //    类型	功能	数据长度	描述
         * //0x0003	光敏电阻信号	DWORD[16]	16级亮度的AD值表
         * //0x0005	白光参数	DWORD[16]	16级亮度的PWM值表
         * //0x0007	红外参数	DWORD[16]	16级亮度的PWM值表
         * 0x000C	设置自定义值	16字节二进制值
         */
        public static final int Light_sensitive_resistance_signal_AD = 0x0003;
        public static final int white_light_parameters_PWM = 0x0005;
        public static final int red_light_parameters_PWM = 0x0007;
        public static final int user_defined_value = 0x000C;
    }


    public static class GATE_CMD {
        public static final int open_left = 0x40;//向左开闸
        public static final int open_right = 0x41;//向右开闸
        public static final int drop_down = 0x42;//落杆
        public static final int nose_up = 0x43;//升杆

    }


}
