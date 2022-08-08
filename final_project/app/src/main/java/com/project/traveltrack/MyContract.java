package com.project.traveltrack;

import android.provider.BaseColumns;

public class MyContract {
    public MyContract(){}
    /*
    * 使用合約類別來管理資料庫結構的描述
    * 能夠系統化的紀錄
    * 例如:想改某個欄位名稱的時候只要在這個類別的欄位修改就能直接完全更改
    * */
    public static abstract class MyTable1 implements BaseColumns{
        //表格名稱
        public static final String TABLE_NAME = "MapTable";
        //欄位名稱
        //緯度
        public static final String LOCATION_LATITUDE = "latitude";
        //經度
        public static final String LOCATION_LONGITUDE= "longitude";
        //日期
        public static final String LOCATION_DATE = "date";
        //時間
        public static final String LOCATION_TIME = "time";
        //照片路徑
        public static final String IMAGE_PATH = "imgPath";

    }
}
