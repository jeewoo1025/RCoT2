package com.example.rcot20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Packet {
    static final int MAX_ARR_SIZE=128;

    static final int MAX_NET_SYNC_SIZE=4;
    static final int MAX_HEADER_SIZE=8;
    static final int MAX_BODY_HEADER_SIZE=4;
    static final int NET_SUB_DATA_HEADER_SIZE=4;
    static final int MAX_CRC_SIZE=2;
    static final int MAX_TAIL_SIZE=2;

    static final int NET_STX_CODE=0x16;
    static final int NET_ETX_CODE=0x03;

    static final int MIN_NET_SUBDATA_OFFSET=(MAX_NET_SYNC_SIZE+MAX_HEADER_SIZE+MAX_BODY_HEADER_SIZE);
    static final int MIN_NET_PKT_SIZE=(MAX_NET_SYNC_SIZE+MAX_HEADER_SIZE+MAX_BODY_HEADER_SIZE+MAX_CRC_SIZE+MAX_TAIL_SIZE);

    static final int NET_PKT_PURE_SIZE=(MAX_NET_SYNC_SIZE+MAX_HEADER_SIZE+MAX_TAIL_SIZE);

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    static final short NET_PKT_GROUP_COMMON=0x01;
    static final short NET_PKT_GROUP_REMOCON=0x02;

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    static final short NET_SUB_FLAGS_MASK=0XF0;
    static final short NET_SUB_FLAGS_REQUEST=0x10;
    static final short NET_SUB_FLAGS_RESPONSE=0x20;

    static final short NET_SUB_ERROR_MASK=0x0F;
    static final short NET_SUB_ERROR_NONE=0x00;
    static final short NET_SUB_ERROR_BUSY=0x01;
    static final short NET_SUB_ERROR_TIMEOUT=0x02;
    static final short NET_SUB_ERROR_EIO=0x03;

    /////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    static final short NET_SUB_MAJ_DISCOVERY=0x01;
    static final short NET_SUB_MIN_WHERE_ARE_YOU=0x01;
    static final short NET_SUB_MIN_HERE_I_AM=0x02;

    static final short NET_SUB_MAJ_PINGPONG=0x02;
    static final short NET_SUB_MIN_PING=0x01;
    static final short NET_SUB_MIN_PONG=0x02;

    /////////////////////////////////////////////////////////////////////////////////////

    static final short NET_SUB_MAJ_READ_KEY=0x03;
    static final short NET_SUB_MIN_REQ_READ=0x01;
    static final short NET_SUB_MIN_READ_DATA=0X02;

    static final short NET_SUB_MAJ_WRITE_KEY=0x04;
    static final short NET_SUB_MIN_WRITE_DATA=0x01;

    void intToBytes(int num, byte[] value) {
        // 원래 unsigned short --> int 로 .. 그래서 수의 범위가 절대로 0xffff를 넘지 않는다.. 그래서 가능...

        byte[] buf = new byte[4];
        int cnt = 0;

        buf[0] = (byte)((num&0xFF000000) >> 24);
        buf[1] = (byte)((num&0xFF0000) >> 16);
        buf[2] = (byte)((num&0xFF00) >> 8);
        buf[3] = (byte)((num&0xFF));

        for(int i = 0; i < value.length; i++)
            value[i] = buf[i+2];
    }

    byte shortToByte(short num) {
        byte value = 0;
        byte[] buf = new byte[2];

        buf[0] = (byte)((num&0xFF00) >> 8);
        buf[1] = (byte)((num&0xFF));

        value = buf[1];

        return value;
    }

    short bytesToShort(byte[] arr) {
        ByteBuffer buf = ByteBuffer.allocate(Short.SIZE/8);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.put(arr);
        buf.flip();

        return buf.getShort();
    }

    int bytesToInt(byte[] arr) {
        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE/8);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.put(arr);
        buf.flip();

        return buf.getInt();
    }
}
