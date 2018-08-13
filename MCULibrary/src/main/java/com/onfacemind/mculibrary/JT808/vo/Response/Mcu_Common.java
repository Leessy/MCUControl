package com.onfacemind.mculibrary.JT808.vo.Response;


/**
 * Created by 刘承. on 2018/3/3.
 * Mcu 通用应答消息
 */

public class Mcu_Common {

    /**
     * 0	应答流水号	WORD	对应的APP消息的流水号
     * 2	应答ID	WORD	对应的APP消息的ID
     * 4	结果	BYTE	0：成功/确认；1：失败；2：消息有误；3：不支持
     */
    private byte replyCode;
    // byte[0-1] 应答流水号 对应的终端消息的流水号
    private int replyFlowId;
    // byte[2-3] 应答ID 对应的终端消息的ID
    private int replyId;
    public int heartbeatStatus;


    public int getReplyFlowId() {
        return replyFlowId;
    }

    public void setReplyFlowId(int replyFlowId) {
        this.replyFlowId = replyFlowId;
    }

    public int getReplyId() {
        return replyId;
    }

    public void setReplyId(int replyId) {
        this.replyId = replyId;
    }

    public byte getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(byte replyCode) {
        this.replyCode = replyCode;
    }

    @Override
    public String toString() {
        return "Mcu_Common{" +
                "replyCode=" + replyCode +
                ", replyFlowId=" + replyFlowId +
                ", replyId=" + replyId +
                '}';
    }
}
