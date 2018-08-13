package com.onfacemind.mculibrary.JT808.Interface;

import com.onfacemind.mculibrary.BuildConfig;
import com.onfacemind.mculibrary.JT808.Msg.MsgDecoder;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Common;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Notification;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_query_response;
import com.onfacemind.mculibrary.JT808.vo.SendMsg.Mcu_SendMsg_Type;
import com.onfacemind.mculibrary.Log;
import com.onfacemind.mculibrary.PackageData;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

/**
 * 信息类型转换口
 */
public abstract class MCUbase implements MCUcontrol, SendControlMsg, SendQueryMsg, SendIOMsg, SendSetParameterMsg, CommanRreplyMsg {
    MsgDecoder msgDecoder;
    protected PublishSubject<Mcu_Notification> notificationPublishSubject = PublishSubject.create();
    protected PublishSubject<Mcu_query_response> query_responsePublishSubject = PublishSubject.create();
    protected PublishSubject<Mcu_Common> mcu_commonPublishSubject = PublishSubject.create();

    public MCUbase() {
        msgDecoder = new MsgDecoder();
    }

    @Override
    public SendControlMsg getMCU_Control() {
        return this;
    }

    @Override
    public SendQueryMsg getMCU_query() {
        return this;
    }

    @Override
    public SendSetParameterMsg getMCU_Set_Parameter() {
        return this;
    }

    @Override
    public SendIOMsg getMCU_IO_MSG() {
        return this;
    }

    @Override
    public CommanRreplyMsg getAppRreply() {
        return this;
    }


    @Override
    public Observable<Mcu_Notification> getMCUNotification() {
        return notificationPublishSubject;
    }

    @Override
    public Observable<Mcu_query_response> getMCUQuery_paramenter() {
        return query_responsePublishSubject;
    }

    @Override
    public Observable<Mcu_Common> getMcu_Common() {
        return mcu_commonPublishSubject;
    }

    @Override
    public Observable<Mcu_Notification> getNotification_Livings() {
        return notificationPublishSubject.filter(new Predicate<Mcu_Notification>() {
            @Override
            public boolean test(Mcu_Notification mcu_notification) throws Exception {
                return mcu_notification.Notification_type == Mcu_SendMsg_Type.MCU_Notification.infrared_reaction
                        && mcu_notification.getRet() == 0;
            }
        }).throttleFirst(2, TimeUnit.SECONDS);
    }

