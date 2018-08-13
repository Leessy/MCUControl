package com.onfacemind.mculibrary.JT808.vo.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 刘承. on 2018/3/3.
 * MCU 返回查询参数应答
 */

public class Mcu_query_response {
    /**
     * 起始字节	字段	数据类型	描述及要求
     0	类型	WORD	查询应答消息类型 （见表4）
     2	数据	BYTE[N]	（见表4）
     */

    /**
     * 0x0001	人体红外信号	BYTE	0：低电平代表有信号 1：高电平代表没信号
     * 0x0002	光敏电阻信号	BYTE	环境照度等级 0-15
     * 0x0003	光敏电阻信号	DWORD[16]	16级亮度的AD值表
     * 0x0004	白光参数	BYTE	亮度等级 0-15
     * 0x0005	白光参数	DWORD[16]	16级亮度的PWM值表
     * 0x0006	红外参数	BYTE	亮度等级 0-15
     * 0x0007	红外参数	DWORD[16]	16级亮度的PWM值表
     * 0x0008	门磁传感器信号	BYTE	0:常开型门磁低电平为门关闭 1:高电平为门开启
     * 0x0009	防拆传感器信号	BYTE	0:低电平为安装状态 1:高电平为拆离状态
     * 0x000C	查询自定义值	BYTE[16]	16字节二进制值
     * <p>
     * 0x000F	查询MCU 唯一ID	BYTE[12]	MCU CDID(96Bit Unique ID)
     */

    //MCU通知类型
    public int Query_type;
    //MCU通知 值
    public int ret;
    //16级表
    List<Integer> Levels = new ArrayList<>();

    public byte[] user_defined_value;

    public byte[] mcu_id;

    public List<Integer> getLevels() {
        return Levels;
    }

    public void setLevels(List<Integer> levels) {
        Levels = levels;
    }

    public int getQuery_type() {
        return Query_type;
    }

    public void setQuery_type(int query_type) {
        Query_type = query_type;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    @Override
    public String toString() {
        return "Mcu_query_response{" +
                "Query_type=" + Query_type +
                ", ret=" + ret +
                '}';
    }
}
