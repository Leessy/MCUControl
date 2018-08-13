package android_serialport_api;

import com.onfacemind.mculibrary.BuildConfig;
import com.onfacemind.mculibrary.Log;

import com.onfacemind.mculibrary.JT808.util.HexStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;

/**
 * Created by 刘承. on 2018/4/18.
 */

public class SeriaProtController {
    private static final String TAG = "SeriaProtController";
    private static SeriaProtController seriaProtController;
    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    readThread mReadThread;

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
        return true;
    }


    /**
     * 发送命令
     *
     * @param bytes
     */
    public void sendCommand(byte[] bytes) {
        try {
            if (mOutputStream != null) {
                mOutputStream.write(bytes);
                mOutputStream.flush();
                Thread.sleep(90);
                if (BuildConfig.DEBUG) {
                    String s = HexStringUtils.toHexString(bytes);
                    Log.d(TAG, "sendCommand: Lib发送数据成功=" + Arrays.toString(bytes));
                    Log.d(TAG, "sendCommand: Lib发送数据成功=" + s + "   threadName=" + Thread.currentThread().getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


//            path = "/dev/ttySAC3";
//            baudrate = 115200;

            /* Open the serial port */
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
            try {
                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();

                /* Create a receiving thread */
                mReadThread = new readThread();//暂不使用接收线程
                mReadThread.start();
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

    // 关闭串口
    public void close() {
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
