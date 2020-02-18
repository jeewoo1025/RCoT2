package com.example.rcot20;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketManager {
    public final static String host = "***.***.**.**";
    public final static int tcpPort = 20000;

    private static Socket clientSocket;

    public static Socket getSocket() throws IOException {
        if(clientSocket == null)
            clientSocket = new Socket();

        if(clientSocket.isConnected() == false)
            clientSocket.connect(new InetSocketAddress(host, tcpPort));

        return clientSocket;
    }

    public static void closeSocket() throws IOException {
        if(clientSocket != null)
            clientSocket.close();
    }
}
