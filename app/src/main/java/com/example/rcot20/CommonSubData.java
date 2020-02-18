package com.example.rcot20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CommonSubData extends Packet {
    private int subDataLen;
    private byte[] subDataArr;

    short flags;
    short cmdMajor;
    short cmdMinor;
    short len;
    short value[];

    CommonSubData() {
        subDataLen = 0;
        subDataArr = null;

        flags = 0;
        cmdMajor = 0;
        cmdMinor = 0;
        len = 0;
        value = null;
    }

    int getSubDataLen() {
        return subDataLen;
    }

    void setSubDataLen() {
        subDataLen = NET_SUB_DATA_HEADER_SIZE;

        if(value != null)
            subDataLen += value.length;
    }

    byte[] getSubDataArr() {
        return subDataArr;
    }

    void setSubDataArr(byte[] arr) {
        subDataArr =  new byte[arr.length];
        System.arraycopy(arr, 0, subDataArr, 0, arr.length);
    }

    int bytesToSubData(CommonSubData sb, byte[] arr) {
        if(arr == null || arr.length < NET_SUB_DATA_HEADER_SIZE)
            return -1;

        ByteBuffer buf = ByteBuffer.allocate(arr.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.put(arr);
        buf.rewind();

        sb.flags = (short)(buf.get() & 0xff);
        sb.cmdMajor = (short)(buf.get() & 0xff);
        sb.cmdMinor = (short)(buf.get() & 0xff);
        sb.len = (short)(buf.get() & 0xff);

        if(buf.position() == NET_SUB_DATA_HEADER_SIZE) {
            int size = arr.length - NET_SUB_DATA_HEADER_SIZE;
            value = new short[size];

            int i = 0;
            while(buf.hasRemaining()) {
                value[i++] = (short)(buf.get() & 0xff);
            }
        }

        sb.setSubDataLen();
        sb.setSubDataArr(arr);

        return 0;
    }

    byte[] subDataToBytes(CommonSubData sb) {
        byte[] temp = new byte[2];
        ByteBuffer buf = ByteBuffer.allocate(sb.getSubDataLen());

        buf.put(shortToByte(sb.flags));
        buf.put(shortToByte(sb.cmdMajor));
        buf.put(shortToByte(sb.cmdMinor));
        buf.put(shortToByte(sb.len));

        if(sb.value != null) {
            for(int i = 0; i < sb.value.length; i++)
                buf.put(shortToByte(sb.value[i]));
        }

        return buf.array();
    }
}
