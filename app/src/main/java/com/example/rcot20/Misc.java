package com.example.rcot20;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Misc {
    static final String DEFAULT_BROADCAST_IP="192.168.88.255";
    static final int DEFAULT_DISCOVERY_PORT=55566;
    static final int DEFAULT_APP_TCP_PORT=20000;
    static final int MAX_BUF_SIZE=512;
    static final String NET_DEVICE_ETHO="wlan0";

    public static class Offset {
        int off;

        Offset() {
            off = 0;
        }

        void setOff(int num) {
            off = num;
        }

        int getOff() {
            return off;
        }
    }

    public static String byteArrayToHex(byte[] arr) {
        int i = 0;
        StringBuilder sb = new StringBuilder();

        for(final byte b : arr) {
            if((i%16) == 0)
                sb.append("\n");
            else if((i%8) == 0)
                sb.append("    ");
            else if((i%4) == 0)
                sb.append("  ");

            sb.append(String.format("%02x ", b & 0xff));
            i++;
        }
        return sb.toString();
    }

    public static InetAddress getIPAddress(String netName) {
        try {
            NetworkInterface intf = NetworkInterface.getByName(netName);
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                    return addr;
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getNetworkInterface() {
        String net_name = NET_DEVICE_ETHO;
        return net_name;
    }

    public static InetAddress getBroadcastAddress(String netName) {
        InetAddress iAddr = null;
        try {
            NetworkInterface intf = NetworkInterface.getByName(netName);
            List<InterfaceAddress> addrs = intf.getInterfaceAddresses();

            for(InterfaceAddress addr : addrs) {
                iAddr = addr.getBroadcast();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return iAddr;
    }

    public static String intArrayToString(int[] arr) {
        // arr 배열 --> int code..
        int num = 0;

        num = arr[0];
        num <<= 8;
        num |= arr[1];
        num <<= 8;
        num |= arr[2];

        return Integer.toString(num);
    }
}
