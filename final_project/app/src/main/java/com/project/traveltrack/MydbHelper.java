package com.project.traveltrack;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import com.project.traveltrack.MyContract;
import android.util.Log;

public class MydbHelper extends SQLiteOpenHelper {
    //設定資料庫的名稱
    public static final String DATABASE_NAME ="MyDbHelper.db";

    //設定儲存的資料型別
    private static final String DOUBLE_TYPE =" Double";
    private static final String DATE_TYPE = " Date";
    private static final String TIME_TYPE = " Time";
    private static final String STRING_TYPE = " String";
    private static final String COMMA_SEP = ",";



    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MyContract.MyTable1.TABLE_NAME + " (" +
                    MyContract.MyTable1._ID + " INTEGER PRIMARY KEY," +
                    MyContract.MyTable1.LOCATION_LATITUDE + DOUBLE_TYPE + COMMA_SEP +
                    MyContract.MyTable1.LOCATION_LONGITUDE + DOUBLE_TYPE + COMMA_SEP +
                    MyContract.MyTable1.LOCATION_DATE + DATE_TYPE + COMMA_SEP +
                    MyContract.MyTable1.LOCATION_TIME + TIME_TYPE + COMMA_SEP +
                    MyContract.MyTable1.IMAGE_PATH + STRING_TYPE
                    +")";
    private  static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + MyContract.MyTable1.TABLE_NAME;

    //建構子
    public MydbHelper(Context context,int version){
        super(context,DATABASE_NAME,null,version);
        Log.i("brad","MydbHelper Constructor Call!");
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //使用者第一次使用則呼叫onCreate
        //或者當資料庫的版本不同的時候也會呼叫
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.i("brad","MydbHelper onCreate Call");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        //當資料庫的版本不同時則呼叫onUpgrade()
        db.execSQL(SQL_DELETE_ENTRIES);
        //呼叫自己的onCreate
        onCreate(db);
        Log.i("brad","MydbHelper onUpgrade Call");
    }

}
