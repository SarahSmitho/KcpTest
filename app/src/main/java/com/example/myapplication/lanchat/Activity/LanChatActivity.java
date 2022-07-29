package com.example.myapplication.lanchat.Activity;

import static com.example.myapplication.lanchat.Bean.Msg.IMAGE;
import static com.example.myapplication.lanchat.Bean.Msg.TEXT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.myapplication.R;
import com.example.myapplication.lanchat.Adapter.MsgAdapter;
import com.example.myapplication.lanchat.Bean.Msg;
import com.example.myapplication.lanchat.Event.MessageEvent;
import com.example.myapplication.lanchat.Util.SDUtil;
import com.example.myapplication.lanchat.socket.TCP;
import com.example.myapplication.lanchat.socket.UDPMulticast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import io.noties.debug.Debug;

public class LanChatActivity extends AppCompatActivity implements UDPMulticast.UdpPacketListener {

    private static final String TAG = "LanChatActivity";
    private static LanChatActivity instance;

    private static final int WRITE_EXTERNAL_STORAGE = 101;
    private static final int CHOOSE_PHOTO = 1;

    private String serverIP;

    //消息输入框
    private EditText editText;
    //输入框监听
    private TextWatcher textWatcher;
    //发送按钮
    private Button sendBt;
    //发送图片
    private Button sendImageBt;

    private ListView msgListView;
    private List<Msg> msgList = new ArrayList<Msg>();
    private MsgAdapter msgAdapter;

