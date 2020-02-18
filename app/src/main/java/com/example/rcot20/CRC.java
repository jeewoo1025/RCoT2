package com.example.rcot20;

public class CRC {
    private static int poly = 0x1021;
    private static int presetValue = 0x0000;

    public static int getCrc(byte[] chkData, int len) {
        int crc = presetValue;

        for(int i = 0; i < len; i++) {
            int b = (chkData[i] & 0xff);
            for(int j = 0; j < 8; j++) {
                boolean bit = ((b >> (7-j) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if(c15 ^ bit)
                    crc ^= poly;
            }
        }

        crc &= 0xffff;
        return crc;
    }
}
