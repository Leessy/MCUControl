package com.onfacemind.mculibrary.JT808.vo.Response;


/**
 * Created by 刘承. on 2018/3/3.
 * MCU 通知消息实体类
 */

public class Mcu_Notification {
    /**
     * 0x0001	红外 触发	0：触发通知（下降沿）1：撤除通知（上升沿）
     * 0x0002	门磁传感 触发	0：下升沿通知（常开型门磁的关门事件）
     *                      1：上升沿通知（常开型门磁的开门事件）
     * 0x0003	震动传感器信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
     * 0x0004	紧急按钮信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
     * 0x0005	防拆传感器 触发	0：解除通知（稳定3秒以上的低电平）
     *                          1：拆离事件触发通知（上升沿）
     */
    //MCU通知类型
    public int Notification_type;
    //MCU通知 值
    public byte ret;

    public Mcu_Notification() {
    }


    public int getNotification_type() {
        return Notification_type;
    }

    public void setNotification_type(int notification_type) {
        Notification_type = notification_type;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(byte ret) {
        this.ret = ret;
    }
}
