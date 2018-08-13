package com.onfacemind.mculibrary.JT808.Msg;

import com.onfacemind.mculibrary.Log;

import com.onfacemind.mculibrary.JT808.util.BCD8421Operater;
import com.onfacemind.mculibrary.JT808.util.BitOperator;
import com.onfacemind.mculibrary.JT808.util.JT808ProtocolUtils;
import com.onfacemind.mculibrary.PackageData;
import com.onfacemind.mculibrary.PackageData.MsgHeader;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Common;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_Notification;
import com.onfacemind.mculibrary.JT808.vo.Response.Mcu_query_response;

import java.util.Arrays;

public class MsgDecoder {
    private static final String TAG = "MsgDecoder";
//	private static final Logger log = LoggerFactory.getLogger(MsgDecoder.class);

    private BitOperator bitOperator;
    private BCD8421Operater bcd8421Operater;

    public MsgDecoder() {
        this.bitOperator = new BitOperator();
        this.bcd8421Operater = new BCD8421Operater();
    }

    public PackageData bytes2PackageData(byte[] data) {
        PackageData ret = new PackageData();

        // 0. 终端套接字地址信息
        // ret.setChannel(msg.getChannel());

        // 1. 16byte 或 12byte 消息头
        MsgHeader msgHeader = this.parseMsgHeaderFromBytes(data);
        ret.setMsgHeader(msgHeader);
        Log.d(TAG, "bytes2PackageData: 解析头=" + msgHeader);

        int msgBodyByteStartIndex = 12;
        // 2. 消息体
        // 有子包信息,消息体起始字节后移四个字节:消息包总数(word(16))+包序号(word(16))
        if (msgHeader.isHasSubPackage()) {
            msgBodyByteStartIndex = 16;
        }

        byte[] tmp = new byte[msgHeader.getMsgBodyLength()];//创建消息头中指定的长度的消息体byte[]
        System.arraycopy(data, msgBodyByteStartIndex, tmp, 0, tmp.length);
        ret.setMsgBodyBytes(tmp);

        // 3. 去掉分隔符之后，最后一位就是校验码
        // int checkSumInPkg =
//         this.bitOperator.oneByteToInteger(data[data.length - 1]);
        int checkSumInPkg = data[data.length - 1];
        // TODO: 2018/5/26 此处进行校验码判断，data最后一为校验码
//        int calculatedCheckSum = this.bitOperator.getCheckSum4JT808(data, 0, data.length - 1);
        int calculatedCheckSum = this.bitOperator.getDecoderCheckSum4JT808(data);//解析接收数据 检验码
        ret.setCheckSum(checkSumInPkg);
        if (checkSumInPkg != calculatedCheckSum) {
            Log.e(TAG, "bytes2PackageData: " + "检验码不一致,msgid:" + msgHeader.getMsgId() + "pkg:" + checkSumInPkg + "calculated:" + calculatedCheckSum);
        }
        return ret;
    }

