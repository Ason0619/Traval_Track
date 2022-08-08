package com.project.traveltrack;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

public abstract class ScreenShotContentObserver extends ContentObserver {

    private Context context;
    private boolean isFromEdit = false;
    private String previousPath;
    public String TAG = "brad";

    public ScreenShotContentObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public boolean deliverSelfNotifications() {
        Log.i("brad", "deliverSelfNotifications()");
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.i("brad", "take photo");
        super.onChange(selfChange);
    }
    //當觀察的URI發生改變的時候就會呼叫此函式
    @Override
    public void onChange(boolean selfChange, Uri uri) {

        Log.i("brad", "onChange"+selfChange);
        //建立一個字串物件來儲存照片的路徑
        String path;
        //建立一個標記物件來讀取媒體庫的檔案
        Cursor cursor = null;
        try {
            //標記物件搜尋在MyjobService所宣告的URI(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //MediaStore是Android的一個文件系統的數據庫，記錄了所有文件的索引，可以透過它快速的查找系統文件
            //而這個URI就是外部存儲設備上的媒體
            /*new String[]{
                    // 媒體項目的顯示名稱
                    // 例如，存儲在的項目 /storage/0000-0000/DCIM/Vacation/IMG1024.JPG的顯示名稱為IMG1024.JPG
                    MediaStore.Images.Media.DISPLAY_NAME,
                    // 磁盤上媒體項的絕對文件系統路徑，就是照片的所存放的路徑
                    MediaStore.Images.Media.DATA
            }*/
            cursor = context.getContentResolver().query(uri, new String[]{
                    // 媒體項目的顯示名稱
                    // 例如，存儲在的項目 /storage/0000-0000/DCIM/Vacation/IMG1024.JPG的顯示名稱為IMG1024.JPG
                    MediaStore.Images.Media.DISPLAY_NAME,
                    // 磁盤上媒體項的絕對文件系統路徑，就是照片的所存放的路徑
                    MediaStore.Images.Media.DATA
            }, null, null, null);
            // 把標記移到媒體庫的最後面，因為是剛剛拍的照片所以會在媒體庫的最後面
            if (cursor != null && cursor.moveToLast()) {
                //建立一個整數的變數來儲存標記的列索引值，之後可以靠這個索引值去讀出存在列裡面的照片名稱
                int displayNameColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                //建立一個整數的變數來儲存標記的列索引值，之後可以靠這個索引值去讀出存在列裡面的照片路徑
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                //建立一個字串變數，使用剛剛所取得的索引值讀出那列的照片名稱
                String fileName = cursor.getString(displayNameColumnIndex);
                //用最一開始宣告的字串變數，使用剛剛所取得的索引值讀出那列的照片名路徑
                path = cursor.getString(dataColumnIndex);
                Log.i("brad", fileName + "----" + path);
                //呼叫MyJobService的靜態函式，將callBackLocation的經緯度跟照片的路徑存入資料庫
                MyJobService.saveLocationDataToPhone(
                        MyJobService.callBackLocation.getLatitude(),
                        MyJobService.callBackLocation.getLongitude(),
                        path
                        );
            }
        } catch (Exception e) {
            e.printStackTrace();
            isFromEdit = true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        super.onChange(selfChange, uri);
    }

    private boolean isScreenshot(String path) {
        return path != null && path.toLowerCase().contains("screenshot");
    }


    protected abstract void onScreenShot(String path, String fileName);

}
