package com.onfacemind.mculib.GATEControl;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;

import android_serialport_api.SerialPort;


/**
 * Created by 刘承. on 2018/4/18.
 * 创建 485 输出串口
 * <p>
 * SerialPort 使用卡尔jar中的
 */

public class SeriaProtController {
    private static final String TAG = "SeriaProtController";
    private static SeriaProtController seriaProtController;
    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    public static SeriaProtController instance() {
        if (seriaProtController == null) {
            seriaProtController = new SeriaProtController();
        }
        return seriaProtController;
    }

    private SeriaProtController() {
    }

    /**
     * 创建
     */
    public boolean create(String path, int baudrate) {
        if (mSerialPort == null) {
            try {
                mSerialPort = getSerialPort(path, baudrate);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "SeriaProtController: 串口启动失败");
                return false;
            }
        }
        if (mSerialPort == null) return false;
        return true;
    }


    /**
     * 发送打印命令
     *
     * @param bytes
     */
    public void sendCommand(byte[] bytes) {
        try {
            if (mOutputStream != null) {
                mOutputStream.write(bytes);
                mOutputStream.flush();
                Log.d(TAG, "sendCommand: Lib发送数据成功=" + Arrays.toString(bytes));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //sleep(100);
    }

    public InputStream getmInputStream() {
        return mInputStream;
    }

    private SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPort == null) {
            /* Read serial port parameters */
//			SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
//			String path = sp.getString("DEVICE", "");
//			int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));


//            String path = "/dev/ttySAC3";//902闸机RS485协议控制
//            int baudrate = 9600;//902闸机RS485协议波特率
//            path = path;//902闸机RS485协议控制
//            String path = "/dev/ttySAC1";
//            int baudrate = 115200;
//
            /* Check parameters */
//            if ((path.length() == 0) || (baudrate == -1)) {
//                //throw new InvalidParameterException();
//                /*use default value.    Nirvana 0710*/
////				path = "/dev/ttyS1";
//                path = "/dev/ttySAC1";
//                baudrate = 115200;
//            }

            /* Open the serial port */
            try {
                mSerialPort = new SerialPort(new File(path), baudrate, 0);
//                mSerialPort = new SerialPort(path, baudrate, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            try {
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();

                /* Create a receiving thread */
//                mReadThread = new ReadThread();//暂不使用接收线程
//                mReadThread.start();
            } catch (SecurityException e) {
                Log.e(TAG, "You do not have read/write permission to the serial port.");
            } catch (InvalidParameterException e) {
                Log.e(TAG, "Please configure your serial port first.");
            }
        }
        return mSerialPort;
    }

    //读取线程
    class readThread extends Thread {
        @Override
        public void run() {
            super.run();
        }
    }

    public void closePrinter() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
