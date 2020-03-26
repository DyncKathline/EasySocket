package com.kathline.easysocket.core;

import java.util.List;

public class MessageHandlerWrap implements MessageHandler,Disposable{

    private boolean isDisposed = false;

    private List list;

    private MessageHandler messageHandler;

    public MessageHandlerWrap(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }


    public void addTo(List list){
        this.list = list;
        this.list.add(this);
    }

    @Override
    public void dispose() {
        if(!isDisposed()){
            this.isDisposed = true;
            this.list.remove(this);
            this.list = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    @Override
    public void receive(byte[] data) {
        if (messageHandler != null) {
            messageHandler.receive(data);
        }

    }
}
