package com.example.myapplication.lanchat.socket;

import static com.example.myapplication.lanchat.Bean.Msg.IMAGE;
import static com.example.myapplication.lanchat.Bean.Msg.RECEIVED;
import static com.example.myapplication.lanchat.Bean.Msg.TEXT;

import android.net.Uri;

import com.example.myapplication.lanchat.Bean.Msg;
import com.example.myapplication.lanchat.Event.MessageEvent;
import com.example.myapplication.lanchat.Util.NetworkUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

import io.noties.debug.Debug;

/**
 * @author liaoqg
 * @brief description
 * @date 2022-08-02
 */
public class TCP {
    private static final boolean DEBUG = true;
    NetworkUtils networkUtils = new NetworkUtils();
    byte data1[] = new byte[10 * 1024 * 1024];
    //IP地址
    private String ipAddr;
    private String sendLocalIP;
    private TCP.TcpReceiverThread receiverThread;
    private TCP.TcpClientSenderThread senderThread;
    //客户端socket（发送）
    private Socket mSocket;
    //服务端socket（接收）
    private ServerSocket mServerSocket;
    private LinkedBlockingQueue<Msg> queue = new LinkedBlockingQueue<>();


    /**
     * 对方的ip地址在构造方法中传入
     *
     * @param ipAddr
     * @throws SocketException
     */
    public TCP(String ipAddr) throws SocketException {
        this.ipAddr = ipAddr;
    }