    /**
     * 解析出 消息头信息
     *
     * @param data
     * @return
     */
    private MsgHeader parseMsgHeaderFromBytes(byte[] data) {
        MsgHeader msgHeader = new MsgHeader();

        // 1. 消息ID word(16)
        // byte[] tmp = new byte[2];
        // System.arraycopy(data, 0, tmp, 0, 2);
        // msgHeader.setMsgId(this.bitOperator.twoBytesToInteger(tmp));
        msgHeader.setMsgId(this.parseIntFromBytes(data, 0, 2));// TODO: 2018/5/29 解析ID 需注意高低顺序 改动！！

        // 2. 消息体属性 word(16)=================>
        // System.arraycopy(data, 2, tmp, 0, 2);
        // int msgBodyProps = this.bitOperator.twoBytesToInteger(tmp);
        int msgBodyProps = this.parseIntFromBytes(data, 2, 2);
        msgHeader.setMsgBodyPropsField(msgBodyProps);
        // [ 0-9 ] 0000,0011,1111,1111(3FF)(消息体长度)
        msgHeader.setMsgBodyLength(msgBodyProps & 0x3ff);
        // [10-12] 0001,1100,0000,0000(1C00)(加密类型)
        msgHeader.setEncryptionType((msgBodyProps & 0x1c00) >> 10);
        // [ 13_ ] 0010,0000,0000,0000(2000)(是否有子包)
        msgHeader.setHasSubPackage(((msgBodyProps & 0x2000) >> 13) == 1);
        // [14-15] 1100,0000,0000,0000(C000)(保留位)
        msgHeader.setReservedBit(((msgBodyProps & 0xc000) >> 14) + "");
        // 消息体属性 word(16)<=================

        // 3. 终端手机号 bcd[6]
        // tmp = new byte[6];
        // System.arraycopy(data, 4, tmp, 0, 6);
        // msgHeader.setTerminalPhone(this.bcd8421Operater.bcd2String(tmp));
        msgHeader.setTerminalPhone(this.parseBcdStringFromBytes(data, 4, 6));

        // 4. 消息流水号 word(16) 按发送顺序从 0 开始循环累加
        // tmp = new byte[2];
        // System.arraycopy(data, 10, tmp, 0, 2);
        // msgHeader.setFlowId(this.bitOperator.twoBytesToInteger(tmp));
        msgHeader.setFlowId(this.parseIntFromBytes(data, 10, 2));

        // 5. 消息包封装项
        // 有子包信息
        if (msgHeader.isHasSubPackage()) {
            // 消息包封装项字段
            msgHeader.setPackageInfoField(this.parseIntFromBytes(data, 12, 4));
            // byte[0-1] 消息包总数(word(16))
            // tmp = new byte[2];
            // System.arraycopy(data, 12, tmp, 0, 2);
            // msgHeader.setTotalSubPackage(this.bitOperator.twoBytesToInteger(tmp));
            msgHeader.setTotalSubPackage(this.parseIntFromBytes(data, 12, 2));

            // byte[2-3] 包序号(word(16)) 从 1 开始
            // tmp = new byte[2];
            // System.arraycopy(data, 14, tmp, 0, 2);
            // msgHeader.setSubPackageSeq(this.bitOperator.twoBytesToInteger(tmp));
            msgHeader.setSubPackageSeq(this.parseIntFromBytes(data, 12, 2));
        }
        return msgHeader;
    }

    protected String parseStringFromBytes(byte[] data, int startIndex, int lenth) {
        return this.parseStringFromBytes(data, startIndex, lenth, null);
    }

    private String parseStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
        try {
            byte[] tmp = new byte[lenth];
            System.arraycopy(data, startIndex, tmp, 0, lenth);
            return new String(tmp, TPMSConsts.string_charset);
        } catch (Exception e) {
//            log.error("解析字符串出错:{}", e.getMessage());
            Log.d(TAG, "parseStringFromBytes: 解析字符串出错:" + e.getMessage());
            e.printStackTrace();
            return defaultVal;
        }
    }

    private String parseBcdStringFromBytes(byte[] data, int startIndex, int lenth) {
        return this.parseBcdStringFromBytes(data, startIndex, lenth, null);
    }

    private String parseBcdStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
        try {
            byte[] tmp = new byte[lenth];
            System.arraycopy(data, startIndex, tmp, 0, lenth);
            return this.bcd8421Operater.bcd2String(tmp);
        } catch (Exception e) {
//            log.error("解析BCD(8421码)出错:{}", e.getMessage());
            Log.d(TAG, "parseBcdStringFromBytes: 解析BCD(8421码)出错:" + e.getMessage());
            e.printStackTrace();
            return defaultVal;
        }
    }

    public int parseIntFromBytes(byte[] data, int startIndex, int length) {
        return this.parseIntFromBytes(data, startIndex, length, 0);
    }

    private int parseIntFromBytes(byte[] data, int startIndex, int length, int defaultVal) {
        try {
            // 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
            final int len = length > 4 ? 4 : length;
            byte[] tmp = new byte[len];
            System.arraycopy(data, startIndex, tmp, 0, len);
            return bitOperator.byteToInteger(tmp);
        } catch (Exception e) {
//            log.error("解析整数出错:{}", e.getMessage());
            Log.d(TAG, "parseIntFromBytes: 解析整数出错:" + e.getMessage());
            e.printStackTrace();
            return defaultVal;
        }
    }

