package com.kathline.easysocket.protocols;

import android.os.SystemClock;

import com.kathline.easysocket.core.Protocols;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 没有经过任何处理，可能会出现粘包问题
 */
public class BaseProtocols implements Protocols {

    public static BaseProtocols create(){
        return new BaseProtocols();
    }

    public BaseProtocols() {
    }

    @Override
    public byte[] unpack(InputStream inputStream) throws IOException {
        int available = inputStream.available();
        if (available > 0) {
            byte[] buffer = new byte[available];
            int size = inputStream.read(buffer);
            if (size > 0) {
                return buffer;
            }
        } else {
            SystemClock.sleep(50);
        }
        return null;
    }

    @Override
    public byte[] pack(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(data.length);
        bb.put(data);
        return bb.array();
    }
}

