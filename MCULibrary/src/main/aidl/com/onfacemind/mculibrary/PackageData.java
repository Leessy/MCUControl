package com.onfacemind.mculibrary;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.List;

//序列化数据
public class PackageData implements Parcelable {

    /**
     * 16byte 消息头
     */
    protected MsgHeader msgHeader;

    // 消息体字节数组
//	@JSONField(serialize=false)
    protected byte[] msgBodyBytes;

    /**
     * 校验码 1byte
     */
    protected int checkSum;

//	@JSONField(serialize=false)
//	protected Channel channel;

    public MsgHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MsgHeader msgHeader) {
        this.msgHeader = msgHeader;
    }

    public byte[] getMsgBodyBytes() {
        return msgBodyBytes;
    }

    public void setMsgBodyBytes(byte[] msgBodyBytes) {
        this.msgBodyBytes = msgBodyBytes;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }

    //	public Channel getChannel() {
//		return channel;
//	}
//
//	public void setChannel(Channel channel) {
//		this.channel = channel;
//	}
    public PackageData() {
    }

    protected PackageData(Parcel in) {
        msgBodyBytes = in.createByteArray();
        checkSum = in.readInt();
    }

    public static final Creator<PackageData> CREATOR = new Creator<PackageData>() {
        @Override
        public PackageData createFromParcel(Parcel in) {
            MsgHeader msgHeader = in.readParcelable(MsgHeader.class.getClassLoader());
            PackageData packageData = new PackageData(in);
            packageData.setMsgHeader(msgHeader);//增加内部类序列
            return packageData;
//            return new PackageData(in);
        }

        @Override
        public PackageData[] newArray(int size) {
            return new PackageData[size];
        }
    };

    @Override
    public String toString() {
        return "PackageData [msgHeader=" + msgHeader + ", msgBodyBytes=" + Arrays.toString(msgBodyBytes) + ", checkSum="
                + checkSum + ", address=" + "]";// + channel
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(msgHeader, flags);//内部类
        dest.writeByteArray(msgBodyBytes);
        dest.writeInt(checkSum);
    }

    public static class MsgHeader implements Parcelable {
        // 消息ID
        protected int msgId;

        /////// ========消息体属性
        // byte[2-3]
        protected int msgBodyPropsField;
        // 消息体长度
        protected int msgBodyLength;
        // 数据加密方式
        protected int encryptionType;
        // 是否分包,true==>有消息包封装项
        protected boolean hasSubPackage;
        // 保留位[14-15]
        protected String reservedBit;
        /////// ========消息体属性

        // 终端手机号
        protected String terminalPhone;
        // 流水号
        protected int flowId;

        //////// =====消息包封装项
        // byte[12-15]
        protected int packageInfoField;
        // 消息包总数(word(16))
        protected long totalSubPackage;
        // 包序号(word(16))这次发送的这个消息包是分包中的第几个消息包, 从 1 开始
        protected long subPackageSeq;
        //////// =====消息包封装项

        public MsgHeader() {

        }

        protected MsgHeader(Parcel in) {
            msgId = in.readInt();
            msgBodyPropsField = in.readInt();
            msgBodyLength = in.readInt();
            encryptionType = in.readInt();
            hasSubPackage = in.readByte() != 0;
            reservedBit = in.readString();
            terminalPhone = in.readString();
            flowId = in.readInt();
            packageInfoField = in.readInt();
            totalSubPackage = in.readLong();
            subPackageSeq = in.readLong();
        }

        public static final Creator<MsgHeader> CREATOR = new Creator<MsgHeader>() {
            @Override
            public MsgHeader createFromParcel(Parcel in) {
                return new MsgHeader(in);
            }

            @Override
            public MsgHeader[] newArray(int size) {
                return new MsgHeader[size];
            }
        };

        public int getMsgId() {
            return msgId;
        }

        public void setMsgId(int msgId) {
            this.msgId = msgId;
        }

        public int getMsgBodyLength() {
            return msgBodyLength;
        }

        public void setMsgBodyLength(int msgBodyLength) {
            this.msgBodyLength = msgBodyLength;
        }

        public int getEncryptionType() {
            return encryptionType;
        }

        public void setEncryptionType(int encryptionType) {
            this.encryptionType = encryptionType;
        }

        public String getTerminalPhone() {
            return terminalPhone;
        }

        public void setTerminalPhone(String terminalPhone) {
            this.terminalPhone = terminalPhone;
        }

        public int getFlowId() {
            return flowId;
        }

        public void setFlowId(int flowId) {
            this.flowId = flowId;
        }

        public boolean isHasSubPackage() {
            return hasSubPackage;
        }

        public void setHasSubPackage(boolean hasSubPackage) {
            this.hasSubPackage = hasSubPackage;
        }

        public String getReservedBit() {
            return reservedBit;
        }

        public void setReservedBit(String reservedBit) {
            this.reservedBit = reservedBit;
        }

        public long getTotalSubPackage() {
            return totalSubPackage;
        }

        public void setTotalSubPackage(long totalPackage) {
            this.totalSubPackage = totalPackage;
        }

        public long getSubPackageSeq() {
            return subPackageSeq;
        }

        public void setSubPackageSeq(long packageSeq) {
            this.subPackageSeq = packageSeq;
        }

        public int getMsgBodyPropsField() {
            return msgBodyPropsField;
        }

        public void setMsgBodyPropsField(int msgBodyPropsField) {
            this.msgBodyPropsField = msgBodyPropsField;
        }

        public void setPackageInfoField(int packageInfoField) {
            this.packageInfoField = packageInfoField;
        }

        public int getPackageInfoField() {
            return packageInfoField;
        }

        @Override
        public String toString() {
            return "MsgHeader [msgId=" + msgId + ", msgBodyPropsField=" + msgBodyPropsField + ", msgBodyLength="
                    + msgBodyLength + ", encryptionType=" + encryptionType + ", hasSubPackage=" + hasSubPackage
                    + ", reservedBit=" + reservedBit + ", terminalPhone=" + terminalPhone + ", flowId=" + flowId
                    + ", packageInfoField=" + packageInfoField + ", totalSubPackage=" + totalSubPackage
                    + ", subPackageSeq=" + subPackageSeq + "]";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(msgId);
            dest.writeInt(msgBodyPropsField);
            dest.writeInt(msgBodyLength);
            dest.writeInt(encryptionType);
            dest.writeByte((byte) (hasSubPackage ? 1 : 0));
            dest.writeString(reservedBit);
            dest.writeString(terminalPhone);
            dest.writeInt(flowId);
            dest.writeInt(packageInfoField);
            dest.writeLong(totalSubPackage);
            dest.writeLong(subPackageSeq);
        }
    }

}
