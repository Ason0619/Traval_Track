package com.project.traveltrack;

import android.provider.BaseColumns;

public class AnotherMyContract {
    public AnotherMyContract(){}
    /*
     * 使用合約類別來管理資料庫結構的描述
     * 能夠系統化的紀錄
     * 例如:想改某個欄位名稱的時候只要在這個類別的欄位修改就能直接完全更改
     * */
    public static abstract class AnotherMyTable1 implements BaseColumns {

        //表格名稱
        public static final String ANOTHERTABLE_NAME = "NameTable";
        //日期
        public static final String ANOTHERLOCATION_DATE = "date";
        //行程名稱
        public static final String SROKE_PATH = "strokePath";

    }
}
