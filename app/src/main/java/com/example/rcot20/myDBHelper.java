package com.example.rcot20;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class myDBHelper extends SQLiteOpenHelper {
    public myDBHelper(Context context) {

        super(context, "groupDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 테이블 생성
        String str = "CREATE TABLE groupTBL ( id STRING PRIMARY KEY, volUp INT, volDown INT, " +
                "chlUp INT, chlDown INT, power INT, hdmi INT, ok INT, up INT, down INT, " +
                "left INT, right INT, before INT, mute INT, tvList INT);";
        db.execSQL(str);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 테이블 삭제, 다시 생성
        db.execSQL("DROP TABLE IF EXISTS groupTBL");
        onCreate(db);
    }
}
