package com.example.rcot20;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CommonNetPacket extends Packet {
    private int pktLen;                // packetArr의 실제 값들이 저장되있는 길이
    private byte[] packetArr;      // class 멤버변수들을 byte 배열에 저장

    // STX
    short[] stx;

    // HEADER
    int hLen;
    int hUniqid;
    int hSeqno;
    short hRetry;
    short hSum;

    // BODY
    short bGroupid;
    short bCount;
    int bLen;

    byte[] subData;

    CommonNetPacket() {
        pktLen = 0;
        packetArr = new byte[MAX_ARR_SIZE];  // 최대 128 byte 크기

        stx = new short[MAX_NET_SYNC_SIZE];
        hLen = 0;
        hUniqid = 0;
        hSeqno = 0;
        hRetry = 0;
        hSum = 0;
        bGroupid = 0;
        bCount = 0;
        bLen = 0;
        subData = null;
    }

    int getPacketLen() {
        return pktLen;
    }

    void setPacketLen(int size) {
        pktLen = size;
    }

    byte[] getPacketArr() {
        byte[] value = new byte[pktLen];
        System.arraycopy(packetArr, 0, value, 0, pktLen);

        return value;
    }

    void setPacketArr(byte[] arr) {
        if(arr == null || arr.length <= 0)
            return;

        int size = packetArr.length;
        if(arr.length < packetArr.length)
            size = arr.length;

        // arr의 0번째 값을 packetArr의 0번째에 size만큼 copy한다
        System.arraycopy(arr, 0, packetArr, 0, size);
    }

    void setSubData(CommonSubData sb) {
        // copy value using System.arraycopy
        sb.setSubDataLen();
        sb.setSubDataArr(sb.subDataToBytes(sb));         // 자기 자신의 데이터를 arr로 일단 변환
        byte[] object = sb.getSubDataArr();

        if(object != null) {
            subData = new byte[object.length + 4];
            // object의 0번 값을 subData의 0번 값으로 len만큼 복사
            System.arraycopy(object, 0, subData, 0, object.length);
        }
    }

    byte[] packetToBytes(CommonNetPacket pkt) {
        // class 멤버변수들을 byte arr 형식으로 바꾼다
        int i = 0;
        ByteBuffer buf = ByteBuffer.allocate(MAX_ARR_SIZE);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // (short)stx 배열 멤버들을 byte 값으로 저장
        for(i = 0; i < stx.length; i++)
            buf.put(shortToByte(pkt.stx[i]));     // (short)stx[i] 값을 byte로 형변환 시켜준다.

        buf.putShort((short)(pkt.hLen&0xffff));
        buf.putShort((short)(pkt.hUniqid & 0xffff));
        buf.putShort((short)(pkt.hSeqno & 0xffff));

        buf.put(shortToByte(pkt.hRetry));
        buf.put(shortToByte(pkt.hSum));
        buf.put(shortToByte(pkt.bGroupid));
        buf.put(shortToByte(pkt.bCount));

        buf.putShort((short)(pkt.bLen & 0xffff));

        if(pkt.subData != null)
            buf.put(pkt.subData);

        return buf.array();
    }

    void bytesToPacket(CommonNetPacket pkt, byte[] buf, int size, int off) {
        // recv한 패킷을 read한다. buf --> packet
        ByteBuffer arr = ByteBuffer.allocate(size);
        arr.order(ByteOrder.LITTLE_ENDIAN);

        byte[] temp = new byte[size];
        System.arraycopy(buf, off, temp, 0, size);

        arr.put(temp);
        arr.rewind();

        // short
        for(int i = 0; i < MAX_NET_SYNC_SIZE; i++)
            pkt.stx[i] = (short)(arr.get() & 0xff);

        // int
        pkt.hLen = (int)(arr.getShort() & 0xffff);
        pkt.hUniqid = (int)(arr.getShort() & 0xffff);
        pkt.hSeqno = (int)(arr.getShort() & 0xffff);

        // short
        pkt.hRetry = (short)(arr.get() & 0xff);
        pkt.hSum = (short)(arr.get() & 0xff);
        pkt.bGroupid = (short)(arr.get() & 0xff);
        pkt.bCount = (short)(arr.get() & 0xff);

        // int
        pkt.bLen = (int)(arr.getShort() & 0xffff);

        // byte[] subData ( flags, cmdMajor, cmdMinor, len, data[] )
        int pos = off + arr.position();
        int subDataLen = size - (NET_PKT_PURE_SIZE + MAX_BODY_HEADER_SIZE + MAX_CRC_SIZE);
        if(subDataLen >= NET_SUB_DATA_HEADER_SIZE) {
            pkt.subData = new byte[subDataLen + MAX_CRC_SIZE + MAX_TAIL_SIZE];
            System.arraycopy(buf, pos, pkt.subData, 0, subDataLen + MAX_CRC_SIZE + MAX_TAIL_SIZE);
        }

        pkt.setPacketLen(size);
        pkt.setPacketArr(buf);
    }

    int buildNetHeader(CommonNetPacket responsePkt, int pktLen, CommonNetPacket requestPkt) {
        int i = 0;
        int len = 0;
        int crc16 = 0;
        byte[] buf;

        for(i = 0; i < 4; i++)
            responsePkt.stx[i] = NET_STX_CODE;

        // header 정보 설정
        responsePkt.hLen = pktLen - (MAX_NET_SYNC_SIZE + MAX_HEADER_SIZE + MAX_TAIL_SIZE);
        if(requestPkt != null)
            responsePkt.hUniqid = requestPkt.hUniqid;

        // hLen ~ hRetry 의 byte sum
        responsePkt.hSum = 0;
        buf = packetToBytes(responsePkt);
        for(i = 0; i < 7; i++) {
            responsePkt.hSum += buf[i + 4];     // header 처음 : len
        }

        // body header 정보설정
        len = pktLen - (NET_PKT_PURE_SIZE + MAX_BODY_HEADER_SIZE + MAX_CRC_SIZE);

        if(requestPkt != null)
            responsePkt.bGroupid = requestPkt.bGroupid;
        responsePkt.bLen = len;

        // 다시 reponsePkt의 update
        buf =  packetToBytes(responsePkt);
        // body crc 의 범위 : body의 bGroupid ~ body의 subData
        int offset = MAX_NET_SYNC_SIZE + MAX_HEADER_SIZE;
        byte[] value = new byte[len + MAX_BODY_HEADER_SIZE];
        System.arraycopy(buf, offset, value, 0, value.length);
        crc16 = CRC.getCrc(value, value.length);

        responsePkt.subData[len + 0] = (byte)(crc16 & 0xff);
        responsePkt.subData[len + 1] = (byte)((crc16 >> 8) & 0xff);

        // tail etx code 설정
        responsePkt.subData[len + 2] = NET_ETX_CODE;
        responsePkt.subData[len + 3] = NET_ETX_CODE;

        // 전체 packet length를 return
        len = NET_PKT_PURE_SIZE + (MAX_BODY_HEADER_SIZE + len + MAX_CRC_SIZE);

        return len;
    }

    public int checkVaildNetsvcPacket(byte[] buf, int len) {
        int i, value, crc16;
        CommonNetPacket pkt = new CommonNetPacket();

        pkt.bytesToPacket(pkt, buf, len, 0);

        // check STX & ETX
        value = 0;
        for(i = 0; i < MAX_NET_SYNC_SIZE; i++)
            if(buf[i] == NET_STX_CODE)
                value++;
        if(value != MAX_NET_SYNC_SIZE) {
            Log.e("에러", "checkValidNetsvcPacket : invalid stx count");
            return 1;
        }

        value = 0;
        for(i = 0; i < MAX_TAIL_SIZE; i++)
            if(buf[len - MAX_TAIL_SIZE  + i] == NET_ETX_CODE)
                value++;
        if(value != MAX_TAIL_SIZE) {
            Log.e("에러", "checkValidNetsvcPacket : invalid etx code");
            return 1;
        }

        if(len != (NET_PKT_PURE_SIZE + pkt.hLen)) {
            Log.e("에러", String.format("checkVaildNetsvcPacket : invalid pkt Len %d != (%d + %d)", len, NET_PKT_PURE_SIZE, pkt.hLen));
            return 1;
        }

        // check header sum
        value = 0;
        for(i = 0; i < 7; i++)
            value += buf[4 + i];
        value &= 0xff;
        if(value != pkt.hSum) {
            Log.e("에러", String.format("checkVaildNetsvcPacket : invalid header sum %X = %X", value, pkt.hSum));
            return 1;
        }

        // check body crc
        byte[] checkArr = new byte[pkt.hLen - MAX_CRC_SIZE];
        int bGroupIdOff = MAX_NET_SYNC_SIZE + MAX_HEADER_SIZE;
        System.arraycopy(buf, bGroupIdOff, checkArr, 0, checkArr.length);
        crc16 = CRC.getCrc(checkArr, checkArr.length);

        value = (buf[len - MAX_CRC_SIZE - MAX_TAIL_SIZE + 1] & 0xff);
        value <<= 8;
        value |= (buf[len - MAX_CRC_SIZE - MAX_TAIL_SIZE + 0] & 0xff);

        if(value != crc16) {
            Log.e("에러", String.format("checkVaildNetsvcPacket : invalid value %d != %d", value, crc16));
            return 1;
        }

        ////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////
        if(pkt.bGroupid != NET_PKT_GROUP_COMMON && pkt.bGroupid != NET_PKT_GROUP_REMOCON) {
            Log.e("에러", String.format("checkValidNetsvcPacket : invalid groupid %d", pkt.bGroupid));
            return 1;
        }

        if(pkt.bCount <= 0) {
            Log.e("에러", String.format("checkValidNetsvcPacket : invalid subdata count : %d", pkt.bCount));
            return 1;
        }

        return 0;
    }

    public int checkValidPacket(byte[] buf, int size, Misc.Offset offset) {
        int i;
        int pktLen;
        int pktOff;

        int stxCnt;
        CommonNetPacket recvPkt = new CommonNetPacket();
        int calcChksum;
        int bodySize;
        int calcCrc, pktCrc;
        int subDataSize;

        stxCnt = 0;
        pktOff = 0;
        pktLen = size;

        if(offset != null)
            offset.setOff(0);

        if(buf == null || size <= 0)
            return -1;

        for(i = 0; i < size; i++) {
            // 1. STX(SYN) Code를 찾고
            if(stxCnt < MAX_NET_SYNC_SIZE) {
                if(buf[i] == NET_STX_CODE) {
                    if(stxCnt == 0) {
                        // start position for real packet
                        recvPkt.bytesToPacket(recvPkt, buf, size, i);
                        pktLen -= i;
                        pktOff = i;
                    }
                    stxCnt++;
                }
                else {
                    if(stxCnt > 0)
                        Log.d("패킷", "checkValidPacket에서 에러");
                    stxCnt = 0;
                }
            }
            if(stxCnt == MAX_NET_SYNC_SIZE)
                break;
        }

        if(pktOff > 0 && offset != null)
            offset.setOff(pktOff);

        if(stxCnt < MAX_NET_SYNC_SIZE)
            return 0;

        if(pktLen < MIN_NET_PKT_SIZE)      // 20
            return 0;

        // 2. check checksum
        byte[] header = new byte[MAX_HEADER_SIZE - 1];
        System.arraycopy(buf, pktOff + MAX_NET_SYNC_SIZE, header, 0, header.length);

        calcChksum = 0;
        for(int j = 0; j < (MAX_HEADER_SIZE-1); j++)
            calcChksum += header[j];
        calcChksum &= 0xff;

        if(recvPkt.hSum != calcChksum) {
            if(offset != null)
                offset.setOff(pktOff + MIN_NET_PKT_SIZE);
            return -1;
        }

        // 3. check full packet is received
        bodySize = recvPkt.hLen;        // body의 length

        if(pktLen > (MAX_NET_SYNC_SIZE + MAX_HEADER_SIZE + MAX_BODY_HEADER_SIZE)) {
            int calcBodySize;

            // 4. check full packet is received
            subDataSize = recvPkt.bLen;     // subData의 length

            calcBodySize = MAX_BODY_HEADER_SIZE + subDataSize + MAX_CRC_SIZE;

            if(bodySize != calcBodySize) {
                Log.d("패킷", "checkValidPacket에서 에러...");

                if(offset != null)
                    offset.setOff(pktOff + pktLen);
                return -1;
            }
        }

        if(pktLen < (MAX_NET_SYNC_SIZE + MAX_HEADER_SIZE + bodySize + MAX_TAIL_SIZE)) {
            // more data is needed.....
            return 0;
        }

        pktLen = MAX_NET_SYNC_SIZE + MAX_HEADER_SIZE + bodySize + MAX_TAIL_SIZE;

        // 4. check CRC
        byte[] checkArr = new byte[bodySize - MAX_CRC_SIZE];
        int bGroupIdOff = MAX_NET_SYNC_SIZE + MAX_HEADER_SIZE + pktOff;
        System.arraycopy(buf, bGroupIdOff, checkArr, 0, checkArr.length);
        calcCrc = CRC.getCrc(checkArr, checkArr.length);

        pktCrc = recvPkt.subData[bodySize - MAX_BODY_HEADER_SIZE - MAX_CRC_SIZE + 1] & 0xff;
        pktCrc <<= 8;
        pktCrc |= recvPkt.subData[bodySize - MAX_BODY_HEADER_SIZE - MAX_CRC_SIZE + 0] & 0xff;

        if(calcCrc != pktCrc) {
            Log.d("패킷", "invalid crc");
            if(offset != null)
                offset.setOff(pktOff + pktLen);
            return -1;
        }

        if(recvPkt.subData[bodySize - MAX_BODY_HEADER_SIZE] != NET_ETX_CODE
                || recvPkt.subData[bodySize - MAX_BODY_HEADER_SIZE + 1] != NET_ETX_CODE) {
            Log.d("패킷", "invalid etx");
            if(offset != null) {
                offset.setOff(pktOff + pktLen);
                return -1;
            }
        }

        return pktLen;
    }
}
