package com.example.rcot20;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    // View
    TextView txtTV;
    Button btnTvPower, btnSettopPower, btnHDMI, btnSchedule, btnOK;
    ImageButton btnPower, btnBefore, btnMute;
    ImageButton btnUp, btnDown, btnLeft, btnRight;
    ImageButton btnVolumeUp, btnVolumeDown, btnChannelUp, btnChannelDown;

    // DB
    private myDBHelper myHelper;
    private SQLiteDatabase sqlDB;

    // etc...
    private Netsvc netsvc;
    private String keyValue;
    private int keyPos;
    private final String[] keyStr = {"volUp", "volDown", "chlUp", "chlDown", "power", "hdmi", "ok", "up", "down", "left", "right", "before", "mute", "tvList"};

    // Handler
    private Handler mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
          // notice that recv Packet from server
          super.handleMessage(msg);
          int cnt = msg.arg1;
          updateDB(cnt);
      }
    };

    public void updateDB(int cnt) {
        int[] code = new int[3];

        // check the Packet from server
        netsvc.netsvcReadDataService(ThreadListener.getThread().getRecvBuf(), cnt, code);

        // put the keyValue into DB
        keyValue = Misc.intArrayToString(code);
        Log.d("소켓", "keyValue : " + keyValue);

        if(keyValue != null && keyValue.length() > 0) {
            sqlDB = myHelper.getWritableDatabase();
            Cursor cursor = sqlDB.rawQuery("SELECT * FROM groupTBL;", null);

            if(cursor.getCount() == 0)
                sqlDB.execSQL("INSERT INTO groupTBL VALUES ( 'jwTV', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);");
            sqlDB.execSQL("UPDATE groupTBL SET " + keyStr[keyPos] + " = " + "'" + keyValue + "' WHERE  id = 'jwTV';");
            sqlDB.close();
            Log.d("소켓", "put    " + keyValue + "    on DB");

            Toast toast = Toast.makeText(this, "Success", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setCustomActionBar();

         // make subThread, connect to Server, set Handler
         ThreadListener.getThread();
         ThreadListener.getThreadStarted();
         ThreadListener.setHandler(mHandler);

        txtTV = (TextView)findViewById(R.id.txtTV);
        btnTvPower = (Button)findViewById(R.id.btnTvPower);
        btnSettopPower = (Button)findViewById(R.id.btnSettopPower);
        btnHDMI = (Button)findViewById(R.id.btnHDMI);
        btnSchedule = (Button)findViewById(R.id.btnSchedule);
        btnOK = (Button)findViewById(R.id.btnOK);
        btnPower = (ImageButton)findViewById(R.id.btnPower);
        btnBefore = (ImageButton)findViewById(R.id.btnBefore);
        btnMute = (ImageButton)findViewById(R.id.btnMute);
        btnUp = (ImageButton)findViewById(R.id.btnUp);
        btnDown = (ImageButton)findViewById(R.id.btnDown);
        btnLeft = (ImageButton)findViewById(R.id.btnLeft);
        btnRight = (ImageButton)findViewById(R.id.btnRight);
        btnVolumeUp = (ImageButton)findViewById(R.id.btnVolumeUp);
        btnVolumeDown = (ImageButton)findViewById(R.id.btnVolumeDown);
        btnChannelUp = (ImageButton)findViewById(R.id.btnChannelUp);
        btnChannelDown = (ImageButton)findViewById(R.id.btnChannelDown);

        myHelper = new myDBHelper(this);
        netsvc = new Netsvc();

        btnVolumeUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(0);
            }
        });

        btnVolumeDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(1);
            }
        });

        btnChannelUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(2);
            }
        });

        btnChannelDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(3);
            }
        });

        btnPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(4);
            }
        });

        btnHDMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(5);
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(6);
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(7);
            }
        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(8);
            }
        });

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(9);
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(10);
            }
        });

        btnBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(11);
            }
        });

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(12);
            }
        });

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCodePacket(13);
            }
        });
    }

    void setCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);        // custom enabled = true
        actionBar.setDisplayHomeAsUpEnabled(false);         // 뒤로가기 버튼
        actionBar.setDisplayUseLogoEnabled(true);           // home logo
        actionBar.setDisplayShowHomeEnabled(true);          // show home logo

        actionBar.setTitle("  RCoT");
        actionBar.setIcon(R.drawable.icon_home);

        actionBar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnRegister:
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent, 1234);
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234 && resultCode == RESULT_OK) {
          int pos = data.getIntExtra("int", 0);
          keyPos = pos;
          readKey();
        }
    }

    public void readKey() {
        // send Packet
        keyValue = null;
        int[] code = new int[3];
        code[0] = 5;                    // read timeout second

        CommonNetPacket sendPkt = netsvc.sendNetsvcService(true, code, 1);
        ThreadListener.getThread().setSendBuf(sendPkt.getPacketArr());
        ThreadListener.getThread().setMode(true, true);

        Toast toast = Toast.makeText(this, "Push the key button!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void sendCodePacket(int index) {
        CommonNetPacket pkt;
        int[] value;

        value = getDatabaseValue("jwTV", index);
        printIntArr(value);

        pkt = netsvc.sendNetsvcService(false, value, 3);

        ThreadListener.getThread().setSendBuf(pkt.getPacketArr());
        ThreadListener.getThread().setMode(true, false);
    }

    public int[] getDatabaseValue(String id, int index) {
        int[] value = new int[3];
        int num = 0;

        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("SELECT * FROM groupTBL;", null);

        while(cursor.moveToNext()) {
            if(cursor.getString(cursor.getColumnIndex("id")).equals(id))
                num = cursor.getInt(cursor.getColumnIndex(keyStr[index])) & 0xffffff;
        }

        if(num != 0) {
            value[0] = (num & 0xff0000) >> 16;
            value[1] = (num & 0x00ff00) >> 8;
            value[2] = (num & 0x0000ff);
            Log.d("소켓", String.format("getDataBaseValue %02X %02X %02X", value[0], value[1], value[2]));
        }
        sqlDB.close();

        return value;
    }

    public void printIntArr(int[] value) {
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < value.length; i++)
            str.append(String.format("%02X ", value[i]));
        Log.d("소켓", str.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // thread terminated
        ThreadListener.closeThread();
    }
}