    WifiManager.MulticastLock multicastLock;
    private UDPMulticast udpMulticast;
    private TCP tcp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_chat);
        //创建上下文全局变量
        instance = this;

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifiManager.createMulticastLock("chat");
        multicastLock.acquire();

        //开启UDP组播
        try {
            udpMulticast = new UDPMulticast();
            //目的地
            udpMulticast.setUdpPacketListener(this);
            udpMulticast.start();
            Log.d(TAG, "接收UDP客户端IP  onClick: 生成udp对象");
        } catch (SocketException e) {
            e.printStackTrace();
        }


        //监听消息输入框
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editText.getText().toString())) {
                    sendBt.setClickable(true);
                } else {
                    sendBt.setClickable(false);
                }
            }
        };


        initView();

     /*   handler = new Handler();*/
        //发送文字消息按钮的点击事件
        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sendContent = editText.getText().toString();
                if (!TextUtils.isEmpty(sendContent)) {
                    int length = sendContent.length();
                    Msg msg = new Msg(Msg.SENT, TEXT, length, sendContent);
                    //给tcp里队列增加msg对象
                    Log.d(TAG, "TCP客户端发送消息   onClick: 开始发送文字");
                    //往TCP队列里添加msg消息
                    tcp.sendMsg(msg);
                    Debug.v("TCP客户端发送消息  onClick: 往TCP队列里添加msg消息" );
                    //把发送的消息放到消息List里
                    msgList.add(msg);
                    //有新消息时，刷新ListView中的显示
                    msgAdapter.notifyDataSetInvalidated();
                    //将ListView定位到最后一行
                    msgListView.setSelection(msgList.size());

                }
                editText.setText("");
            }
        });

        // 点击发送图片按钮，选择图片
        sendImageBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //动态申请获取访问 读写磁盘的权限,也可以在注册文件上直接注册
                    if (ContextCompat.checkSelfPermission(LanChatActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(LanChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);

                    } else {
                        Intent intent = new Intent("android.intent.action.GET_CONTENT");
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_PHOTO);
                    }
                } else {
                    Intent intent = new Intent("android.intent.action.GET_CONTENT");
                    intent.setType("image/*");
                    startActivityForResult(intent, CHOOSE_PHOTO);
                }
                Log.d(TAG, "客户端发送图片过程  onClick: 选择图片成功");

            }


        });

        //EventBus注册,subscriber，订阅者为该Activity
        EventBus.getDefault().register(this);
    }

    public static LanChatActivity getInstance(){
        return instance;
    }

    /**
     * 初始化视图界面
     */
    private void initView() {
        editText = findViewById(R.id.et_send_content);
        sendBt = findViewById(R.id.btn_send);
        sendImageBt = findViewById(R.id.btn_image);
        editText.addTextChangedListener(textWatcher);
        // 初始化消息数据
        msgAdapter = new MsgAdapter(LanChatActivity.this, R.layout.msg_item, msgList);
        msgListView = findViewById(R.id.msg_list_view);
        msgListView.setAdapter(msgAdapter);
        // 一开始消息输入框内容为空，设置为不可点击
        sendBt.setClickable(false);

        try {
            String s=serverIP;
            Log.d(TAG, "initView:= "+serverIP);
            tcp=new TCP(serverIP);
            Log.d(TAG, "initView: 生成TCP对象成功="+serverIP);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 事件处理（真正的处理逻辑），收到消息（事件）后做处理
     * MAIN：表示事件处理函数的线程在主线程（UI）线程，因此在这里不能进行耗时操作
     * 这里主要做一些UI的更新
     */
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true,priority = 1)
    public void showWords(MessageEvent messageEvent) {
        Log.d(TAG, "TCP客户端接收消息   showWords: 开始接收事件," + messageEvent.getType());
        if (!TextUtils.isEmpty(messageEvent.getWords()) || messageEvent.getImage() != null) {
            // 判断接收的消息类型,魔法值，0为文字，1为图片
            if (messageEvent.getType() == TEXT) {
                Msg msg = new Msg(Msg.RECEIVED, TEXT, messageEvent.getLength(), messageEvent.getWords());
                Log.d(TAG, "TCP客户端接收消息   showWords: ="+msg.getContent());
                msgList.add(msg);
                msgAdapter.notifyDataSetChanged();//有新消息时，刷新ListView中的显示
                msgListView.setSelection(msgList.size());//将ListView定位到最后一行
                Log.d(TAG, "TCP客户端接收消息   showWords: 添加进msgList成功");
            } else if (messageEvent.getType() == IMAGE) {
                Log.d(TAG, "TCP服务端接收消息  图片情形  showWords: image size=" + (messageEvent.getImage().length));
                Bitmap bitmap = BitmapFactory.decodeByteArray(messageEvent.getImage(), 0, messageEvent.getImage().length);
                Log.d("TCP服务端接收消息  图片情形", "showWords: bitmap=" + bitmap);
                Msg msg = new Msg(Msg.RECEIVED, Msg.IMAGE,messageEvent.getLength(), bitmap);
                Log.d(TAG, "TCP服务端接收消息  图片情形  showWords: 显示图片,bitmap=" + (bitmap != null));
                msgList.add(msg);
                msgAdapter.notifyDataSetChanged();//有新消息时，刷新ListView中的显示
                msgListView.setSelection(msgList.size());//将ListView定位到最后一行
            }

        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                Debug.v("客户端发送图片过程   选择图片");
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    final String path = SDUtil.getFilePathByUri(LanChatActivity.this, data.getData());
                    if (!TextUtils.isEmpty(imageUri.toString())) {

                        File file = new File(path);
                        FileInputStream fileInputStream = null;
                        try {
                            fileInputStream= new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        int imageLength = 0;
                        try {
                            imageLength = fileInputStream.available();
                            Debug.v("客户端发送图片过程   imageLength="+imageLength);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        byte[] imageData = new byte[imageLength];
                        try {
                            while (fileInputStream.read(imageData)!=-1);
                            Debug.v("客户端发送图片过程   imageData接收数据完毕");
                            Debug.v("客户端发送图片过程   imageData="+imageData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        Debug.v("客户端发送图片过程   imageUri.toString()="+imageUri.toString());
                        Msg imageMsg = new Msg(Msg.SENT, Msg.IMAGE, imageLength, imageData,imageUri);
                        Debug.v("客户端发送图片过程   生成Msg对象成功");
                        Debug.v("客户端发送图片过程 imageMsg= "+imageMsg.getContentType());
                        tcp.sendMsg(imageMsg);
                        //我们
                        //Msg msg = new Msg(Msg.SENT, Msg.IMAGE, imageLength, imageUri);
                        msgList.add(imageMsg);
                        Log.d(TAG, "客户端发送图片过程   onActivityResult:将生成Msg对象放入msgList中 ");
                        //有新消息时，刷新ListView中的显示
                        msgAdapter.notifyDataSetChanged();
                        //将ListView定位到最后一行
                        msgListView.setSelection(msgList.size());
                    }
                }
                Debug.v("客户端发送图片过程   准备跳出switch");
                break;
            default:
                break;
        }
        Debug.v("客户端发送图片过程   跳出switch成功");
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            tcp.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //开启TCP发送和接收线程
    @Override
    protected void onResume() {

        super.onResume();


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭socket连接，避免端口被持续占用
        udpMulticast.stop();

        try {
            tcp.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //EventBus解注册，取消订阅
        EventBus.getDefault().unregister(this);

        if (null == multicastLock) {
            multicastLock.release();
            multicastLock = null;
        }
    }

    @Override
    public void onReceived(MessageEvent event) {
        serverIP = event.clientLocalIP;
        Debug.v("received packet from:%s", serverIP);
    }
}