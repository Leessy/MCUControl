package com.onfacemind.mculibrary.JT808.util;

/**
 * Created by 刘承. on 2018/3/3.
 */

public class CurrentFlowId {
    private static int currentFlowId = 0;

    /**
     * 创建消息流水号 word(16) 按发送顺序从 0 开始循环累加
     *
     * @return
     */
    synchronized public static int getFlowId() {
        if (currentFlowId >= 0xffff)
            currentFlowId = 0;
        return currentFlowId++;
    }

}
