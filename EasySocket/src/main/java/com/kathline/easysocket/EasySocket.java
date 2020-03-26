package com.kathline.easysocket;

import com.kathline.easysocket.core.Client;
import com.kathline.easysocket.core.ConnectHandler;
import com.kathline.easysocket.core.ConnectHandlerWrap;
import com.kathline.easysocket.core.Disposable;
import com.kathline.easysocket.core.LogUtil;
import com.kathline.easysocket.core.MessageHandler;
import com.kathline.easysocket.core.MessageHandlerWrap;
import com.kathline.easysocket.core.NetworkExecutor;
import com.kathline.easysocket.core.Protocols;

import java.nio.charset.Charset;

public class EasySocket {

    public static final class Builder {


        private Client mClient;

        private Protocols mProtocols;

        private byte[] mPingData;

        private int mPingInterval = 0;

        private boolean isSaveOffLineMsg = true;

        public Builder client(Client client){
            this.mClient = client;
            return this;
        }

        public Builder protocols(Protocols protocols){
            this.mProtocols = protocols;
            return this;
        }

        public Builder pingData(byte[] data){
            this.mPingData = data;
            return this;
        }

        public Builder pingInterval(int pingInterval){
            this.mPingInterval = pingInterval;
            return this;
        }

        public Builder debug(boolean debug){
            LogUtil.debug(debug);
            return this;
        }

        public Builder isSaveOffLineMsg(boolean isSaveOffLineMsg) {
            this.isSaveOffLineMsg = isSaveOffLineMsg;
            return this;
        }

        public EasySocket build() {
            return new EasySocket(this.mClient,this.mProtocols,this.mPingInterval,this.mPingData,this.isSaveOffLineMsg);
        }
    }


    private NetworkExecutor mNetworkExecutor;

    public EasySocket(Client client, Protocols protocols, int pingInterval, byte[] pingData, boolean isSaveOffLineMsg) {
        this.mNetworkExecutor = new NetworkExecutor(client,protocols,pingInterval,pingData, isSaveOffLineMsg);
    }

    public Disposable onMessage(MessageHandler handler){
        MessageHandlerWrap packet = new MessageHandlerWrap(handler);
        packet.addTo(mNetworkExecutor.getResponseHandlerList());
        return packet;
    }

    public Disposable onConnect(ConnectHandler connectHandler){
        ConnectHandlerWrap connectHandlerWrap = new ConnectHandlerWrap(connectHandler);
        connectHandlerWrap.addTo(mNetworkExecutor.getConnectHandlerList());
        return connectHandlerWrap;
    }

    public void send(String msg){
        send(msg.getBytes(Charset.defaultCharset()));
    }

    public void send(byte[] data){

        mNetworkExecutor.send(data);

    }

    public void connect(){
        mNetworkExecutor.connect();
    }

    public void disconnect(){
        mNetworkExecutor.disconnect();
    }

    public void destroy() {
        mNetworkExecutor.destroy();
    }
}
