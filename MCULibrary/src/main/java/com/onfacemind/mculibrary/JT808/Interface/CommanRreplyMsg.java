package com.onfacemind.mculibrary.JT808.Interface;

import com.onfacemind.mculibrary.PackageData;

/**
 * APP 通用 操作
 * 回复 MCU 数据
 */
public interface CommanRreplyMsg {
    //回复通用应答
    void CommanRreply(PackageData packageData);

    //回复通用应答
    void Heartbeat();

}
