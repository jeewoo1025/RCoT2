package com.example.rcot20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText devicename, ipAddress;
    Button loginButton, eyeButton;

    int counter = 3;
    int time = 10;

    Handler handler1= new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            counter=3;
            time=10;
            loginButton.setText("LOGIN");
            loginButton.setEnabled(true);
            loginButton.setBackgroundResource(R.drawable.green);
        }
    };

    Handler handler2 = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            loginButton.setText(String.valueOf(time) + " sec");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        devicename = (EditText)findViewById(R.id.devicename);
        ipAddress = (EditText)findViewById(R.id.ipAddress);
        loginButton = (Button)findViewById(R.id.loginButton);
        eyeButton = (Button)findViewById(R.id.eyeButton);

        hideAndShow();
    }

    public void loginClicked(View view) {
        if(validate() && infoMatch()) {
            final ProgressDialog progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setMessage("Please wait");
            progressDialog.show();

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            // 딜레이 후 새 액티비티 생성
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            progressDialog.dismiss();
                        }
                    }
                    , 3000);    // 3초 정도 딜레이를 준 뒤 시작
        } else {
            if(validate()) {
                Toast.makeText(this, "Doesn't match", Toast.LENGTH_SHORT).show();
                chancesLeft();
            }
        }
    }

    public boolean infoMatch() {
        String device = devicename.getText().toString();
        String ipAddr = ipAddress.getText().toString();

        if(device.equalsIgnoreCase("tv") && ipAddr.equalsIgnoreCase("0000")) {
            return true;
        } else
            return false;
    }

    public boolean validate() {
        String device_name = devicename.getText().toString();
        String ip_addr = ipAddress.getText().toString();
        if(device_name.isEmpty()) {
            devicename.setError("Enter a valid device name");
            return false;
        } else {
            devicename.setError(null);
            if(ip_addr.isEmpty()) {
                ipAddress.setError("Enter a valid ip address ( XXX.XX.XX.XXX )");
                return false;
            } else
                ipAddress.setError(null);
            return true;
        }
    }

    public void chancesLeft() {
        counter--;
        switch (counter) {
            case 2:
                Toast.makeText(this, "2 Left!", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, "1 Left!", Toast.LENGTH_SHORT).show();
                break;
            case 0:
                blockTheButton();
                break;
        }
    }

    public void blockTheButton() {
        loginButton.setText("10 sec");
        loginButton.setEnabled(false);
        loginButton.setBackgroundResource(R.drawable.grey);

        Thread thread2 = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while(time > 1) {
                            synchronized (this) {
                                try {
                                    wait(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                time--;
                            }
                            handler2.sendEmptyMessage(0);
                        }
                    }
                }
        );
        thread2.start();

        Thread thread1 = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            try {
                                wait(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        handler1.sendEmptyMessage(0);
                    }
                }
        );
        thread1.start();
    }

    public void hideAndShow() {
        eyeButton.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                ipAddress.setInputType(InputType.TYPE_CLASS_TEXT);
                                break;
                            case MotionEvent.ACTION_UP:
                                ipAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                break;
                        }
                        return true;
                    }
                }
        );
    }

}
