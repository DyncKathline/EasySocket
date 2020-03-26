package com.kathline.easysocket.core;

public class RequestPacket {

    private byte[] data;

    public RequestPacket(byte[] data) {
        this.data = data;
    }

    byte[] getData(){
        return this.data;
    }

}
