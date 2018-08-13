package android_serialport_api;
/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

import com.onfacemind.mculibrary.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SerialPort {

    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

		/* Check access permission */
        if (!device.canRead() || !device.canWrite()) {

            try {
                Log.e(TAG, "1============================");
                // Missing read/write permission, trying to chmod the file
                Process su;
                su = Runtime.getRuntime().exec("/system/xbin/su");
                //String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                //+ "exit\n";
                String cmd = "chmod 666 " + device.getAbsolutePath();
                Log.e(TAG, "cmd=====================" + cmd);
                Runtime.getRuntime().exec(cmd);
                //su.getOutputStream().write(cmd.getBytes());
                if (/*(su.waitFor() != 0) || */!device.canRead()
                        || !device.canWrite()) {
                    Log.e(TAG, "2=====================");
                    throw new SecurityException();
                }
            } catch (Exception e) {
                Log.e(TAG, "3=================");
                e.printStackTrace();
                throw new SecurityException();
            }

            //do_exec("su root\n");
            //Log.e(TAG, "=============cmd : "+device.getAbsolutePath());
            //do_exec("chmod 755 " + device.getAbsolutePath() + "\n");
            //do_exec("rm /sdcard/123");
            //Runtime.getRuntime().exec("/system/xbin/su");
            //Runtime.getRuntime().exec("chmod 755 /dev/ttyS1");

        }

        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    String do_exec(String cmd) {
        String s = "/n";
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line + "/n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cmd;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {
        System.loadLibrary("MCUSerial_port");//文件名
    }
}
