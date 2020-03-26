package com.kathline.easysocket.core;

import java.io.IOException;
import java.io.InputStream;

public interface Protocols {

    /**
     * 分包
     * @param inputStream
     * @return
     * @throws IOException
     */
    byte[] unpack(InputStream inputStream) throws IOException;
    /**
     * 打包 如：加包头
     * @param data
     * @return
     */
    byte[] pack(byte[] data);


}
