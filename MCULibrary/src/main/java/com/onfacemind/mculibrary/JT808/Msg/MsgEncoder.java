package com.onfacemind.mculibrary.JT808.Msg;

import com.onfacemind.mculibrary.Log;

import com.onfacemind.mculibrary.JT808.util.BitOperator;
import com.onfacemind.mculibrary.JT808.util.CurrentFlowId;
import com.onfacemind.mculibrary.JT808.util.HexStringUtils;
import com.onfacemind.mculibrary.JT808.util.JT808ProtocolUtils;
import com.onfacemind.mculibrary.PackageData;
import com.onfacemind.mculibrary.JT808.vo.SendMsg.AppCommonMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MsgEncoder {
    private final String TerminalPhone = "888888888888";//模拟 12位 设备号
    public BitOperator bitOperator;
    private JT808ProtocolUtils jt808ProtocolUtils;

    public MsgEncoder() {
        this.bitOperator = new BitOperator();
        this.jt808ProtocolUtils = new JT808ProtocolUtils();
    }


    /**
     * 创建一个心跳包
     *
     * @return
     * @throws Exception
     */
    public byte[] HeartBeat() throws Exception {
        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(0, 0b000, false, 0);
        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(TerminalPhone,
                TPMSConsts.cmd_terminal_heart_beat, null, msgBodyProps, CurrentFlowId.getFlowId());
        // 连接消息头和消息体
//        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(msgHeader, 0, msgHeader.length - 1);
        // 连接并且转义
        return this.doEncode(msgHeader, checkSum);
    }

    /**
     * 控制 MCU 对应设备
     *
     * @return
     * @throws Exception
     */
    public byte[] control_Mcu(int controlCalss, byte ret) throws Exception {
        byte[] msgBodyBytes = bitOperator.integerTo2Bytes(controlCalss);//对应控制MCU类型
        byte[] byteret = new byte[]{ret};//对应控制值
        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length + byteret.length, 0b000, false, 0);
        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(TerminalPhone,
                TPMSConsts.cmd_terminal_control, null, msgBodyProps, CurrentFlowId.getFlowId());
        // 连接消息头和消息体
        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes, byteret);
        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length - 1);
        // 连接并且转义
        return this.doEncode(headerAndBody, checkSum);
    }

    /**
     * 查询 MCU参数
     *
     * @param queryCalss 查询类型
     * @return
     * @throws Exception
     */
    public byte[] Query_Parameter(int queryCalss) throws Exception {
        byte[] msgBodyBytes = bitOperator.integerTo2Bytes(queryCalss);//对应查询类型
        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length, 0b000, false, 0);
        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(TerminalPhone,
                TPMSConsts.cmd_terminal_param_query, null, msgBodyProps, CurrentFlowId.getFlowId());
        // 连接消息头和消息体
        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length - 1);
        // 连接并且转义
        return this.doEncode(headerAndBody, checkSum);
    }

    /**
     * 发送 IO 数据  端口测试
     *
     * @param IOPort 指定对应 IO端口
     * @param ret    发送值  0 或 1
     * @return
     * @throws Exception
     */
    public byte[] Send_IO_Data(int IOPort, int ret) throws Exception {
        byte[] msgBodyBytes = {(byte) IOPort, (byte) ret};
//        String s = HexStringUtils.toHexString(msgBodyBytes);
        Log.i(" 发送IO", "onSend: sendBytes 发送数据 = " + Arrays.toString(msgBodyBytes));
        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length, 0b000, false, 0);
        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(TerminalPhone,
                TPMSConsts.cmd_terminal_param_IO, null, msgBodyProps, CurrentFlowId.getFlowId());
        // 连接消息头和消息体
        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length - 1);
        // 连接并且转义
        return this.doEncode(headerAndBody, checkSum);
    }

    /**
     * RS485 协议控制    测试！！！！！！！！！！！！！
     *
     * @param queryCalss 同步码+设备地址+命令码+校验码
     *                   同步码 235（0xEB）
     *                   设备地址  0-255
     *                   命令码 GATE_CMD下
     * @return
     * @throws Exception
     */
