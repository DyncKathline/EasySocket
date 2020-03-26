package com.kathline.easysocket.core;


/**
 * Created by yaopeng(aponone@gmail.com) on 2018/11/2.
 */
public interface ConnectHandler{

    void connectSuccess();

    void connectFail();

    void disconnect();
}
