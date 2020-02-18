package com.example.rcot20;

import android.util.Log;

public class Netsvc {
    public CommonNetPacket sendNetsvcService(boolean readCmd, int[] code, int codeCnt) {
        CommonNetPacket pkt = new CommonNetPacket();
        CommonSubData sb = new CommonSubData();
        int pktSize;
        int bodySize;
        int subDataSize;
        int i;

        sb.setSubDataLen();
        subDataSize = sb.getSubDataLen() + codeCnt;        // 4 + 3

        bodySize = Packet.MAX_BODY_HEADER_SIZE + subDataSize + Packet.MAX_CRC_SIZE;     // 4 + 7 + 2
        pktSize = Packet.NET_PKT_PURE_SIZE + bodySize;      // 4 + 8 + 2 + 13
        pkt.setPacketLen(pktSize);

        //////////////////////////////////////////////////////////////////////////// build subData
        pkt.hUniqid = 0x1234;
        pkt.bGroupid = Packet.NET_PKT_GROUP_REMOCON;
        pkt.bCount = 1;

        if(readCmd) {
            sb.cmdMajor = Packet.NET_SUB_MAJ_READ_KEY;
            sb.cmdMinor = Packet.NET_SUB_MIN_REQ_READ;
        } else {
            sb.cmdMajor = Packet.NET_SUB_MAJ_WRITE_KEY;
            sb.cmdMinor = Packet.NET_SUB_MIN_WRITE_DATA;
        }
        sb.flags = Packet.NET_SUB_FLAGS_REQUEST;
        sb.len = (short)(codeCnt & 0xff);

        sb.value = new short[codeCnt];
        if(codeCnt > 0) {
            for(i = 0; i < codeCnt; i++)
                sb.value[i] = (short)(code[i] & 0xff);
        }
        pkt.setSubData(sb);

        //////////////////////////////////////////////////////////////////////////// build remained info
        i = pkt.buildNetHeader(pkt, pktSize, null);

        if(pktSize != i) {
            Log.e("패킷", String.format("Netsvc의 sendService에서 i(%d) != pktSize(%d)", i, pktSize));
            return null;
        }

        // packet class --> byte 배열
        pkt.setPacketLen(pktSize);
        pkt.setPacketArr(pkt.packetToBytes(pkt));

        return pkt;
    }

    public int netsvcReadDataService(byte[] buf, int len, int[] code) {
        CommonNetPacket pkt = new CommonNetPacket();
        CommonSubData sb = new CommonSubData();
        int re;
        int i,j;
        int subDataSize;
        int subDataCnt;

        re = pkt.checkVaildNetsvcPacket(buf, len);
        if(re > 0)
            return 1;

        //////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////
        re = 1;
        pkt.bytesToPacket(pkt, buf, len, 0);

        subDataSize = pkt.bLen;
        subDataCnt = 0;

        i = 0;
        while(i < subDataSize) {
            sb.bytesToSubData(sb, pkt.subData);

            if((sb.flags & Packet.NET_SUB_FLAGS_MASK) == Packet.NET_SUB_FLAGS_RESPONSE) {
                if(sb.cmdMajor == Packet.NET_SUB_MAJ_READ_KEY
                        && sb.cmdMinor == Packet.NET_SUB_MIN_READ_DATA) {
                    if(sb.len > 0) {
                        StringBuilder str = new StringBuilder();
                        str.append(String.format("Remocon Data (F=%X) :  ", sb.flags & Packet.NET_SUB_ERROR_MASK));
                        for(j = 0; j < sb.len; j++) {
                            code[j] = sb.value[j];
                            str.append(String.format("%02X  ", sb.value[j]));
                        }
                        Log.d("소켓", str.toString());
                    } else
                        Log.d("소켓", String.format("Remocon Data Receiving Error 0x%X", sb.flags & Packet.NET_SUB_ERROR_MASK));
                    re = 0;
                    subDataCnt++;
                }
            }

            if(re > 0)
                Log.d("소켓", String.format("** invalid command %02X - %02X flags = %02X, len = %02X...",
                        sb.cmdMajor, sb.cmdMinor, sb.flags, sb.len));

            i += 4 + sb.len;
        }

        if(pkt.bCount != subDataCnt)
            Log.d("소켓", String.format("subData request count is mismatch %d != %d", pkt.bCount, subDataCnt));

        return re;
    }
}