//    public byte[] control_Parameter_RS485(int queryCalss) throws Exception {
//        byte[] msgBodyBytes = new byte[4];
//        msgBodyBytes[0] = bitOperator.integerTo1Byte(0xEB);//同步码
//        msgBodyBytes[1] = bitOperator.integerTo1Byte(0xFF);//设备地址待定
//        msgBodyBytes[2] = bitOperator.integerTo1Byte(queryCalss);//命令码
//        byte xor = msgBodyBytes[0];
//        for (int i = 1; i < msgBodyBytes.length - 1; i++) {
//            xor ^= msgBodyBytes[i];
//        }
//        msgBodyBytes[3] = xor;//校验码
//
//        String s = HexStringUtils.toHexString(msgBodyBytes);
//        Log.i(" 发送IO", "onSend: sendBytes 发送数据 = " + Arrays.toString(msgBodyBytes));
//        Log.i(" 发送IO", "onSend: sendBytes 消息体  发送数据 === " + s);
//        // 消息头
//        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length, 0b000, false, 0);
//        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(TerminalPhone,
//                0x8006, null, msgBodyProps, CurrentFlowId.getFlowId());
//        // 连接消息头和消息体
//        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes);
//
//        // 校验码
//        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length - 1);
//        // 连接并且转义
//        return this.doEncode(headerAndBody, checkSum);
//    }


    /**
     * 修改 MCU参数
     *
     * @param setCalss 修改类型
     * @param ints     对应修改值  16级
     * @return
     * @throws Exception
     */
    public byte[] set_Parameter(int setCalss, int[] ints) throws Exception {
        byte[] msgBodyBytes = bitOperator.integerTo2Bytes(setCalss);//对应修改类型
        if (ints.length != 16) throw new Exception("数据长度错误！！");
        byte[] byteret = new byte[32];
        for (int i = 0; i < ints.length; i++) {
            byte[] bytes = bitOperator.integerTo2Bytes(ints[i]);
            byteret[i * 2] = bytes[0];
            byteret[i * 2 + 1] = bytes[1];
        }
//        byte[] byteret = new byte[]{00, 01, 00, 02,
//                00, 03, 00, 04, 00, 05, 00, 06, 00, 07, 01, 01, 01, 02, 01, 03, 01, 04, 01, 05, 01, 06, 01, 07, 02, 01, 02, 02};//修改值
        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBodyBytes.length + byteret.length, 0b000, false, 0);
        byte[] msgHeader = this.jt808ProtocolUtils.generateMsgHeader(TerminalPhone,
                TPMSConsts.cmd_terminal_param_settings, null, msgBodyProps, CurrentFlowId.getFlowId());
        // 连接消息头和消息体
        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, msgBodyBytes, byteret);//顺序是否错误 ？？
//        byte[] headerAndBody = this.bitOperator.concatAll(msgHeader, byteret, msgBodyBytes);
        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length - 1);
        // 连接并且转义
        return this.doEncode(headerAndBody, checkSum);
    }

    /**
     * 创建回复MCU 通用消息
     *
     * @param packageData
     * @return
     */
    public byte[] toAppCommonMsg(PackageData packageData) throws Exception {
        PackageData.MsgHeader msgHeader = packageData.getMsgHeader();
        AppCommonMsg ret = new AppCommonMsg(msgHeader.getFlowId(), msgHeader.getMsgId(), AppCommonMsg.success);
        byte[] msgBody = this.bitOperator.concatAll(Arrays.asList(//
                bitOperator.integerTo2Bytes(ret.getReplyFlowId()), // 应答流水号
                bitOperator.integerTo2Bytes(ret.getReplyId()), // 应答ID,对应的终端消息的ID
                new byte[]{ret.getReplyCode()}// 结果
        ));
        // 消息头
        int msgBodyProps = this.jt808ProtocolUtils.generateMsgBodyProps(msgBody.length, 0b000, false, 0);
        byte[] msgHeaders = this.jt808ProtocolUtils.generateMsgHeader(msgHeader.getTerminalPhone(),
                TPMSConsts.cmd_common_resp, msgBody, msgBodyProps, CurrentFlowId.getFlowId());
        byte[] headerAndBody = this.bitOperator.concatAll(msgHeaders, msgBody);

        // 校验码
        int checkSum = this.bitOperator.getCheckSum4JT808(headerAndBody, 0, headerAndBody.length - 1);
        // 连接并且转义
        return this.doEncode(headerAndBody, checkSum);
    }


    //组合消息 和 检验码 并转义
    private byte[] doEncode(byte[] headerAndBody, int checkSum) throws Exception {
        byte[] noEscapedBytes = this.bitOperator.concatAll(Arrays.asList(//
                new byte[]{TPMSConsts.pkg_delimiter}, // 0x7e
                headerAndBody, // 消息头+ 消息体
                bitOperator.integerTo1Bytes(checkSum), // 校验码
                new byte[]{TPMSConsts.pkg_delimiter}// 0x7e
        ));
        // 转义
        return jt808ProtocolUtils.doEscape4Send(noEscapedBytes, 1, noEscapedBytes.length - 1);//转义到7e前 包括校验码也需要转义
    }


}
