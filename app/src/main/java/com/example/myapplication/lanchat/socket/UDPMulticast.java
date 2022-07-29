package com.example.myapplication.lanchat.socket;


import com.example.myapplication.lanchat.Event.MessageEvent;
import com.example.myapplication.lanchat.Util.NetworkUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import io.noties.debug.Debug;


public class UDPMulticast {
    private static String multicastHost = "224.0.0.1";
    private String sendLocalIP;
    NetworkUtils networkUtils = new NetworkUtils();
    private UdpReceiverThread receiverThread;
    private UdpSender senderThread;
    public UDPMulticast() throws SocketException {
    }

    public void start() {
        Debug.v("starting");
        sendLocalIP = networkUtils.getIPAddress(true);
        Debug.v("local ip=%s", sendLocalIP);
        if (null == senderThread) {
            senderThread = new UdpSender();
            senderThread.start();
        }

        if (null == receiverThread) {
            receiverThread = new UdpReceiverThread();
            receiverThread.start();
        }
    }

    public void stop() {
        if (null != receiverThread) {
            receiverThread.stopThread();
            receiverThread = null;
        }

        if (null != senderThread) {
            senderThread.stopThread();
            senderThread = null;
        }
    }

    private class UdpSender extends Thread {
        private MulticastSocket multicastSocket;
        private  boolean running = false;
        @Override
        public void run() {
            super.run();
            running = true;
            Debug.v("udp sender started");
            InetAddress destAddress = null;
            try {
                destAddress = InetAddress.getByName(multicastHost);
                Debug.v("发送UDP客户端IP  UDPMulticastClient: 客户端生成多点广播Socket");
                multicastSocket = new MulticastSocket(10025);
                multicastSocket.setTimeToLive(50);
                Debug.v("发送UDP客户端IP  UDPMulticastClient: sendLocalIP=" + sendLocalIP);
                byte[] buf = sendLocalIP.getBytes(StandardCharsets.UTF_8);
                DatagramPacket dp = new DatagramPacket(buf, buf.length, destAddress, 10025);

                int count = 0;
                while (running && count < 20) {
                    multicastSocket.send(dp);
                    Debug.v( "发送UDP客户端IP  UDPMulticastClient: 发送成功");
                    Thread.sleep(100);
                    count ++;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stopThread() {
            running = true;
            multicastSocket.close();
        }
    }

    private class UdpReceiverThread extends Thread {
        private boolean running = false;
        private MulticastSocket receiveMulticast;

        @Override
        public void run() {
            super.run();
            running = true;
            Debug.v("udp receiver thread started");
            InetAddress receiveAddress = null;
            try {
                receiveAddress = InetAddress.getByName(multicastHost);
                receiveMulticast = new MulticastSocket(10025);
                receiveMulticast.joinGroup(receiveAddress);

                while (running) {
                    Debug.v("going to receive udp packet");
                    DatagramPacket dp = new DatagramPacket(new byte[13], 13);
                    receiveMulticast.receive(dp);
                    byte[] receivedClientLocalIP = dp.getData();
                    String remoteIp = new String(receivedClientLocalIP);
                    Debug.v("接收UDP客户端IP   UDPMulticastServer: receivedClientLocalIP=" + new String(receivedClientLocalIP));
                    Debug.v("接收UDP客户端IP   UDPMulticastServer: sendLocalIP=" + remoteIp);
                    //byte[] 长度不一样也会导致同一个数后面很多0，所以相同的数不相等
                    if (remoteIp.equals(sendLocalIP)) {
                        // 自定义事件（作为通信载体，可以发送数据）
                        MessageEvent messageEvent = new MessageEvent(sendLocalIP);
                        // 在任意线程里发布事件：EventBus.getDefault()为事件发布者，而post()为发布动作
                        fireOnReceived(messageEvent);
                        Debug.v("接收UDP客户端IP   UDPMulticastServer: 发送给LanActivity客户端IP成功");
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void stopThread(){
            running = false;
            receiveMulticast.close();
        }
    }

    private UdpPacketListener udpPacketListener;
    //udpMulticast.setUdpPacketListener(this);
    //获取上下文，（关联），因为实现了interface UdpPacketListener接口。所以LanActivity也是UdpPacketListener
    //的一个实现类,然后udpPacketListener就是LanActivity;
    public void setUdpPacketListener(UdpPacketListener listener) {
        //udpPacketListener=LanActivity;
        udpPacketListener = listener;
    }

    //fire 通知
    private void fireOnReceived(MessageEvent event) {
        if (null != udpPacketListener) {
            udpPacketListener.onReceived(event);
        }
    }

    //相当于UDPMuticast的内部类，这就类比于成员变量，那内部类实现的对象就为成员对象
    //现在关键就在于LanActivity也是UdpPacketListener的实现类，所以上面的setUdpPacketListener
    //方法中就相当于对象给对象赋值  udpPacketListener = listener;
    //这时候  udpPacketListener = LanActivity;
    //当fireOnReceived(messageEvent);时相当于LanActivity.onReceived(event);
    //相当于  LanActivity.serverIP = event.clientLocalIP;
    //什么叫接口，接口就是连接的门一样把两个类连起来

    //implement相当于给一个类移植了一个interface里定好的方法，其实也相当于自己写的方法
    public interface UdpPacketListener {
        void onReceived(MessageEvent event);
    }
}




