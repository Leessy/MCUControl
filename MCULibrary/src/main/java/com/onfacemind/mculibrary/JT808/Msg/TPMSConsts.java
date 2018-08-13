package com.onfacemind.mculibrary.JT808.Msg;

import java.nio.charset.Charset;

public class TPMSConsts {

    public static final String string_encoding = "GBK";

    public static final Charset string_charset = Charset.forName(string_encoding);
    // 标识位
    public static final int pkg_delimiter = 0x7e;

    // 客户端发呆15分钟后,服务器主动断开连接
    public static int tcp_client_idle_minutes = 30;


    // MCU通用应答
    public static final int msg_id_terminal_common_resp = 0x0001;
    // MCU通知消息
    public static final int msg_id_terminal_notification = 0x0002;
    // MCU查询应答
    public static final int msg_id_terminal_query = 0x0004;


    // APP通用应答
    public static final int cmd_common_resp = 0x8001;
    // APP心跳包
    public static final int cmd_terminal_heart_beat = 0x8002;
    // APP控制MCU设备开关
    public static final int cmd_terminal_control = 0x8003;
    // APP查询MCU设备参数信息
    public static final int cmd_terminal_param_query = 0x8004;
    // APP设置MCU参数
    public static final int cmd_terminal_param_settings = 0X8005;
    // APP 发送io 数据 1-6个io口
    public static final int cmd_terminal_param_IO = 0X8006;


    //
    public static final int cmd_terminal_ = 0X8006;


}