    public void sendMsg(Msg msg) {
        try {
            queue.put(msg);
            Debug.v("TCP客户端发送消息 sendMsg: 往tcp对象增加msg成功");
            Debug.v("TCP客户端发送消息 ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() throws InterruptedException {
        Debug.v("TCP客户端开启线程  服务端开启线程");
        if (null == receiverThread) {
            receiverThread = new TCP.TcpReceiverThread();
            receiverThread.start();
        }

        //还是那个问题，服务端要比客户端先启动
        if (null == senderThread) {
            senderThread = new TCP.TcpClientSenderThread();

            senderThread.start();

            senderThread.sleep(100);
        }
    }

    public void stop() throws IOException {
        Debug.v("TCP客户端关闭线程  服务端关闭线程");
        if (null != receiverThread) {
            //receiverThread.stopThread(); 暂时不解决
            receiverThread = null;
        }

        if (null != senderThread) {
            senderThread.stopThread();
            senderThread = null;
        }
    }

    public void stopThread() throws IOException {
        //running = false;  报错，暂时不解决
        mServerSocket.close();
    }

    private void readAll(InputStream inputStream, byte[] buf, int offset, int length) throws IOException {
        int readSize = 0;

        while (readSize < length) {
            //是文件读的长度！！！！不是数量
            int size = inputStream.read(buf, offset + readSize, length - readSize);
            readSize = readSize + size;
            if (DEBUG) Debug.v("readAll: 正在执行");
        }
        Debug.v("readAll: 执行成功");
    }

    private class TcpClientSenderThread extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            super.run();
            running = true;
            try {
                mSocket = new Socket(ipAddr, 6000);
                Debug.v("TCP客户端发送消息   ClientSend: 生成TCP Socket对象成功");
            } catch (IOException e) {
                e.printStackTrace();
                //必须要有接收方，不然就会报错,而且接收端必须在客户端前面生成
                //java.net.ConnectException: failed to connect to /127.0.0.1 (port 6000) from
                // /:: (port 46110): connect failed: ECONNREFUSED (Connection refused)
            }

            try {
                OutputStream outputStream = mSocket.getOutputStream();
                Debug.v("TCP客户端发送消息   ClientSend: 生成输出流对象成功");
                byte[] lenBuf = new byte[4];
                while (running) {
                    Debug.v("TCP客户端发送消息   ClientSend: 从queque里面取消息");
                    // ???两边这个log为甚么呢 ????这里为什么会卡住
                    Msg msg = queue.take();
                    Debug.v("TCP客户端发送消息   ClientSend: 从queque里面取消息成功");
                    if (msg != null) {
                        byte contentType = (byte) msg.getContentType();
                        outputStream.write(contentType);
                        switch (contentType) {
                            case TEXT: {
                                Debug.v("TCP客户端发送消息   ClientSend: 文字情形");
                                String content = msg.getContent();
                                Debug.v("TCP客户端发送消息   ClientSend: 文字内容=" + content);
                                byte[] buf = content.getBytes(StandardCharsets.UTF_8);
                                int length = buf.length;
                                lenBuf[0] = (byte) ((length >> 24) & 0xff);
                                lenBuf[1] = (byte) ((length >> 16) & 0XFF);
                                lenBuf[2] = (byte) ((length >> 8) & 0xff);
                                lenBuf[3] = (byte) ((length & 0xff));
                                Debug.v("TCP客户端发送消息   ClientSend: lenBuf=" + lenBuf);
                                outputStream.write(lenBuf);
                                outputStream.write(buf);
                                Debug.v("TCP客户端发送消息   ClientSend: 发送文字写入到输出流成功");
                                break;
                            }

                            case IMAGE: {
                                Debug.v("TCP客户端发送消息  图片情形  ClientSend: 图片情形");
                                byte[] imageBuf = msg.getImageData();
                                int length = imageBuf.length;
                                lenBuf[0] = (byte) ((length >> 24) & 0xff);
                                lenBuf[1] = (byte) ((length >> 16) & 0xff);
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
                Debug.v("TCP客户端发送消息   ClientSend: Socket关闭");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stopThread() throws IOException {
            running = true;
            mSocket.close();
        }
    }

    private class TcpReceiverThread extends Thread {
        private boolean running = false;

        @Override
        public void run() {
            super.run();
            running = true;
            Debug.v("udp receiver thread started");
            //while (true) {
            try {
                mServerSocket = new ServerSocket(6000);

                Debug.v("TCP服务端接收消息   TcpReceiverThread: 开启服务端成功");
            } catch (IOException e) {
                e.printStackTrace();
                Debug.v("TCP服务端接收消息   TcpReceiverThread: 开启服务端失败");
            }

            Socket mReceiveSocket = null;
            try {
                //2022-08-02 22:46:11.067 28378-28808/com.example.myapplication E/AndroidRuntime: FATAL EXCEPTION: Thread-8
                //    Process: com.example.myapplication, PID: 28378
                //    java.lang.NullPointerException: Attempt to invoke virtual method 'java.net.Socket java.net.ServerSocket.accept()' on a null object reference
                //        at com.example.myapplication.lanchat.socket.TCP$TcpReceiverThread.run(TCP.java:212)
                mReceiveSocket = mServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                try {
                    //为什么输入流里一直有输入的数据 if (inputStream.available() > 0) {
                    //socket是阻塞式通信，accep()是会阻塞的和，read()也会阻塞，一直到有东西连接为止，
                    // 或者有数据可读，所以在这个循环中，第一次的时候因为有客户端连接，所以能够执行后面的代码，
                    // 输出数据，但后面开始就没有客户端连接，所以就一直阻塞在accept()，导致后面的没法执行
                    Debug.v("TCP服务端接收消息   TcpReceiverThread: 获取输入流");
                    InputStream inputStream = mReceiveSocket.getInputStream();
                    int contentType = inputStream.read();
                    byte[] lenBuf = new byte[4];
                    readAll(inputStream, lenBuf, 0, 4);
                    //卡着这里不往下走了，在这里跳出来了，就应该在这个方法里打log
                       /* int length = ((lenBuf[0] << 24) & 0xff) | ((lenBuf[1] << 16) & 0xff) |
                                ((lenBuf[2] << 8) & 0xff) | ((lenBuf[3]) & 0xff);*/
                    int length = 0;
                    for (int i = 0; i < lenBuf.length; i++) {
                        length += (lenBuf[i] & 0xff) << ((3 - i) * 8);
                    }


                    switch (contentType) {
                        case TEXT: {
                            Debug.v("TCP服务端接收消息   TcpReceiverThread: 文字情形");
                            byte[] textContent = new byte[length];
                            readAll(inputStream, textContent, 0, length);

                            String content = new String(textContent, 0, length, StandardCharsets.UTF_8);
                            Msg msg = new Msg(RECEIVED, contentType, length, content);

                            if (msg != null) {
                                MessageEvent messageEvent = new MessageEvent(TEXT, length, content);
                                // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                                EventBus.getDefault().post(messageEvent);
                                Debug.v("TCP服务端接收消息   EventBus: 文字情形给Activity发消息");
                            }
                            break;
                        }

                        case IMAGE: {
                            Debug.v("TCP服务端接收消息  图片情形   TcpReceiverThread: 图片情形");
                            byte[] imageContent = new byte[length];
                            Debug.v("TCP服务端接收消息  图片情形   TcpReceiverThread: imageContent");
                            readAll(inputStream, imageContent, 0, length);
                            Msg msg = new Msg(RECEIVED, contentType, length, imageContent);
                            if (msg != null) {
                                MessageEvent messageEvent = new MessageEvent(IMAGE, length, imageContent);
                                // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                                EventBus.getDefault().post(messageEvent);
                                Debug.v("TCP服务端接收消息   EventBus: 图片情形给Activity发消息");
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}