    /**
     * 0	应答流水号	WORD	对应的MCU消息的流水号
     * 2	应答ID	WORD	对应的MCU消息的ID
     * 4	结果	BYTE	0：成功/确认；1：失败；2：消息有误； 3：不支持；
     */
    //接收通用应答消息
    protected void Receive_common_respData(PackageData packageData) {
        Mcu_Common mcu_common = msgDecoder.toMcu_Common(packageData);
        if (mcu_common == null) return;
        mcu_commonPublishSubject.onNext(mcu_common);
        //是否打印
        if (!BuildConfig.DEBUG) return;
        int replyFlowId = mcu_common.getReplyFlowId();
        byte replyCode = mcu_common.getReplyCode();
        int replyId = mcu_common.getReplyId();
//        int flow = msgDecoder.parseIntFromBytes(bytes, 0, 2);//应答流水号
//        int id = msgDecoder.parseIntFromBytes(bytes, 2, 2);//应答ID
//        int ret = msgDecoder.parseIntFromBytes(bytes, 4, 1);//应答结果
        Log.d("MCU_Common_MSG 通用应答: ", "flow=" + replyFlowId + "  id=" + replyId + "   ret=" + replyCode);

    }


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
    //接收 MCU通知消息
    protected void Receive_notification_Data(PackageData packageData) {
        Mcu_Notification mcu_notification = msgDecoder.toMcu_Notification(packageData);
        if (mcu_notification == null) return;
        notificationPublishSubject.onNext(mcu_notification);
        //是否打印
        if (!BuildConfig.DEBUG) return;
        int type = mcu_notification.getNotification_type();
        int rets = mcu_notification.getRet();//通知结果
        switch (type) {
            case Mcu_SendMsg_Type.MCU_Notification.infrared_reaction://人体红外信号	0：触发通知（下降沿）1：撤除通知（上升沿）
                Log.d("MCU_Common_MSG 通知消息", "type---人体红外信号    结果=" + rets);
                break;
            case Mcu_SendMsg_Type.MCU_Notification.fluxgate_sensor://门磁传感 触发	0：下升沿通知（常开型门磁的关门事件）1：上升沿通知（常开型门磁的开门事件）
                Log.d("MCU_Common_MSG 通知消息", "type---门磁传感触发    结果=" + rets);
                break;
            case Mcu_SendMsg_Type.MCU_Notification.Shock_Sensor://震动传感器信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
                Log.d("MCU_Common_MSG 通知消息", "type---震动传感器信号    结果=" + rets);
                break;
            case Mcu_SendMsg_Type.MCU_Notification.urgency_signal://紧急按钮信号	数据无，用0x0填充;通知上报周期3秒，直到信号解除
                Log.d("MCU_Common_MSG 通知消息", "type---紧急按钮信号    结果=" + rets);
                break;
            case Mcu_SendMsg_Type.MCU_Notification.dismantle_Sensor://防拆传感器 触发	0：解除通知（稳定3秒以上的低电平）
                Log.d("MCU_Common_MSG 通知消息", "type---防拆传感器    结果=" + rets);
                break;
        }
    }


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
     * <p>
     * 0x000A	查询固件版本序号	（版本序号0为Debug，1~65535） 2 byte
     * 0x000B	光敏电阻当前值	环境照度AD值 2 byte
     */
    //接收 MCU查询结果
    protected void Receive_query_Data(PackageData packageData) {
        Mcu_query_response mcu_query_response = msgDecoder.toMcu_QueryResponse(packageData);
        if (mcu_query_response == null) return;
        query_responsePublishSubject.onNext(mcu_query_response);
        //是否打印
        if (!BuildConfig.DEBUG) return;
        int type = mcu_query_response.getQuery_type();//查询类型
//        int rets = mcu_query_response.getRet();//查询结果
        switch (type) {
            case Mcu_SendMsg_Type.MCU_QueryParameter.Body_infrared_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---人体红外信号    结果=" + mcu_query_response.ret);
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_resistance_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---光敏电阻信号等级    结果=" + mcu_query_response.ret);
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_resistance_signal_AD://DWORD[16]	16级亮度的AD值表
                Log.d("MCU_Common_MSG 查询结果", "type---光敏电阻信号    16级亮度=" + mcu_query_response.getLevels().toString());
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.white_light_parameters:
                Log.d("MCU_Common_MSG 查询结果", "type---白光参数亮度    结果=" + mcu_query_response.ret);
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.white_light_parameters_PWM://DWORD[16]	16级亮度的PWM值表
                Log.d("MCU_Common_MSG 查询结果", "type---白光参数      16级亮度=" + mcu_query_response.getLevels().toString());
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.red_light_parameters://BYTE	亮度等级 0-15
                Log.d("MCU_Common_MSG 查询结果", "type---红光参数亮度    结果=" + mcu_query_response.ret);
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.red_light_parameters_PWM://DWORD[16]	16级亮度的PWM值表
                Log.d("MCU_Common_MSG 查询结果", "type---红光参数      16级亮度=" + mcu_query_response.getLevels().toString());
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Gate_magnetic_sensor_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---门磁传感器信号    结果=" + mcu_query_response.ret);
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Antiknock_sensor_signal:
                Log.d("MCU_Common_MSG 查询结果", "type---防拆传感器信号    结果=" + mcu_query_response.ret);
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.VersionCode:
                Log.d("MCU_Common_MSG 查询结果", "type---MCU版本    结果=" + mcu_query_response.ret);
                break;
            case Mcu_SendMsg_Type.MCU_QueryParameter.Light_sensitive_ret:
                Log.d("MCU_Common_MSG 查询结果", "type---光敏电阻值    结果=" + mcu_query_response.ret);
                break;
        }
    }

}
