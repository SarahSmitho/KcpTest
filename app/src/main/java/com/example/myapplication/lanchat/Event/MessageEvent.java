package com.example.myapplication.lanchat.Event;

import android.graphics.Bitmap;

import java.net.InetAddress;

/**
 * eventbus 事件（作为事件的载体）
 *     public static final int RECEIVED = 0;//收到一条消息
 *     public static final int SENT = 1;//发出一条消息
 *     public static final int TEXT = 2;//消息内容为文字
 *     public static final byte IMAGE = 3;//消息内容为图片
 */
public class MessageEvent {
    public String clientLocalIP; //客户端addressd对象
    private int contentType;//发出、收到消息
    private String words;//文字消息


    private int length;
    private byte[] image;//图片字节流
    private int imageId;//头像ID
    private Bitmap bitmap;

    /**
     * 构造函数（文字消息事件）
     *
     * @param contentType
     * @param words
     */
    public MessageEvent(int contentType,int length,String words) {
        this.contentType = contentType;
        this.words = words;
    }

    /**
     * 构造函数（图片消息事件）
     *
     * @param contentType
     * @param image
     */
    public MessageEvent(int contentType, int length,byte[] image) {
        this.contentType = contentType;
        this.image = image;
    }

    public MessageEvent(int contentType, int length,Bitmap image) {
        this.contentType = contentType;
        this.bitmap = image;
    }

    //传递得到的客户端IP对象
    public MessageEvent(String  clientLocalIP) {
        this.clientLocalIP = clientLocalIP;
    }

    /**
     * getType
     *
     * @return
     */
    public int getType() {
        return contentType;
    }


    public int getLength() {
        return length;
    }

    /**
     * getWords
     *
     * @return
     */
    public String getWords() {
        return words;
    }

    /**
     * getImage
     *
     * @return
     */
    public byte[] getImage() {
        return image;
    }
}
