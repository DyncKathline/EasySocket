package com.kathline.easysocket.core;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * Created by yaopeng(aponone@gmail.com) on 2018/11/1.
 */
public class NetworkExecutor{

    private final static String TAG = NetworkExecutor.class.getSimpleName();

    private BlockingQueue<RequestPacket> mRequestQueue = new LinkedBlockingDeque<>();

    private List<MessageHandlerWrap> mMessageHandlerWrapList = new ArrayList<>();

    private List<ConnectHandlerWrap> mConnectHandlerList = new ArrayList<>();

    private Client mClient;

    private Protocols mProtocols;

    private int mPingInterval;

    private byte[] mPingData;

    private boolean isSaveOffLineMsg;

    private Thread sendThread;

    private Thread receiveThread;

    private Thread pingThread;

    private boolean isForceStop = false;

    private static Schedulers mSchedulers = new Schedulers();
    private Thread connectThread;


    public NetworkExecutor(Client mClient, Protocols mProtocols,int mPingInterval, byte[] mPingData, boolean isSaveOffLineMsg) {
        this.mClient = mClient;
        this.mProtocols = mProtocols;
        this.mPingInterval = mPingInterval;
        this.mPingData = mPingData;
        this.isSaveOffLineMsg = isSaveOffLineMsg;
    }

    public void connect(){
        if (mClient.isDisconnected()){
            isForceStop = false;
            connectThread = new Thread(connectRunnable, "ConnectThread");
            connectThread.start();
        }
    }

    public void disconnect(){
        if (mClient.isConnected()){
            isForceStop = true;
            mClient.disconnect();
            connectHandler(3);
            connectThread.interrupt();
        }
    }

    public void send(byte[] data){

        RequestPacket packet = new RequestPacket(data);
        try {
            getRequestQueue().put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disconnectForError(){
        disconnect();
    }

    public List<MessageHandlerWrap> getResponseHandlerList() {
        return mMessageHandlerWrapList;
    }

    public BlockingQueue<RequestPacket> getRequestQueue() {
        return mRequestQueue;
    }

    public List<ConnectHandlerWrap> getConnectHandlerList() {
        return mConnectHandlerList;
    }

    public void connectHandler(int status){
        for (int i=getConnectHandlerList().size()-1;i>=0;i--){
            ConnectHandlerWrap connectHandler = getConnectHandlerList().get(i);
            if (!connectHandler.isDisposed()){

                switch (status){
                    case 1:
                        mSchedulers.connectSuccess(connectHandler);
                        break;
                    case 2:
                        mSchedulers.connectFail(connectHandler);
                        break;
                    case 3:
                        mSchedulers.disconnect(connectHandler);
                        break;
                }
            }
        }
    }

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            mClient.connect();
            LogUtil.d(Thread.currentThread().getName()+": =======开启连接线程========");

            if (mClient.isConnected()){
                if(!isSaveOffLineMsg) {
                    mRequestQueue.clear();
                }
                receiveThread = new Thread(receiveRunnable,"ReceiveThread");
                receiveThread.start();
                sendThread = new Thread(sendRunnable,"SendThread");
                sendThread.start();
                pingThread = new Thread(pingRunnable,"PingThread");

                if (mPingInterval>0){
                    pingThread.start();
                }

                connectHandler(1);
            }else {
                connectHandler(2);
            }
        }
    };
    /**
     * 接收数据线程
     */
    private Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(Thread.currentThread().getName()+": =======开启接收数据线程========");
//            InputStream in = null;


            Socket socket = mClient.getSocket();
            while (mClient.isConnected()) {
                if(!socket.isClosed()&&!socket.isInputShutdown()){
                    try {
                        InputStream in = socket.getInputStream();
                        receiveData(in);
                    } catch (IOException e) {
//                        e.printStackTrace();
                        LogUtil.e("=======断开连接========");
                        disconnectForError();
                    }
                }
            }
            LogUtil.d(Thread.currentThread().getName()+": =======退出接收数据线程========");
            sendThread.interrupt();//退出发送线程
            pingThread.interrupt();//退出心跳线程
        }
    };

    private void receiveData(InputStream in) throws IOException{

        byte[] buf = mProtocols.unpack(in);
        if (buf!=null){
            LogUtil.d(Thread.currentThread().getName()+": =======接收数据转发给"+ mMessageHandlerWrapList.size()+"个监听器=======:数据大小:"+buf.length);
            for (int i = mMessageHandlerWrapList.size()-1; i >=0 ; i--) {
                MessageHandlerWrap messageHandlerWrap = mMessageHandlerWrapList.get(i);
                if (!messageHandlerWrap.isDisposed()){
                    mSchedulers.messageReceive(messageHandlerWrap,buf);
                }
            }
        }
    }

    /**
     * 发送数据线程
     */
    private Runnable sendRunnable = new Runnable() {

        @Override
        public void run() {
            LogUtil.d(Thread.currentThread().getName()+": =======开启发送数据线程========");
            DataOutputStream dataOutputStream = null;
            try {
                Socket socket = mClient.getSocket();
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                while (!Thread.currentThread().isInterrupted()){
                    RequestPacket packet = mRequestQueue.take();
                    byte[] data = mProtocols.pack(packet.getData());
                    dataOutputStream.write(data);
                    dataOutputStream.flush();
                    LogUtil.d(Thread.currentThread().getName()+": =======发送数据========:数据大小:"+data.length);

                }


            } catch (IOException e) {
                e.printStackTrace();
                disconnectForError();
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }finally {
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.flush();
                        //            dataOutputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            LogUtil.d(Thread.currentThread().getName()+": =======退出发送数据线程========");
        }
    };
    /**
     * 发送心跳包线程
     */
    private Runnable pingRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(Thread.currentThread().getName()+": =======开启心跳线程========");
            RequestPacket packet = new RequestPacket(mPingData);
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    LogUtil.d(Thread.currentThread().getName()+": =======发送心跳包========:数据大小:"+mPingData.length);
                    getRequestQueue().put(packet);
                    Thread.sleep(mPingInterval*1000);
                }
            }catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }
            LogUtil.d(Thread.currentThread().getName()+": =======退出心跳线程========");
        }
    };

}
