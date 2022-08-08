package com.project.traveltrack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.project.traveltrack.ui.home.HomeFragment;

import android.hardware.camera2.*;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyJobService extends JobService{
    //建立一個位置要求的物件，用於設定關於位置更新的資訊，例如:設定讀取位置資訊的間隔時間
    //呼叫configLocationRequest()來初始化
    private LocationRequest locationRequest;
    //建立一個融合位置提供者的物件
    //執行時間有效率地選取位置提供者，以符合電池效率的方式提供最佳的位置資訊。
    //例如，使用者在戶外流覽時，會取得使用 GPS 閱讀的最佳位置。
    //如果使用者接著走到室內，其中 GPS 的運作效果不佳（如果有的話），則已融合的位置提供者可能會自動切換至 WiFi，其運作效果較佳
    public static FusedLocationProviderClient mFusedLocationClient;
    //Notification.Builder物件builder被指定用來建構 Notification，捨棄了直接宣告 notification
    private Notification.Builder builder;
    //建立一個通知的管理者
    //功能負責整個 Android 系統的通知控管機制
    private NotificationManager notificationManager;
    //建立一個通知的物件
    //用來儲存相關設定屬性(如是否閃燈、震動…)，當作真正通知訊息主體之類別
    private Notification notification;
    //建立一個List類型是Location來儲存LocationCallback locationResult.getLocations()所得到的位置
    public List<Location> locationList;
    //建立一個callBackLocaition來儲存locationList.get(locationList.size() - 1)(最新的位置)
    //callBackLocation可以跟拍照監聽共用，當拍照的時候可以將callBackLocation跟照片的路徑(String)一併存入資料庫
    public static Location callBackLocation;
    //內容觀察者，用來背景偵聽使用者是否有拍照
    private ScreenShotContentObserver screenShotContentObserver;
    private Handler handler = new Handler();






    //當HomeFragment呼叫背景工作的時候首先執行onStartJob
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("brad","onStartJob is called!");
        //呼叫configLocationRequest()初始化locationRequest
        configLocationRequest();
        //建立FusedLocationClient(FusedLocationClient)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //locationRequest引數來設定FusedLocationClient多久更新一次位置，等等更新資訊
        //Looper(Looper.myLooper()可以不斷的接收訊息，並且執行回呼叫(mLocationCallback)
        //FusedLocationClient使用這些引數來不斷的接收並更新使用者的位置
        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        //當背景工作一開始的時候，就直接呼叫前景服務
        startForegroundService();
        screenShotContentObserver = new ScreenShotContentObserver(handler, this) {
            @Override
            protected void onScreenShot(String path, String fileName) {
            }
        };
        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                screenShotContentObserver
        );

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i("brad","onStopJob is called!");
        return false;
    }


    // 建立Location請求物件
    private void configLocationRequest() {
        locationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        locationRequest.setInterval(10000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        locationRequest.setFastestInterval(10000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //設置位置更新之間的最小位移（以米為單位)
        locationRequest.setSmallestDisplacement(10);
    }

    /*
    * callback function觀念
    * 當主程式呼叫一個函式A 的同時傳入一個指標函式B 給他，
    * 當呼叫的函式A 執行到某個條件時，會去呼叫一開始傳入的指標函式B，
    * 此時便會回到主程式這端進行主程式傳入的函式B去做動作，而做完之後會再回到函式A 去做未完的事情，完成後才又回來主程式
    * *****************************************************************************************************************
    * 宣告一個LocationCallback變數
    * 當使用者位置改變時就會把位置回呼叫到此變數
    * mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    * 使用Looper.myLooper()來不斷的接收改變的訊息
    * */
    LocationCallback mLocationCallback = new LocationCallback() {
        //LocationResult表示地理位置的數據類來自融合的位置提供程序
        //當位置改變時傳送LocationResult
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //locationResult.getLocations()返回計算的位置，從最舊到最新的順序。
            locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //(locationList.size() - 1)因為locationList = locationResult.getLocations()
                //List裡面有很多位置-1代表是最近得到的位置(意即最新的位置)
                callBackLocation = locationList.get(locationList.size() - 1);
                Log.i("brad", "Location: " + callBackLocation.getLatitude() + " " + callBackLocation.getLongitude());
                //設定通知裡面的內容，內容為經緯度
                builder.setContentText(callBackLocation.getLatitude()+" . "+callBackLocation.getLongitude());
                //更新通知
                notificationManager.notify(1,notification);
                //呼叫saveLocationDataToPhone()來將新的位置存入資料庫
                saveLocationDataToPhone(callBackLocation.getLatitude(),callBackLocation.getLongitude(),null);
                //建立一個經緯度的物件來設定Marker(標記)的位置
                LatLng latLng = new LatLng(callBackLocation.getLatitude(), callBackLocation.getLongitude());
                //檢查Marker(標記)是否存在，不存在的話則新增存在的話則改變Marker(標記)的位置
                if(HomeFragment.firstMarker == null && HomeFragment.lastMarker == null){
                    //新增firstMarker跟lastMarker到地圖上面，一開始是同一個位置
                    HomeFragment.firstMarker = HomeFragment.mMap.addMarker(HomeFragment.markerOptions.position(latLng).title("First place"));
                    HomeFragment.lastMarker = HomeFragment.mMap.addMarker(HomeFragment.markerOptions.position(latLng).title("Last place"));
                } else{
                    //firstMarker留在使用者的出發位置lastMarker則隨著使用者改變位置
                    HomeFragment.lastMarker.setPosition(latLng);
                    //呼叫HomeFragment靜態函式trackToM畫出路徑
                    HomeFragment.trackToMe(callBackLocation.getLatitude(), callBackLocation.getLongitude());
                }
                //移動地圖鏡頭，移動到使用者目前的位置
                //HomeFragment.mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }
    };

    //用於構建和啟動前台服務
    private void startForegroundService() {
        builder = new Notification.Builder(this);
        Log.i("brad", "start foreground service.");
        //通知管理者notificationManager取得系統服務getSystemService
        notificationManager =	(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null) {
            //如果專案的目標平台是Android 8.0(API level 26)時，開發者一定要實作通知頻道
            //設定頻道的ID
            String channelId = "myname";
            // I MPORTANCE_NONE 關閉通知
            // IMPORTANCE_MIN 開啟通知，不彈出，没有提示音，狀態欄中無顯示
            // IMPORTANCE_LOW 開啟通知，不彈出，沒有提示音，狀態欄中顯示
            // IMPORTANCE_DEFAULT 開啟通知，不彈出，發出提示音，狀態欄中顯示
            // IMPORTANCE_HIGH 開啟通知，彈出，發出提示音，狀態欄中顯示
            int importance = NotificationManager.IMPORTANCE_LOW;
            //通知頻道的使用只能用在API26以上的行動裝置
            //建立通知頻道並且設定頻道ID、頻道名稱、頻道屬性
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Notification", importance);
            //閃爍指示燈的設置
            notificationChannel.enableLights(false);
            //設定是否震動
            notificationChannel.enableVibration(false);
            //創建通知頻道
            notificationManager.createNotificationChannel(notificationChannel);
            //設定通知的小標示，目前設定為Android小綠人
            builder.setSmallIcon(R.drawable.ic_android);
            //設定通知的頻道ID，跟上面的notificationChannel ID相同
            builder.setChannelId(channelId);

        }
        //把builder的資訊都塞進去通知notification裡面
        notification = builder.build();
        //開始前景服務，前景服務的通知欄通知為notification
        startForeground(1, notification);
    }

    //停止前景服務的函式
    public  void  stopForegroundService() {
        Log.i("brad", "Stop foreground service.");
        // 停止前景服務並且把手機上的通知移除
        stopForeground(true);
        // 停止前景服務
        stopSelf();
    }

    //當HomeFragment呼叫onDestroy時候會scheduler.cancelAll()取消所有工作
    @Override
    public void onDestroy() {
        Log.i("brad", "JobService is onDestroy!");
        //呼叫stopForegroundService()結束前景服務
        //stopForegroundService();
        //把mFusedLocationClient移除監聽停止更新
       //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        //把locationList清空
        //locationList.clear();
        try {
            //把mFusedLocationClient移除監聽停止更新
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            //把locationList清空
            locationList.clear();
            getContentResolver().unregisterContentObserver(screenShotContentObserver);
        } catch (Exception e) {
            Log.i("brad", "error ! " + e);
            e.printStackTrace();
        }
        if(HomeFragment.traceOfMe!=null){
            HomeFragment.traceOfMe.clear();
        }
        super.onDestroy();
    }

    /* saveLocationDataToPhone()將經緯度時間存入資料庫
    *  如果只有mLocationCallback呼叫函式則只存經緯度時間
    *  若是ScreenShotContentObserver的onChange()呼叫則存入經緯度時間跟照片路徑
    */
    public static  void saveLocationDataToPhone(double lat,double lng,String imgPath){
        //建立一個新的日期物件，內容為現在
        Date dNow = new Date();
        //設定要儲存日期時間的格式
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
        SimpleDateFormat dt = new SimpleDateFormat ("hh:mm:ss");
        // System.out.println("当前时间为: " + ft.format(dNow));
        //格式化現在的時間
        String date = ft.format(dNow);
        String time = dt.format(dNow);
        //使用Log來顯示目前使用者的時間，以便開發者開發
        Log.i("brad","Now time : " + date +" "+ time);
        //將經緯度日期時間照片路徑丟到值裡面
        ContentValues values = new ContentValues();
        values.put(MyContract.MyTable1.LOCATION_LATITUDE,lat);
        values.put(MyContract.MyTable1.LOCATION_LONGITUDE,lng);
        values.put(MyContract.MyTable1.LOCATION_DATE,date);
        values.put(MyContract.MyTable1.LOCATION_TIME,time);
        values.put(MyContract.MyTable1.IMAGE_PATH,imgPath);
        //建立一個newRowId來接收新增的資料ID
        long newRowId;
        //databace.insert()把剛剛的values加入所建立的資料庫裏面
        newRowId = HomeFragment.database.insert(MyContract.MyTable1.TABLE_NAME,null,values);
        //假如回傳的不是-1而是一個正常的值則代表資料存入資料庫，如為-1則代表資料存入失敗
        if(newRowId != -1){
            Log.i("brad","data is save");
        }else{
            Log.i("brad","data is not save");
        }
    }

}
