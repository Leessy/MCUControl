package com.onfacemind.mculibrary.JT808.vo.SendMsg;

/**
 * Created by 刘承. on 2018/3/3.
 * App 回复MCU 通用消息
 */

public class AppCommonMsg {

    public static final byte success = 0;
    public static final byte failure = 1;
    public static final byte msg_error = 2;
    public static final byte unsupported = 3;

    // byte[0-1] 应答流水号 对应的终端消息的流水号
    private int replyFlowId;
    // byte[2-3] 应答ID 对应的终端消息的ID
    private int replyId;
    /**
     * 0：成功∕确认<br>
     * 1：失败<br>
     * 2：消息有误<br>
     * 3：不支持<br>
     */
    private byte replyCode;

    public AppCommonMsg(int replyFlowId, int replyId, byte replyCode) {
        this.replyFlowId = replyFlowId;
        this.replyId = replyId;
        this.replyCode = replyCode;
    }

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
}
