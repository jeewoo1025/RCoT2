package com.example.rcot20;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private IntroViewPagerAdapter pagerAdapter;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setCustomActionBar();

        btnSave = (Button)findViewById(R.id.btnSave);

        // fill list screen
        List<ScreenItem> mList = new ArrayList<>();
        setScreenItem(mList);

        // viewPager
        viewPager = (ViewPager)findViewById(R.id.viewPager);
        pagerAdapter = new IntroViewPagerAdapter(this, mList);
        viewPager.setAdapter(pagerAdapter);

        btnSave.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("int", viewPager.getCurrentItem());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    void setScreenItem(List<ScreenItem> list) {
        list.add(new ScreenItem("VOL +", "Volume Up", R.drawable.icon_plus));
        list.add(new ScreenItem("VOL -", "Volume Down", R.drawable.icon_minus));
        list.add(new ScreenItem("CH", "Channel Up", R.drawable.icon_up));
        list.add(new ScreenItem("CH", "Channel Down", R.drawable.icon_down));
        list.add(new ScreenItem("Power", "전원", R.drawable.icon_power));
        list.add(new ScreenItem("HDMI", "HDMI", R.drawable.icon_hdmi));
        list.add(new ScreenItem("OK", "확인", R.drawable.icon_ok));
        list.add(new ScreenItem("Up", "위", R.drawable.icon_up));
        list.add(new ScreenItem("Down", "아래", R.drawable.icon_down));
        list.add(new ScreenItem("Left", "왼쪽", R.drawable.icon_left));
        list.add(new ScreenItem("Right", "오른쪽", R.drawable.icon_right));
        list.add(new ScreenItem("Before", "이전", R.drawable.icon_rotate));
        list.add(new ScreenItem("Mute", "음소거", R.drawable.icon_mute));
        list.add(new ScreenItem("List", "편성표", R.drawable.icon_list));
    }

    void setCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Register");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnInfo:
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.register, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
