package com.kathline.easysocket.core;

import java.net.Socket;

public interface Client {


    Socket getSocket();

    void connect();

    void disconnect();

    boolean isConnected();

    boolean isDisconnected();

    boolean isConnecting();
}
