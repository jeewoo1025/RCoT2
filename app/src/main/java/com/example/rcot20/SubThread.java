package com.example.rcot20;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class SubThread extends Thread {
    private Handler mHandler;
    private boolean isRunning;
    private boolean isSendMode;     // run에서 send할꺼냐?
    private boolean isRecvMode;     // run에서 recv할꺼냐?
    private byte[] sendBuf;
    private byte[] recvBuf;
    private InputStream in;         // recv
    private OutputStream out;       // send
    private int readByteCnt;

    public SubThread() {
        init();
    }

    public void init() {
        isRunning = false;
        isSendMode = false;
        isRecvMode = false;
        sendBuf = new byte[Packet.MAX_ARR_SIZE];
        recvBuf = new byte[Packet.MAX_ARR_SIZE];
        in = null;
        out = null;
        readByteCnt = 0;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void setMode(boolean send, boolean recv) {
        isSendMode = send;
        isRecvMode = recv;
        Log.d("소켓", "setMode(" + isSendMode + ", " + isRecvMode + ") is started....");
    }

    public int setSendBuf(byte[] buf) {
        if(buf.length > Packet.MAX_ARR_SIZE) {
            Log.e("에러", String.format("setSendBuf(), bufLength(%d) > 128", buf.length));
            return 1;
        }

        Arrays.fill(sendBuf, (byte)0);
        System.arraycopy(buf, 0, sendBuf, 0, buf.length);
        return 0;
    }

    public byte[] getRecvBuf() {
        return recvBuf;
    }

    public boolean getRecvMode() {
        return isRecvMode;
    }

    public int getReadByteCnt() {
        return readByteCnt;
    }

    public void sendMessage(int val) {
        Message msg = mHandler.obtainMessage();     // message 객체 할당
        msg.arg1 = val;
        mHandler.sendMessage(msg);
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void closeStream() throws IOException{
        if(in != null)
            in.close();
        if(out != null)
            out.close();
    }

    @Override
    public void run() {
        super.run();
        Log.d("소켓", "run() is started.....");

        try{
            in = SocketManager.getSocket().getInputStream();
            out = SocketManager.getSocket().getOutputStream();
        } catch (IOException e) {
            Log.e("소켓", "in or out stream 생성     " + e.getMessage());
            isRunning = false;
        }

        while (isRunning) {
            try {
                if(isSendMode) {
                    // send Packet
                    out.write(sendBuf);
                    out.flush();

                    // debug
                    Log.d("소켓", "Server에게 Packet을 send 했습니다...");
                    Log.d("소켓", Misc.byteArrayToHex(sendBuf));
                    isSendMode = false;
                }

                if(isRecvMode) {
                    // wait_for_ready..
                    while(true) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            isRunning = false;
                            break;
                        }

                        Arrays.fill(recvBuf, (byte) 0);
                        readByteCnt = in.read(recvBuf);     // recv
                        if(readByteCnt > 0)
                            break;
                        else
                            Log.e("소켓", "read Packet에서 wrong count : " + readByteCnt);
                    }

                    // debug
                    Log.d("소켓", "Server에서 Packet을 recv 했습니다!!!!" + readByteCnt);
                    Log.d("소켓", Misc.byteArrayToHex(recvBuf));
                    sendMessage(readByteCnt);
                    isRecvMode = false;
                }
                Thread.sleep(10);
            } catch (IOException e) {
                Log.e("소켓", "At run()...." + e.getMessage());
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        try {
            closeStream();
            SocketManager.closeSocket();
            Log.d("소켓", "스레드 종료");
        } catch (IOException e) {
            Log.e("소켓","run 메소드 : in.close() or out.close()" + e.getMessage());
        }
    }
}
