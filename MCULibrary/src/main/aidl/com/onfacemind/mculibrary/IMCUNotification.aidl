// IMCUNotification.aidl
package com.onfacemind.mculibrary;
import com.onfacemind.mculibrary.PackageData;
// Declare any non-default types here with import statements

interface IMCUNotification {
     //回调原始数据
    void MCUDatas(in byte[] bs);

    //回调解析数据后的对象类型
    void MCUDataWhole(in PackageData data);
}
