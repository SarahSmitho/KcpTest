package com.example.myapplication.lanchat.Bean;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * 消息体
 */
public class Msg {
    public static final int RECEIVED = 0;//收到一条消息
    public static final int SENT = 1;//发出一条消息
    public static final int TEXT = 2;//消息内容为文字
    public static final byte IMAGE = 3;//消息内容为图片
    //?　？
    public int length; //文字、图片的长度
    private int type;//消息的类型
    private int contentType;//消息内容的类型
    //private int imageId;//头像id
    private String content;//消息的文字内容
    private Uri imageUri;//消息的图片Uri
    private Bitmap imageBitmap;//消息的图片Bitmap

    public byte[] getImageData() {
        return imageData;
    }

    private byte[]  imageData;

    /**
     * 构造函数（文字消息）
     *
     * @param type
     * @param contentType
     * @param content
     */
    public Msg(int type, int contentType, int length, String content) {
        this.type = type;
        this.contentType = contentType;
        this.length = length;
        this.content = content;
    }

    /**
     * 构造函数（图片消息）
     *
     * @param type
     * @param contentType
     * @param imageUri
     */
    public Msg(int type, int contentType, int length, Uri imageUri) {
        this.type = type;
        this.contentType = contentType;
        this.length = length;
        this.imageUri = imageUri;
    }

    public Msg(int type, int contentType, int length,byte[] imageData) {
        this.type = type;
        this.contentType = contentType;
        this.length = length;
        this.imageData = imageData;
    }

    public Msg(int type, int contentType, int length,byte[] imageData,Uri imageUri) {
        this.type = type;
        this.contentType = contentType;
        this.length = length;
        this.imageData = imageData;
        this.imageUri = imageUri;
    }

    /**
     * 构造函数（头像ID消息）
     *
     * @param type
     * @param contentType
     * @param imageBitmap
     */
    public Msg(int type, int contentType,  int length, Bitmap imageBitmap) {
        this.type = type;
        this.contentType = contentType;
        this.length=length;
        //this.imageId = imageId;
        this.imageBitmap = imageBitmap;
    }

    /**
     * getType
     *
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * setType
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * getContentType
     *
     * @return
     */
    public int getContentType() {
        return contentType;
    }

    /**
     * setContentType
     *
     * @param contentType
     */
    public void setContentType(int contentType) {
        this.contentType = contentType;
    }


    /**
     * getContent
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * setContent
     *
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * getImageUri
     *
     * @return
     */
    public Uri getImageUri() {
        return imageUri;
    }

    /**
     * setImageUri
     *
     * @param imageUri
     */
    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * getImageBitmap
     *
     * @return
     */
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    /**
     * setImageBitmap
     *
     * @param imageBitmap
     */
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
