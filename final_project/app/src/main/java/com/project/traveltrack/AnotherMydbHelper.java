package com.project.traveltrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AnotherMydbHelper extends SQLiteOpenHelper {
    //設定資料庫的名稱
    public static final String DATABASE_NAME ="AnotherMyDbHelper.db";

    //設定儲存的資料型別
    private static final String DATE_TYPE = " Date";
    private static final String STRING_TYPE = " String";
    private static final String COMMA_SEP = ",";



    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME + " (" +
                    AnotherMyContract.AnotherMyTable1._ID + " INTEGER PRIMARY KEY," +
                    AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE + DATE_TYPE + COMMA_SEP +
                    AnotherMyContract.AnotherMyTable1.SROKE_PATH + STRING_TYPE
                    +")";

    private  static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " +  AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME;


    //建構子
    public AnotherMydbHelper(Context context, int version){
        super(context,DATABASE_NAME,null,version);
        Log.i("brad","AnotherMydbHelper Constructor Call!");
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //使用者第一次使用則呼叫onCreate
        //或者當資料庫的版本不同的時候也會呼叫
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.i("brad","AnotherMydbHelper onCreate Call");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        //當資料庫的版本不同時則呼叫onUpgrade()
        db.execSQL(SQL_DELETE_ENTRIES);
        //呼叫自己的onCreate
        onCreate(db);
        Log.i("brad","AnotherMydbHelper onUpgrade Call");
    }
}
