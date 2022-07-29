package com.example.myapplication.lanchat.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import com.example.myapplication.R;
import com.example.myapplication.lanchat.Bean.Msg;

import java.util.List;

/**
 * 该工具类，包含许多其他杂七杂八的小功能函数
 */
public class OtherUtil {
    /**
/*     * 初始化消息数据
     * @param msgList
     *//*
    private void initMsg(List<Msg> msgList) {
        Msg msg1 = new Msg(Msg.RECEIVED,Msg.TEXT,App.getImageId(),"I miss you!");
        msgList.add(msg1);

        Msg msg2 = new Msg(Msg.SENT,Msg.TEXT,App.getImageId(),"I miss you,too!");
        msgList.add(msg2);

        Msg msg3 = new Msg(Msg.RECEIVED,Msg.TEXT,App.getImageId(),"I will come back soon!");
        msgList.add(msg3);

    }*/



    /**
     * 获取连上wifi后的IP地址
     * @param context
     * @return
     */
    public static String getWifiIp(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    /**
     * 整型转IP
     * @param ipInt
     * @return
     */
    public static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }


}