//    public TerminalRegisterMsg toTerminalRegisterMsg(PackageData packageData) {
//        TerminalRegisterMsg ret = new TerminalRegisterMsg(packageData);
//        byte[] data = ret.getMsgBodyBytes();
//
//        TerminalRegInfo body = new TerminalRegInfo();
//
//        // 1. byte[0-1] 省域ID(WORD)
//        // 设备安装车辆所在的省域，省域ID采用GB/T2260中规定的行政区划代码6位中前两位
//        // 0保留，由平台取默认值
//        body.setProvinceId(this.parseIntFromBytes(data, 0, 2));
//
//        // 2. byte[2-3] 设备安装车辆所在的市域或县域,市县域ID采用GB/T2260中规定的行 政区划代码6位中后四位
//        // 0保留，由平台取默认值
//        body.setCityId(this.parseIntFromBytes(data, 2, 2));
//
//        // 3. byte[4-8] 制造商ID(BYTE[5]) 5 个字节，终端制造商编码
//        // byte[] tmp = new byte[5];
//        body.setManufacturerId(this.parseStringFromBytes(data, 4, 5));
//
//        // 4. byte[9-16] 终端型号(BYTE[8]) 八个字节， 此终端型号 由制造商自行定义 位数不足八位的，补空格。
//        body.setTerminalType(this.parseStringFromBytes(data, 9, 8));
//
//        // 5. byte[17-23] 终端ID(BYTE[7]) 七个字节， 由大写字母 和数字组成， 此终端 ID由制造 商自行定义
//        body.setTerminalId(this.parseStringFromBytes(data, 17, 7));
//
//        // 6. byte[24] 车牌颜色(BYTE) 车牌颜 色按照JT/T415-2006 中5.4.12 的规定
//        body.setLicensePlateColor(this.parseIntFromBytes(data, 24, 1));
//
//        // 7. byte[25-x] 车牌(STRING) 公安交 通管理部门颁 发的机动车号牌
//        body.setLicensePlate(this.parseStringFromBytes(data, 25, data.length - 25));
//
//        ret.setTerminalRegInfo(body);
//        return ret;
//    }
//
//

    /**
     * 转换成mcu通用应答实体类
     *
     * @param packageData
     * @return
     */
    public Mcu_Common toMcu_Common(PackageData packageData) {
        byte[] msgBodyBytes = packageData.getMsgBodyBytes();
        Mcu_Common mcu_common = new Mcu_Common();
        // 1. byte[0-2] 应答流水号(DWORD)
        mcu_common.setReplyFlowId(this.parseIntFromBytes(msgBodyBytes, 0, 2));
        // 2. byte[2-4] 应答ID(DWORD)
        mcu_common.setReplyId(this.parseIntFromBytes(msgBodyBytes, 2, 2));
        // 3. byte[4] 应答结果(BYTE)
        mcu_common.setReplyCode(msgBodyBytes[4]);

//        Log.d(TAG, "toMcu_Common: 解析心跳状态" + mcu_common.getReplyId());
//        Log.d(TAG, "toMcu_Common: 解析心跳状态" + msgBodyBytes.length);
        /***新增心跳通用应答 第5位*/
        if (mcu_common.getReplyId() == TPMSConsts.cmd_terminal_heart_beat
                && msgBodyBytes.length == 6) {
            mcu_common.heartbeatStatus = msgBodyBytes[5];
        }
        return mcu_common;
    }

    /**
     * 转换成mcu  消息通知实体类
     *
     * @param packageData
     * @return
     */
    public Mcu_Notification toMcu_Notification(PackageData packageData) {
        byte[] msgBodyBytes = packageData.getMsgBodyBytes();
        Mcu_Notification mcu_notification = new Mcu_Notification();
        // 1. byte[0-2] 通知类型(DWORD)
        mcu_notification.setNotification_type(this.parseIntFromBytes(msgBodyBytes, 0, 2));
        // 2. byte[2]  通知 值(BYTE)
        mcu_notification.setRet(msgBodyBytes[2]);
        return mcu_notification;
    }

    /**
     * 转换成mcu  回复查询 实体类
     *
     * @param packageData
     * @return
     */
    public Mcu_query_response toMcu_QueryResponse(PackageData packageData) {
        byte[] msgBodyBytes = packageData.getMsgBodyBytes();
        Mcu_query_response mcu_query_response = new Mcu_query_response();
        if (msgBodyBytes.length < 2) return mcu_query_response;

        // 1. byte[0-2] 查询类型(DWORD)
        mcu_query_response.setQuery_type(this.parseIntFromBytes(msgBodyBytes, 0, 2));
        switch (mcu_query_response.getQuery_type()) {
            case 0x0001:
            case 0x0002:
            case 0x0004:
            case 0x0006:
            case 0x0008:
            case 0x0009:
                mcu_query_response.setRet(msgBodyBytes[2]);//1 byte
                break;
            case 0x000A:
            case 0x000B:
                //2 byte
                mcu_query_response.setRet(parseIntFromBytes(msgBodyBytes, 2, 2));//1 byte
                break;
            case 0x0003:
            case 0x0005:
            case 0x0007:
                if (msgBodyBytes.length == 34) {
                    for (int i = 2; i < msgBodyBytes.length; i += 2) {
                        mcu_query_response.getLevels().add(this.parseIntFromBytes(msgBodyBytes, i, 2));//16级表值
                    }
                } else {
                    Log.d(TAG, "toMcu_QueryResponse: 亮度值 长度错误！！！！");
                }
                break;
            case 0x000C:
                mcu_query_response.user_defined_value = new byte[16];
                if (msgBodyBytes.length == 18) {
                    for (int i = 0; i < mcu_query_response.user_defined_value.length; i++) {
                        mcu_query_response.user_defined_value[i] = msgBodyBytes[i + 2];
                    }
                } else {
                    Log.d(TAG, "toMcu_QueryResponse: 亮度值 长度错误！！！！");
                }
                break;
            case 0x000F:
                mcu_query_response.mcu_id = new byte[12];
                if (msgBodyBytes.length == 14) {
                    for (int i = 0; i < mcu_query_response.mcu_id.length; i++) {
                        mcu_query_response.mcu_id[i] = msgBodyBytes[i + 2];
                    }
                } else {
                    Log.d(TAG, "toMcu_QueryResponse: 亮度值 长度错误！！！！");
                }
                break;
            default:
        }
        Log.d(TAG, "toMcu_QueryResponse！！！！==" + Arrays.toString(msgBodyBytes));
        return mcu_query_response;
    }


    private float parseFloatFromBytes(byte[] data, int startIndex, int length) {
        return this.parseFloatFromBytes(data, startIndex, length, 0f);
    }

    private float parseFloatFromBytes(byte[] data, int startIndex, int length, float defaultVal) {
        try {
            // 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
            final int len = length > 4 ? 4 : length;
            byte[] tmp = new byte[len];
            System.arraycopy(data, startIndex, tmp, 0, len);
            return bitOperator.byte2Float(tmp);
        } catch (Exception e) {
//            log.error("解析浮点数出错:{}", e.getMessage());
            Log.d(TAG, "parseFloatFromBytes: 解析浮点数出错:" + e.getMessage());
            e.printStackTrace();
            return defaultVal;
        }
    }
}
