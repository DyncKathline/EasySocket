package com.kathline.lemonnet;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.kathline.easysocket.core.ConnectHandler;
import com.kathline.easysocket.core.Disposable;
import com.kathline.easysocket.EasySocket;
import com.kathline.easysocket.core.MessageHandler;
import com.kathline.easysocket.core.SocketClient;
import com.kathline.easysocket.protocols.TextProtocols;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private EasySocket easySocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SocketClient client = new SocketClient.Builder()
                .ip("192.168.31.117")
                .port(8000)
                .setKeepAlive(true)
                .build();
        String pingData = "{\"type\":\"ping\"}";
        easySocket = new EasySocket.Builder()
                .client(client)
                .protocols(TextProtocols.create())
                .pingInterval(0)
                .pingData(pingData.getBytes())
                .isSaveOffLineMsg(false)
                .debug(true)
                .build();

        easySocket.onConnect(new ConnectHandler() {

            @Override
            public void connectSuccess() {
                Log.d(TAG,"===连接成功===");
                easySocket.send("连接成功");
                Toast.makeText(MainActivity.this, "连接成功!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connectFail() {
                Log.d(TAG,"===连接失败===");
                easySocket.connect();
            }

            @Override
            public void disconnect() {
                Log.d(TAG,"===连接断开===");
                easySocket.connect();
            }

        });
        final Disposable disposable = easySocket.onMessage(new MessageHandler() {
            @Override
            public void receive(byte[] data) {
                Log.d(TAG,"====1.数据监听====: "+new String(data));
            }
        });
//        easySocket.onMessage(new MessageHandler() {
//            @Override
//            public void receive(byte[] data) {
//                Log.d(TAG,"====2.数据监听====: "+new String(data));
//            }
//        });
        findViewById(R.id.start_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easySocket.connect();
            }
        });
        findViewById(R.id.stop_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easySocket.disconnect();
            }
        });

        findViewById(R.id.send_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str = "hello apon！";

                easySocket.send(str);
            }
        });

        findViewById(R.id.disposable_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disposable.dispose();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        easySocket.destroy();
    }
}
