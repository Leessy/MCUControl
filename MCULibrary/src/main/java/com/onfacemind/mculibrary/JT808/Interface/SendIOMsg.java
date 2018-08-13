package com.onfacemind.mculibrary.JT808.Interface;

/**
 * io 口数据发送
 */
public interface SendIOMsg {
    //    io口  1-6

//    端口 BYTE
//    根据硬件决定，目前只有6个IO输出
//    范围 1-254
//            1
//    状态 BYTE
//    高电平：1
//    低电平：0


// IO 暂不实现


    //高电平
    void SendIO_1_up();

    //低电平
    void SendIO_1_low();

    //高电平
    void SendIO_2_up();

    //低电平
    void SendIO_2_low();

    //高电平
    void SendIO_3_up();

    //低电平
    void SendIO_3_low();

    //高电平
    void SendIO_4_up();

    //低电平
    void SendIO_4_low();

    //高电平
    void SendIO_5_up();

    //低电平
    void SendIO_5_low();

    //高电平
    void SendIO_6_up();

    //低电平
    void SendIO_6_low();

}
