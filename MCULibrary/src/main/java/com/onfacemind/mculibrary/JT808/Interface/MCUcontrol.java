package com.onfacemind.mculibrary.JT808.Interface;

import android.content.Context;

import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Common;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Notification;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_query_response;
import com.onfacemind.mculibrary.PackageData;

import io.reactivex.Observable;

/**
 * 消息种类分配中心
 */
public interface MCUcontrol {
    /**
     * mcu 控制类型
     * 消息ID: 0x8003
     *
     * @return
     */
    SendControlMsg getMCU_Control();

    /**
     * 查询类型
     *
     * @return
     */
    SendQueryMsg getMCU_query();

    /**
     * mcu参数修改 设置
     *
     * @return
     */
    SendSetParameterMsg getMCU_Set_Parameter();

    /**
     * mcu  转发IO data
     *
     * @return
     */
    SendIOMsg getMCU_IO_MSG();

    /**
     * APP 回复 MCU 应答处理
     *
     * @return
     */

    CommanRreplyMsg getAppRreply();

    //启动 MCU服务
    void startServer(Context context);

    //启动 MCU服务
    void stopServer();

    Observable<PackageData> getPackageData();

    Observable<byte[]> getMCUDatas();



    Observable<Mcu_Notification> getMCUNotification();

    Observable<Mcu_Notification> getNotification_Livings();//只需要人体感应通知

    Observable<Mcu_query_response> getMCUQuery_paramenter();

    Observable<Mcu_Common> getMcu_Common();

}
