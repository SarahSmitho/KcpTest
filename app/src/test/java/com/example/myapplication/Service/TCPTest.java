/*
package com.example.myapplication.Service;

import static com.example.myapplication.lanchat.Bean.Msg.IMAGE;
import static com.example.myapplication.lanchat.Bean.Msg.TEXT;
import static com.example.myapplication.lanchat.Bean.Msg.RECEIVED;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.myapplication.lanchat.Bean.Msg;
import com.example.myapplication.lanchat.Event.MessageEvent;
import com.example.myapplication.lanchat.Util.Base64Util;
import com.example.myapplication.lanchat.Util.PictureUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import io.noties.debug.Debug;

public class TCP {

    private static final boolean DEBUG = true;

    //??
    private static final String TAG = "TCP";
    byte data1[] = new byte[10 * 1024 * 1024];
    //IP地址
    private String ipAddr;
    //客户端socket（发送）
    private Socket mSocket;
    //服务端socket（接收）
    private ServerSocket mServerSocket;
    private LinkedBlockingQueue<Msg> queue = new LinkedBlockingQueue<>();
    private boolean running = true;

    */
/**
     * 对方的ip地址在构造方法中传入
     *
     * @param ipAddr
     * @throws SocketException
     *//*

    public TCP(String ipAddr) throws IOException {
        this.ipAddr = ipAddr;
    }

    public void sendMsg(Msg msg) {
        try {
            queue.put(msg);
            Log.d(TAG, "TCP客户端发送消息 sendMsg: 往tcp对象增加msg成功");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {

    }

    public void stop() {

    }

    public void ClientSend() {


        try {
            mSocket = new Socket(ipAddr,6000);
            Log.d(TAG, "TCP客户端发送消息   ClientSend: 生成TCP Socket对象成功");
        } catch (IOException e) {
            e.printStackTrace();
            //必须要有接收方，不然就会报错
            //java.net.ConnectException: failed to connect to /127.0.0.1 (port 6000) from
            // /:: (port 46110): connect failed: ECONNREFUSED (Connection refused)
        }

        try {
            OutputStream outputStream = mSocket.getOutputStream();
            Log.d(TAG, "TCP客户端发送消息   ClientSend: 生成输出流对象成功");
            byte[] lenBuf = new byte[4];
            while (running) {
                Log.d(TAG, "TCP客户端发送消息   ClientSend: 从queque里面取消息");
                // ???两边这个log为甚么呢
                Msg msg = queue.take();
                if (msg != null) {
                    byte contentType = (byte) msg.getContentType();
                    outputStream.write(contentType);
                    switch (contentType) {
                        case TEXT: {
                            Log.d(TAG, "TCP客户端发送消息   ClientSend: 文字情形");
                            String content = msg.getContent();
                            Log.d(TAG, "TCP客户端发送消息   ClientSend: 文字内容="+content);
                            byte[] buf = content.getBytes(StandardCharsets.UTF_8);
                            int length = buf.length;
                            lenBuf[0] = (byte) ((length >> 24) & 0xff);
                            lenBuf[1] = (byte) ((length >> 16) & 0XFF);
                            lenBuf[2] = (byte) ((length >> 8) & 0xff);
                            lenBuf[3] = (byte) ((length & 0xff));
                            Log.d(TAG, "TCP客户端发送消息   ClientSend: lenBuf="+lenBuf);
                            outputStream.write(lenBuf);
                            outputStream.write(buf);
                            Log.d(TAG, "TCP客户端发送消息   ClientSend: 发送文字写入到输出流成功");
                            break;
                        }

                        case IMAGE: {
                            Log.d(TAG, "TCP客户端发送消息   ClientSend: 图片情形");
                            byte[] imageBuf = msg.getImageData();
                            int length = imageBuf.length;
                            lenBuf[0] = (byte) ((length >> 24) & 0xff);
                            lenBuf[1] = (byte) ((length >> 16) & 0XFF);
                            lenBuf[2] = (byte) ((length >> 8) & 0xff);
                            lenBuf[3] = (byte) ((length & 0xff));
                            outputStream.write(lenBuf);
                            outputStream.write(imageBuf);
                            break;
                        }
                    }
                }
            }
            outputStream.flush();
            mSocket.shutdownOutput();
            Log.d(TAG, "TCP客户端发送消息   ClientSend: Socket关闭");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void ClientReceice() {
        Log.d(TAG, "TCP客户端接收消息  ClientReceice: TCP客户端接收消息");
        try {
            InputStream inputStream = mSocket.getInputStream();
            int contentType = inputStream.read();
            byte[] lenBuf = new byte[4];
            readAll(inputStream, lenBuf, 0, 4);
            int length = ((lenBuf[0] << 24) & 0xff) | ((lenBuf[1] << 16) & 0xff) |
                    ((lenBuf[2] << 8) & 0xff) | ((lenBuf[3]) & 0xff);
            switch (contentType) {
                case TEXT: {
                    byte[] textContent = new byte[length];
                    readAll(inputStream, textContent, 0, length);

                    String content = new String(textContent, 0, length, StandardCharsets.UTF_8);
                    Msg msg = new Msg(RECEIVED, contentType, length, content);
                    if (msg != null) {
                        MessageEvent messageEvent = new MessageEvent(TEXT, length, content);
                        // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                        EventBus.getDefault().post(messageEvent);
                    }
                    break;
                }

                case IMAGE: {
                    byte[] imageContent = new byte[length];
                    readAll(inputStream, imageContent, 0, length);
                    Msg msg = new Msg(RECEIVED, contentType, length, imageContent);
                    if (msg != null) {
                        MessageEvent messageEvent = new MessageEvent(IMAGE, length, imageContent);
                        // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                        EventBus.getDefault().post(messageEvent);
                    }
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAll(InputStream inputStream, byte[] buf, int offset, int length) throws IOException {
        int readSize = 0;

        while (readSize < length) {
            //是文件读的长度！！！！不是数量
            int size = inputStream.read(buf, offset + readSize, length - readSize);
            readSize = readSize + size;
            if (DEBUG) Debug.v("readAll: 正在执行");
        }
        Log.d(TAG, "readAll: 执行成功");
    }

    public void ServerReceive() {
        while (true) {
            try {
                mServerSocket = new ServerSocket(6000);
                Log.d(TAG, "TCP服务端接收消息   ServerReceive: 开启服务端成功");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "TCP服务端接收消息   ServerReceive: 开启服务端失败");
            }

            try {
                Socket mReceiveSocket=mServerSocket.accept();
                Log.d(TAG, "TCP服务端接收消息   ServerReceive: 获取输入流");
                InputStream inputStream = mReceiveSocket.getInputStream();
                while (true) {
                    if (inputStream.available() > 0) {
                        int contentType = inputStream.read();
                        byte[] lenBuf = new byte[4];
                        readAll(inputStream, lenBuf, 0, 4);
                        //卡着这里不往下走了，在这里跳出来了，就应该在这个方法里打log
                        int length = ((lenBuf[0] <<  24) & 0xff) | ((lenBuf[1] << 16) & 0xff) |
                                ((lenBuf[2] << 8) & 0xff) | ((lenBuf[3]) & 0xff);
                        switch (contentType) {
                            case TEXT: {
                                Log.d(TAG, "TCP服务端接收消息   ServerReceive: 文字情形");
                                byte[] textContent = new byte[length];
                                readAll(inputStream, textContent, 0, length);

                                String content = new String(textContent, 0, length, StandardCharsets.UTF_8);
                                Msg msg = new Msg(RECEIVED, contentType, length, content);

                                if (msg != null) {
                                    MessageEvent messageEvent = new MessageEvent(TEXT, length, content);
                                    // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                                    EventBus.getDefault().post(messageEvent);
                                    Log.d(TAG, "TCP服务端接收消息   EventBus: 文字情形给Activity发消息");
                                }
                                break;
                            }

                            case IMAGE: {
                                Log.d(TAG, "TCP服务端接收消息   ServerReceive: 图片情形");
                                byte[] imageContent = new byte[length];
                                readAll(inputStream, imageContent, 0, length);
                                Msg msg = new Msg(RECEIVED, contentType, length, imageContent);
                                if (msg != null) {
                                    MessageEvent messageEvent = new MessageEvent(IMAGE, length, imageContent);
                                    // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                                    EventBus.getDefault().post(messageEvent);
                                    Log.d(TAG, "TCP服务端接收消息   EventBus: 图片情形给Activity发消息");
                                }
                                break;
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}




*/
