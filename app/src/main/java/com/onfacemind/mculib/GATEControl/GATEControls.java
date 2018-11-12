package com.onfacemind.mculib.GATEControl;

import android.util.Log;


/**
 * Created by 刘承. on 2018/4/24.
 */

public class GATEControls {
    private static final String TAG = "GATEControls";
    public static boolean init;
    static GATEControls gateControls = new GATEControls();

    public static GATEControls instance() {
        return gateControls;
    }

    public boolean createSerialProt() {
        if (!init)
            init = SeriaProtController.instance().create("/dev/ttySAC3", 9600);//固定地址
        return init;
    }


    //test
    public void testData() {
        if (init) {
            SeriaProtController.instance().sendCommand("123456789".getBytes());
        }
    }

    //左开
    public void leftopen() {
        if (init)
            sendMsg(0x40);
        else Log.d(TAG, "leftopen: 发送失败 串口未初始化");
    }

    //右开
    public void rightopen() {
        if (init)
            sendMsg(0x41);
        else Log.d(TAG, "leftopen: 发送失败 串口未初始化");
    }

    void sendMsg(int queryCalss) {
        byte[] msgBodyBytes = new byte[5];
        msgBodyBytes[0] = integerTo1Byte(0xEB);//同步码
        msgBodyBytes[1] = integerTo1Byte(0x01);//设备地址待定
        msgBodyBytes[2] = integerTo1Byte(queryCalss);//命令码
        msgBodyBytes[3] = integerTo1Byte(0);//命令码
        byte xor = msgBodyBytes[0];
        for (int i = 1; i < msgBodyBytes.length - 1; i++) {
            xor ^= msgBodyBytes[i];
        }
        msgBodyBytes[4] = xor;//校验码
        String s = toHexString(msgBodyBytes);
        Log.d(TAG, "sendMsg: =" + s);
        SeriaProtController.instance().sendCommand(msgBodyBytes);

//4字节模式
//        byte[] msgBodyBytes = new byte[4];
//        msgBodyBytes[0] = integerTo1Byte(0xEB);//同步码
//        msgBodyBytes[1] = integerTo1Byte(0x01);//设备地址待定
//        msgBodyBytes[2] = integerTo1Byte(queryCalss);//命令码
//        byte xor = msgBodyBytes[0];
//        for (int i = 1; i < msgBodyBytes.length - 1; i++) {
//            xor ^= msgBodyBytes[i];
//        }
//        msgBodyBytes[3] = xor;//校验码
//        String s = toHexString(msgBodyBytes);
//        Log.d(TAG, "sendMsg: =" + s);
//        pinterText("sendMsg: =" + s);
    }

    /**
     * 把一个整形该为byte
     *
     * @param value
     */
    public byte integerTo1Byte(int value) {
        return (byte) (value & 0xFF);
    }

    public static String toHexString(byte[] bs) {
        return new String(encodeHex(bs));
    }

    private static final char[] DIGITS_HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    protected static char[] encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_HEX[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_HEX[0x0F & data[i]];
        }
        return out;
    }

//        new readThread(SeriaProtController.instance().getmInputStream()).start();//启动读取线程
}
