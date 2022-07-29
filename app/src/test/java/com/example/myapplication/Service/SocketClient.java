package com.example.myapplication.Service;

import android.util.Log;

import com.example.myapplication.lanchat.Event.MessageEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * author: xpf
 * date: 2020/1/2 14:46
 * description: socket客户端流程
 */
public class SocketClient {
    public static void main(String[] args) throws IOException {
        // 1 创建一个客户端Socket 指定服务器的ip地址和端口
        Socket socket = new Socket("127.0.0.1",12346);
        // 2 获取输出流，向服务器发送信息
        OutputStream os = socket.getOutputStream();//向服务器写信息
        PrintWriter pw = new PrintWriter(os);//将输出流包装成打印流
        pw.write("Hello 服务器");
        pw.flush();
        socket.shutdownOutput();//关闭输出流
        socket.close();
    }

    /**
     * 图片转换为文件流并发送，客户端通过Socket发送
     *
     * @param path
     * @throws FileNotFoundException
     */
    public void sendImage(String path) throws FileNotFoundException {

        System.out.println("==================");
        //1、创建一个Socket，连接到服务器端、指定端口号。放在子线程中运行，否则会有问题。
        try {//指定ip地址和端口号
            Log.d(TAG, "sendImage:生成TCP客户端Socket对象 ipAddr=" + ipAddr);
            mSocket = new Socket(ipAddr, 6000);

            File file = new File(path);
            Log.d(TAG, "sendImage: 图片路径 path=" + path);
            FileInputStream in = new FileInputStream(file);
            Byte bIMAGE = new Byte(IMAGE);
            int length = in.available();
            Integer iLength = new Integer(length);

            byte[] data=new byte[1024];
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            int readLength=0;
            while ((readLength=in.read(data))>0){
                bout.write(data,0,readLength);
            }

            ArrayList arrayList = new ArrayList();
            arrayList.add(bIMAGE);
            arrayList.add(iLength);
            arrayList.add(new String(data));

            Byte[] arrayListByteArray = (Byte[]) arrayList.toArray(new Byte[arrayList.size()]);
            //Arrays.toString(arrayList.toArray()).getBytes();
            //Object[] arrayListByteArray = arrayList.toArray();

            OutputStream mOutStream = mSocket.getOutputStream();
            DataOutputStream dataOutputStream=new DataOutputStream(mOutStream);
            dataOutputStream.write(arrayListByteArray);
            mOutStream.write(bout.toByteArray());

            mOutStream.flush();
            mSocket.shutdownOutput();
            Log.d(TAG, "sendImage: 图片发送完了！");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "sendImage: 发送socket异常");
        }

    }


    /**
     * 接收图片，服务端通过ServerSocket接收
     */
    public void receiveImage() throws SocketException {

        while (true) {

            try {
                mServerSocket = new ServerSocket(6000);
                Log.d(TAG, "receiveImage: 开启服务端成功");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "receiveImage: 开启服务端失败");
            }

            try {
                mSocket = mServerSocket.accept();
                InputStream mInStream = mSocket.getInputStream();
                DataInputStream dis = new DataInputStream(mInStream);
                byte[] type = new byte[1];
                //第一步接收消息类型,把信息为IMAGE读到type里
                dis.read(type);
                //接收长度
                int readLength = -1;
                int totalLength = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((readLength = mInStream.read(buffer)) > 0) {
                    baos.write(buffer, 0, readLength);
                    totalLength += readLength;
                }
                Log.d(TAG, "receiveImage: 读完数据了");
                baos.flush();
                byte[] receiveData = baos.toByteArray();
                baos.close();
                if (receiveData != null) {
                    MessageEvent messageEvent = new MessageEvent(IMAGE, totalLength, baos.toByteArray());
                    // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                    EventBus.getDefault().post(messageEvent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}


