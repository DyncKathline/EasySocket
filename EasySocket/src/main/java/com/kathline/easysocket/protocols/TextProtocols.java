package com.kathline.easysocket.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kathline.easysocket.core.Protocols;

/**
 * 协议格式为 数据包+换行符，即在每个数据包末尾加上一个换行符表示包的结束。
 */
public class TextProtocols implements Protocols {

    private List<Byte> bytes;

    private byte[] tail = "\n".getBytes();

    public static TextProtocols create(){
        return new TextProtocols();
    }

    public TextProtocols() {
        this.bytes = new ArrayList<>();
    }

    @Override
    public byte[] unpack(InputStream inputStream) throws IOException {
        bytes.clear();
        int len;
        byte temp;
        byte[] result = null;

        while ((len = inputStream.read()) != -1) {
            temp = (byte) len;
            bytes.add(temp);
            Byte[] byteArray = bytes.toArray(new Byte[]{});
            if (endWith(byteArray,tail)){
                result = getRangeBytes(bytes,0,bytes.size() - tail.length);
                break;
            }
        }

        if (len == -1){
            throw new IOException();
        }

        return result;
    }

    private boolean endWith(Byte[] src, byte[] target) {
        if (src.length == 0 || src.length < target.length) {
            return false;
        }
        for (int i = 0; i < target.length; i++) {//逆序比较
            if (target[target.length - i - 1] != src[src.length - i - 1]) {
                return false;
            }
        }
        return true;
    }

    private byte[] getRangeBytes(List<Byte> list, int start, int end) {
        Byte[] temps = Arrays.copyOfRange(list.toArray(new Byte[0]), start, end);
        byte[] result = new byte[temps.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = temps[i];
        }
        return result;
    }

    @Override
    public byte[] pack(byte[] data) {
        ByteBuffer bb = ByteBuffer.allocate(tail.length + data.length);
        bb.put(data);
        bb.put(tail);
        return bb.array();
    }
}
