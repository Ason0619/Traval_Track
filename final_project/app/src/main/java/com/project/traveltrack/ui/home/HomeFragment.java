package com.project.traveltrack.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.KeyListener;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.project.traveltrack.AnotherMyContract;
import com.project.traveltrack.MapsActivity;
import com.project.traveltrack.MyContract;
import com.project.traveltrack.MyItem;
import com.project.traveltrack.MyItemRenderer;
import com.project.traveltrack.MyJobService;
import com.project.traveltrack.MydbHelper;
import com.project.traveltrack.R;
import com.project.traveltrack.singal;
import com.project.traveltrack.ui.dashboard.DashboardFragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    //宣告程式所需要的物件或變數
    public static String Name="1999-06-19";
    //GoogleMap mMap地圖物件
    public static GoogleMap mMap;
    //nowLocation 存放使用者目前更新的位置
    private Location nowLocation;
    //latitude , longitude 用來存取新位置的經度和緯度
    private double latitude;
    private double longitude;
    //MarkerOptions可以用來設定Marker的屬性
    public static MarkerOptions markerOptions;
    //firstMarker , lastMarker用來標記起點和終點
    //(例如程式一開始的時候firstMarker的位置就在lastLocation，lastMarker的位置則跟著新位置更新)
    public static Marker firstMarker,lastMarker;
    //traceOfMe 陣列列表用來儲存要畫線的經緯度
    public static ArrayList<LatLng> traceOfMe;
    //mydbHelper可以用來輔助資料庫物件，例如建立資料庫或更新資料庫版本等等
    public static MydbHelper mydbHelper;
    //database資料庫用來儲存資料(經緯度，日期，時間)
    public static SQLiteDatabase database;
    //btnSelect圖像按鈕(日曆)用來呼叫DatePickerDialog讓使用者選擇日期
    private ImageButton btnSelect;
    //dm用來取得手機螢幕的資訊(視窗的高度、寬度)
    private DisplayMetrics dm;
    //selectDateToLoad 日期物件用來儲存使用者所選擇的日期
    private Date selectDateToLoad;
    //line為畫線的物件
    private static Polyline line;
    //PERMISSMIONCODE 用來設定取得權限後傳給onRequestPermissionsResult函式的requestCode
    private int PERMISSMIONCODE = 2;
    //mapFragment 地圖片段
    SupportMapFragment mapFragment;
    //JobScheduler 處理 APP 在後台時資料更新，且可規定只有在裝置街上插頭電源和連接 wifi 時在進行資料下載更新
    public static JobScheduler scheduler;
    //ComponentName 組件名稱，透過調用Intent中的setComponent方法，我們可以打開另外一個活動或服務
    private ComponentName jobService;
    //JobInfo是用來包裝工作的條件、開始時間、截止時間、具體的工作等等要設定的參數
    private JobInfo jobInfo;
    //Intent用來啟動後台服務
    public static Intent service;
    //群集管理用來管理相片標記的物件
    private ClusterManager<MyItem> mClusterManager;

    private ImageButton delet;

    private ImageButton draw;

    private  String startTime;

    private View view;

    FrameLayout frameLayout;

    private ProgressDialog progressDialog;

    private Handler handler;

    private boolean flag;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frameLayout = new FrameLayout(getActivity());
        view = inflater.inflate(R.layout.activity_maps, container, false);
        Log.i("brad","onCreate is called ");
        Log.i("brad","Name"+Name);
        //當程式一建立的時候就呼叫函式檢查權限
        if(checkLocationPermission()){
        }else{
            //如果checkLocationPermission()回傳-1代表使用者已將某個權限關閉則在要求使用者開放權限
            Log.i("brad","Permission else is called ");
            requestPermission();
        }
        // 獲取SupportMapFragment並在準備使用地圖時得到通知
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        //檢查使用者是否已經開放權限如果開放權限則可進入onMapReady()
       if(checkLocationPermission()) {
           mapFragment.getMapAsync(this);
        }else{
           Log.i("brad","Permission else is called ");
            //如果checkLocationPermission()回傳0代表使用者已將某個權限關閉則在要求使用者開放權限
            requestPermission();
        }

        markerOptions = new MarkerOptions();
        //建立一個資料庫輔助物件
        mydbHelper = new MydbHelper(view.getContext(),1);
        Log.i("brad","new MydbHelper Call");
        //創建選擇日期按鈕，然後取得手機視窗大小後將按鈕放到合適的位置

        //addListenerOnButton();
        dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        //視窗的寬度
        int screenWidth = dm.widthPixels;
        //視窗高度
        int screenHeight = dm.heightPixels;
        //btnSelect.setX(screenWidth-btnSelect.getWidth()-250);
        //  btnSelect.setY(screenHeight-btnSelect.getHeight()-250);
//        btnSelect.setVisibility(View.VISIBLE);
        delet=(ImageButton)view.findViewById(R.id.delete);
        delet.setOnClickListener(deletListner);

        draw=(ImageButton)view.findViewById(R.id.draw);

        draw.setOnClickListener(drawtListner);

        //loadDateRoute();
        return view;
    }

    //刪除見的聆聽者
    private View.OnClickListener deletListner= new View.OnClickListener() {
        @Override
        //按下Button事件時會進入這個 function
        public void onClick(View v) {
            //database.delete(MyContract.MyTable1.TABLE_NAME,"date=?",new String[]{startTime});
            mClusterManager.clearItems();
            mMap.clear();
           //mLoadingBar.setVisibility(View.GONE);
        }
    };

    private View.OnClickListener drawtListner= new View.OnClickListener() {
        @Override
        //按下Button事件時會進入這個 function
        public void onClick(View v) {

            handler = new Handler();
           buildloading();
            //new Thread(r1).start();
            //buildloading();
            //mLoadingBar.setVisibility(View.VISIBLE);
            drawloaddate();
            progressDialog.dismiss();
        }
    };

    //先將地圖和經緯度陣列清單清空後，載入資料庫資料並將兩個Marker放入地圖並畫出路線
    public void drawloaddate() {
        /*handler.post(new Runnable() {
                         @Override
                         public void run() {
                         }});*/
        //宣告四個double物件
        //用來計算下一個點是否距離過遠，來判斷下一個點是否為路徑上連續的點
        //如果距離太遠代表該點為不合理的點，有可能為使用者關掉定位後又開啟導致出現一個獨立的點或路徑
        double disFirstLat;
        double disFirstLng;
        double disFinalLat;
        double disFinalLng;
        String imgpath;
        //清空經緯度陣列這樣等等讀取檔案時使用trackToMe(lat,lng)時才不會也使用到目前在陣列裡面的位置資料
        //如果traceOfMe是null的話那就新增，如果traceOfMe不是空的則把裡面的資料清除
        if(traceOfMe==null){
            traceOfMe = new ArrayList<LatLng>();
        }
        if(!traceOfMe.isEmpty()){
            traceOfMe.clear();
        }
        if(mClusterManager!=null)
        {
            //把標記都清除
            mClusterManager.clearItems();
        }

        //搜尋選擇日期所有的經緯度並畫出當初的路線
        //格式化選擇的日期並搜索資料庫
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        startTime = Name;
        //startTime=Name;
        //query是資料庫的語法
        //query (String table,String[] columns,String selection,String[] selectionArgs,String groupBy,String having,String orderBy)
        //table         =>用於編譯查詢的表名，該程式的表名為MapTable
        //columns       =>要返回的列的列表。傳遞null將返回所有列，不建議這樣做，以防止從不會使用的存儲中讀取數據。
        //selection     =>用於聲明要返回哪些行的過濾器，格式為SQL WHERE子句（不包括WHERE本身）。傳遞null將返回給定表的所有行。startTime為使用者選擇的時間
        //selectionArgs =>你要尋找的值
        //groupBy       =>一個聲明如何對行進行分組的過濾器，格式為SQL GROUP BY子句（不包括GROUP BY本身）。傳遞null將導致行不被分組。
        //having        =>如果使用行分組，過濾器將聲明要包括在游標中的行組，其格式設置為SQL HAVING子句（不包括HAVING本身）。傳遞null將導致包括所有行組，並且在不使用行分組時是必需的。
        //orderBy       =>如何對行進行排序，格式為SQL ORDER BY子句（不包括ORDER BY本身）。傳遞null將使用默認的排序順序，該順序可能是無序的。
        Cursor cursor = database.query(
                MyContract.MyTable1.TABLE_NAME,null,"date=?",new String[]{startTime},
                null,null,null);
        String[] columns={MyContract.MyTable1.LOCATION_DATE};
        Cursor c= database.query(true,MyContract.MyTable1.TABLE_NAME,columns,null,new String[]{},null,null,null,null);
        Log.i("brad","All :"+c.getCount()+" Record !");
        c.moveToFirst();
        //getCount()取得使用者搜尋的資料有幾筆符合
        int count = cursor.getCount();
        //使用Log跳出訊息方便開發者監控程式過程
        //   Log.i("brad","Now : your date is "+startTime);
        //顯示在android studio 的Log.i上面讓開發者可以知道尋找的資料有幾筆
        Log.i("brad","Now : you select date have "+count+" Record !");
        //當count = 0代表你選擇的日期沒有任何紀錄，所以使用Toast跳出訊息提醒使用者然後return直接跳出函式
        if(count == 0){
            //Toast.makeText(view.getContext(),"There is no relevant record for the date you choose !",Toast.LENGTH_SHORT).show();
            //scheduler.cancelAll();
            //結束時把地圖清空
            mMap.clear();
            Toast.makeText(view.getContext(),"There is no relevant record for the date you choose !",Toast.LENGTH_SHORT).show();
            //alert.dismiss();
            //呼叫onDestroy
            return;
        }
        Toast.makeText(view.getContext(),"Open Track : "+startTime,Toast.LENGTH_SHORT).show();
        //把地圖的物件全部清除
        mMap.clear();
        //建立一個Bitmap物件用來接收BitmapFactory.decodeFile(imgpath)所回傳的點陣圖
        //decodeFile用來解碼文件的一個函式
        //impath是存在資料庫的照片路徑字串
        Bitmap bitmap;
        //smallMarker用來縮放上面的bitmap物件
        Bitmap smallMarker;
        //將指標移至第一筆資料
        if (cursor.moveToFirst())
        {
            //建立一個經緯度的物件存放第一個地方的經緯度
            LatLng sydney = new LatLng(
                    cursor.getDouble(cursor.getColumnIndex("latitude")),
                    cursor.getDouble(cursor.getColumnIndex("longitude")));
            Log.i("brad","marker else");
            /*
            if(firstMarker.isVisible()&&lastMarker.isVisible()){
                Log.i("brad","marker is visible");
            }else{
                Log.i("brad","marker is unvisible");
                firstMarker.setVisible(true);
                lastMarker.setVisible(true);
            }*/
            firstMarker = HomeFragment.mMap.addMarker(HomeFragment.markerOptions.position(sydney).title("First place"));
            lastMarker = HomeFragment.mMap.addMarker(HomeFragment.markerOptions.position(sydney).title("Last place"));
            firstMarker.setPosition(sydney);
            lastMarker.setPosition(sydney);
            //如果初始位置也有拍照(imgPath != null)則把照片讀近來然後
            if(cursor.getString(cursor.getColumnIndex("imgPath"))!=null){
                //imgpath用來儲存資料庫在欄位imgPath的路徑字串
                imgpath = cursor.getString(cursor.getColumnIndex("imgPath"));
                //使用deccodeFile(imgpath)來解碼該路徑的照片
                // bitmap = readBitMap(imgpath);
                bitmap =bitmapToString(imgpath);
                //使用createScaledBitmap從當前的圖片一定的比例創建一個新的位圖
                smallMarker = Bitmap.createScaledBitmap(bitmap, 84, 84, false);
                //把這個點加入群集裡面因為這個點上面有拍照所以在點上建立一個照片的標記
                mClusterManager.addItem(new MyItem(sydney.latitude,sydney.longitude,smallMarker,imgpath));
            }
            //把選擇的日期的第一個位置經緯度存到disFirstLat跟disFirstLng
            disFirstLat = cursor.getDouble(cursor.getColumnIndex("latitude"));
            disFirstLng = cursor.getDouble(cursor.getColumnIndex("longitude"));
            //呼叫trackToMe()來畫路線
            trackToMe(disFirstLat,disFirstLng);
            do{
                //讀出那一列的所有資訊並且顯示到Logcat
                // int id = cursor.getInt(cursor.getColumnIndex("_id"));
                //  String d = cursor.getString(cursor.getColumnIndex("date"));
                //  String t = cursor.getString(cursor.getColumnIndex("time"));
                //  Log.i("brad","database data"+id+" = "+lat+" "+lng+" and time is "+d+" "+t);
                //disFinal來存下一個位置的經緯度
                disFinalLat = cursor.getDouble(cursor.getColumnIndex("latitude"));
                disFinalLng = cursor.getDouble(cursor.getColumnIndex("longitude"));
                //使用getDistanceBetween函式來運算disFirst和disFinal經緯度的距離，單位為米(M)
                //當兩點距離超過300公尺的時候代表下一點不是在使用者的連續路徑中，所以獨立建立一個標記點
                if(getDistanceBetween(new LatLng(disFirstLat,disFirstLng),new LatLng(disFinalLat,disFinalLng)) > 300){
                    //mMap.addMarker(markerOptions.position(new LatLng(disFirstLat,disFirstLng)).title("Independent location"));
                    //如果下一個位置有相片的記錄的話就建立相片標記
                    if(cursor.getString(cursor.getColumnIndex("imgPath"))!=null){
                        //imgpath用來儲存資料庫在欄位imgPath的路徑字串
                        imgpath = cursor.getString(cursor.getColumnIndex("imgPath"));
                        //使用deccodeFile(imgpath)來解碼該路徑的照片
                        //去判斷照片是否存在，因為使用者有可能把照片刪除，或是這個字串不是照片
                        if(bitmapToString(imgpath) == null){
                            //如果等於null就代表這字串不是照片所以從資料庫刪除字串
                            database.delete(MyContract.MyTable1.TABLE_NAME,"imgpath=?",new String[]{imgpath});
                        }
                        else {
                            //else代表是一個可以解碼的照片
                            //bitmap = readBitMap(imgpath);
                            bitmap =bitmapToString(imgpath);
                            //使用createScaledBitmap從當前的圖片一定的比例創建一個新的位圖
                            smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                            //把這個點加入群集裡面因為這個點上面有拍照所以在點上建立一個照片的標記
                            mClusterManager.addItem(new MyItem(disFinalLat, disFinalLng, smallMarker, imgpath));
                            //mMap.addMarker(markerOptions.position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        }
                    }else{
                        //如果下一個位置沒有拍照的話就建立一個紅色的預設標記
                        //mMap.addMarker(markerOptions.position(new LatLng(disFinalLat,disFinalLng)).title("Independent location"));
                    }
                    //把traceOfMe陣列清空因為使用者已經離開上一個路徑，有可能接下來是新的一條路徑，所以清空繼續畫下一條新的路
                    if(!traceOfMe.isEmpty()){
                        traceOfMe.clear();
                    }
                }else{
                    //else代表下一個點還在路徑上所以繼續延續路線的繪畫
                    Log.i("brad","not if");
                    if(cursor.getString(cursor.getColumnIndex("imgPath"))!=null){
                        //imgpath用來儲存資料庫在欄位imgPath的路徑字串
                        imgpath = cursor.getString(cursor.getColumnIndex("imgPath"));
                        //使用deccodeFile(imgpath)來解碼該路徑的照片

                        //去判斷照片是否存在，因為使用者有可能把照片刪除，或是這個字串不是照片
                        if(getSmallBitmap(imgpath) == null){
                            //如果等於null就代表這字串不是照片所以從資料庫刪除字串
                            database.delete(MyContract.MyTable1.TABLE_NAME,"imgpath=?",new String[]{imgpath});
                        }
                        else {
                            //else代表是一個可以解碼的照片
                            //bitmap = readBitMap(imgpath);
                            bitmap =bitmapToString(imgpath);
                            Log.i("brad","imgpath = "+imgpath);
                            //Log.i("brad","bitmapToString(imgpath) = "+readBitMap(imgpath));
                            //使用createScaledBitmap從當前的圖片一定的比例創建一個新的位圖
                            smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                            //把這個點加入群集裡面因為這個點上面有拍照所以在點上建立一個照片的標記
                            if(mClusterManager!=null)
                            {
                                mClusterManager.addItem(new MyItem(disFinalLat, disFinalLng, smallMarker, imgpath));
                            }
                            //mMap.addMarker(markerOptions.position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        }
                    }
                    //呼叫trackToMe把下一個地點的路線畫出
                    trackToMe(disFinalLat,disFinalLng);

                }
                //把地圖的鏡頭移動到下一個位置
                lastMarker.setPosition(new LatLng(disFinalLat,disFinalLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(disFinalLat,disFinalLng), 15));
                //兩個點都運算完了，把下一個位置當作第一個位置，繼續算兩點是否距離過長
                disFirstLat = disFinalLat;
                disFirstLng = disFinalLng;
                //將指標移至下一筆資料
            }while (cursor.moveToNext());
            //alert.dismiss();
        }

        //群集管理聚集
        if(mClusterManager !=null)
        {
            mClusterManager.cluster();
        }
        //將標記物件關閉
        cursor.close();
        Log.i("brad","Now : loadDateRoute is running map is clear() and line is remove!");
        Log.i("brad","All :"+c.getCount()+" Record !");
        // Log.i("brad","Now : you select date have "+count+" Record !");
        // Log.i("brad","All    :"+"   "+c.getString(c.getColumnIndex("date"))+" Record !");
        // c.moveToNext();
        //Log.i("brad","All    :"+"   "+c.getString(c.getColumnIndex("date"))+" Record !");
        c.close();
        //getActivity().setTitle(startTime);
    }

    //此函式用來接收是否開放權限的結果*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("brad","onRequestPermissionsResult Call");
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == PERMISSMIONCODE) {
            // 當requestCode = PERMISSIONCODE時代表使用者已經決定是否開放所有權限(位置及儲存空間)
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // 不等於 PERMISSION_GRANTED 代表被按下拒絕
                Toast.makeText(view.getContext(),"I need LOCATION PERMISSION !",Toast.LENGTH_SHORT).show();
                super.onDestroy();
            }else{
                //當使用者開放權限後就進行onMapReady()
                mapFragment.getMapAsync(this);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("brad","onMapReady is called ");
        //設定地圖為googleMap
        mMap = googleMap;
        //設置使用者尚未抓到位置時的預設鏡頭位置
        //將地圖的鏡頭移動到台灣(經緯度23.786083, 120.952960大約位於南投縣的雙龍村)
        //經由鏡頭的縮放功能讓使用者的預設鏡頭為整個台灣
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(23.786083, 120.952960), 7));
        //清除地圖
        mMap.clear();
        //使用資料庫輔助物件獲得一個可讀可寫的空間
        //假如使用getWritableDatabase()如果使用者的空間不足寫入則會拋出例外
        //使用getReadableDatabase()如果使用者的空間不足只會進入唯讀模式還是可以讀出資料庫的內容
        Log.i("brad","getReadableDatabase() Call");
        database = mydbHelper.getReadableDatabase();
        if(checkLocationPermission()) {
            //開啟自己位置的小藍標
            mMap.setMyLocationEnabled(true);
            //把右下角一個類似定位的按鈕開啟
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            //使用ComponentName物件jobService來包裝MapsActivity class跟MyJobService class
            jobService = new ComponentName(view.getContext(), MyJobService.class);
            //使用Intent物件service來包裝MapsActivity class跟MyJobService class
            service = new Intent(view.getContext(), MyJobService.class);
            //當程式開啟之後startService(service)就開啟後台服務(偵聽使用者的位置)
           // pollServer();
            //getActivity().startService(service);
            //呼叫函式設定群集
            setRenderer();
        }else{
            //如果checkLocationPermission()回傳-1代表使用者已將某個權限關閉則在要求使用者開放權限
            requestPermission();
        }
    }

    //此函式可防止旋轉螢幕後重啟活動
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
       if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 什麼都不用寫
        }
        else {
            // 什麼都不用寫
        }
    }

    //捕捉返回鍵
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //當使用者按下返回件時呼叫ConfirmExit()，確認使用者是否結束應用程式
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //按返回鍵，則執行退出確認
            Log.i("brad: ","onKeyDown  Call !    KeyEvent.KEYCODE_BACK ");
            ConfirmExit();
            return true;
        }
        Log.i("brad: ","onKeyDown  Call !");
        return getActivity().onKeyDown(keyCode, event);
    }

    //當程式結束執行時呼叫onDestroy()關閉程式執行並關閉資料庫並且取消背景服務工作
    @Override
    public void onDestroy() {
        Log.i("brad","onDestroy is called ");
        //關閉資料庫
        //mydbHelper.close();
        //database.close();
        //停止背景服務
        //getActivity().stopService(service);
        //取消所有工作
        //scheduler.cancelAll();
        //結束時把地圖清空
        mMap.clear();
        //呼叫onDestroy
        firstMarker = null;
        lastMarker = null;
        super.onDestroy();
    }

    //addListenerOnButton在程式剛一開始執行的時候就創建並且將按鈕監聽的函式完成
    private void addListenerOnButton() {

        //btnSelect = (ImageButton) view.findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            //改寫onClick函式，當按下時會新增一個DatePickerDialog讓使用者選擇日期
            @Override
            public void onClick(View v) {
                //創建一個新的OnDateSetListener實例。 當用戶單擊DatePickerDialog中的“確定”按鈕時，將調用此偵聽器.
                DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    //建立選擇日期的監聽者並改寫函式
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        //將選擇的日期呼叫函式格式化之後使用Toast跳出選擇的日期
                        // selectDate = setDateFormat(year,monthOfYear,dayOfMonth);
                        // Toast.makeText(MapsActivity.this,selectDate+" choice !",Toast.LENGTH_SHORT).show();
                        //當選擇日期之後將呼叫載入路線的函式
                        Calendar cal = Calendar.getInstance();
                        cal.set(year,monthOfYear,dayOfMonth);
                        selectDateToLoad = cal.getTime();
                        loadDateRoute();
                    }
                };

                //獲得現在使用者的日期，將其載入DatePickerDialog預設的選項
                Calendar now = Calendar.getInstance();
                int year = now.get(java.util.Calendar.YEAR);
                int month = now.get(java.util.Calendar.MONTH);
                int day = now.get(java.util.Calendar.DAY_OF_MONTH);

                //創建一個新的datePickerDialog物件
                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(), onDateSetListener, year, month, day);

                //顯示datePickerDialog
                datePickerDialog.show();
            }
        });
    }

    //選擇日期之後，先將地圖和經緯度陣列清單清空後，載入資料庫資料並將兩個Marker放入地圖並畫出路線
    private void loadDateRoute() {
        //宣告四個double物件
        //用來計算下一個點是否距離過遠，來判斷下一個點是否為路徑上連續的點
        //如果距離太遠代表該點為不合理的點，有可能為使用者關掉定位後又開啟導致出現一個獨立的點或路徑
        double disFirstLat;
        double disFirstLng;
        double disFinalLat;
        double disFinalLng;
        String imgpath;
        //清空經緯度陣列這樣等等讀取檔案時使用trackToMe(lat,lng)時才不會也使用到目前在陣列裡面的位置資料
        //如果traceOfMe是null的話那就新增，如果traceOfMe不是空的則把裡面的資料清除
        if(traceOfMe==null){
            traceOfMe = new ArrayList<LatLng>();
        }
        if(!traceOfMe.isEmpty()){
            traceOfMe.clear();
        }
        //把標記都清除
//        mClusterManager.clearItems();
        //搜尋選擇日期所有的經緯度並畫出當初的路線
        //格式化選擇的日期並搜索資料庫
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        startTime = sdf.format(selectDateToLoad);
        //startTime=Name;
        //query是資料庫的語法
        //query (String table,String[] columns,String selection,String[] selectionArgs,String groupBy,String having,String orderBy)
        //table         =>用於編譯查詢的表名，該程式的表名為MapTable
        //columns       =>要返回的列的列表。傳遞null將返回所有列，不建議這樣做，以防止從不會使用的存儲中讀取數據。
        //selection     =>用於聲明要返回哪些行的過濾器，格式為SQL WHERE子句（不包括WHERE本身）。傳遞null將返回給定表的所有行。startTime為使用者選擇的時間
        //selectionArgs =>你要尋找的值
        //groupBy       =>一個聲明如何對行進行分組的過濾器，格式為SQL GROUP BY子句（不包括GROUP BY本身）。傳遞null將導致行不被分組。
        //having        =>如果使用行分組，過濾器將聲明要包括在游標中的行組，其格式設置為SQL HAVING子句（不包括HAVING本身）。傳遞null將導致包括所有行組，並且在不使用行分組時是必需的。
        //orderBy       =>如何對行進行排序，格式為SQL ORDER BY子句（不包括ORDER BY本身）。傳遞null將使用默認的排序順序，該順序可能是無序的。
        Cursor cursor = database.query(
                MyContract.MyTable1.TABLE_NAME,null,"date=?",new String[]{startTime},
            null,null,null);
        String[] columns={MyContract.MyTable1.LOCATION_DATE};
        Cursor c= database.query(true,MyContract.MyTable1.TABLE_NAME,columns,null,new String[]{},null,null,null,null);
        Log.i("brad","All :"+c.getCount()+" Record !");
        c.moveToFirst();
        //getCount()取得使用者搜尋的資料有幾筆符合
        int count = cursor.getCount();
        //使用Log跳出訊息方便開發者監控程式過程
        //   Log.i("brad","Now : your date is "+startTime);
        //顯示在android studio 的Log.i上面讓開發者可以知道尋找的資料有幾筆
        Log.i("brad","Now : you select date have "+count+" Record !");
        //當count = 0代表你選擇的日期沒有任何紀錄，所以使用Toast跳出訊息提醒使用者然後return直接跳出函式
        if(count == 0){
            Toast.makeText(view.getContext(),"There is no relevant record for the date you choose !",Toast.LENGTH_SHORT).show();
//            scheduler.cancelAll();
            //結束時把地圖清空
            mMap.clear();
            //呼叫onDestroy
            return;
        }
        //把地圖的物件全部清除
        mMap.clear();
        //建立一個Bitmap物件用來接收BitmapFactory.decodeFile(imgpath)所回傳的點陣圖
        //decodeFile用來解碼文件的一個函式
        //impath是存在資料庫的照片路徑字串
        Bitmap bitmap;
        //smallMarker用來縮放上面的bitmap物件
        Bitmap smallMarker;
        //將指標移至第一筆資料
        if (cursor.moveToFirst())
        {
            //建立一個經緯度的物件存放第一個地方的經緯度
            LatLng sydney = new LatLng(
                    cursor.getDouble(cursor.getColumnIndex("latitude")),
                    cursor.getDouble(cursor.getColumnIndex("longitude")));
            Log.i("brad","marker else");
            /*
            if(firstMarker.isVisible()&&lastMarker.isVisible()){
                Log.i("brad","marker is visible");
            }else{
                Log.i("brad","marker is unvisible");
                firstMarker.setVisible(true);
                lastMarker.setVisible(true);
            }*/
            firstMarker = HomeFragment.mMap.addMarker(HomeFragment.markerOptions.position(sydney).title("First place"));
            lastMarker = HomeFragment.mMap.addMarker(HomeFragment.markerOptions.position(sydney).title("Last place"));
            firstMarker.setPosition(sydney);
            lastMarker.setPosition(sydney);
            //如果初始位置也有拍照(imgPath != null)則把照片讀近來然後
            if(cursor.getString(cursor.getColumnIndex("imgPath"))!=null){
                //imgpath用來儲存資料庫在欄位imgPath的路徑字串
                imgpath = cursor.getString(cursor.getColumnIndex("imgPath"));
                //使用deccodeFile(imgpath)來解碼該路徑的照片
                // bitmap = readBitMap(imgpath);
                bitmap =bitmapToString(imgpath);
                //使用createScaledBitmap從當前的圖片一定的比例創建一個新的位圖
                smallMarker = Bitmap.createScaledBitmap(bitmap, 84, 84, false);
                //把這個點加入群集裡面因為這個點上面有拍照所以在點上建立一個照片的標記
                mClusterManager.addItem(new MyItem(sydney.latitude,sydney.longitude,smallMarker,imgpath));
            }
            //把選擇的日期的第一個位置經緯度存到disFirstLat跟disFirstLng
            disFirstLat = cursor.getDouble(cursor.getColumnIndex("latitude"));
            disFirstLng = cursor.getDouble(cursor.getColumnIndex("longitude"));
            //呼叫trackToMe()來畫路線
            trackToMe(disFirstLat,disFirstLng);
            do{
                //讀出那一列的所有資訊並且顯示到Logcat
                // int id = cursor.getInt(cursor.getColumnIndex("_id"));
                //  String d = cursor.getString(cursor.getColumnIndex("date"));
                //  String t = cursor.getString(cursor.getColumnIndex("time"));
                //  Log.i("brad","database data"+id+" = "+lat+" "+lng+" and time is "+d+" "+t);
                //disFinal來存下一個位置的經緯度
                disFinalLat = cursor.getDouble(cursor.getColumnIndex("latitude"));
                disFinalLng = cursor.getDouble(cursor.getColumnIndex("longitude"));
                //使用getDistanceBetween函式來運算disFirst和disFinal經緯度的距離，單位為米(M)
                //當兩點距離超過400公尺的時候代表下一點不是在使用者的連續路徑中，所以獨立建立一個標記點
                if(getDistanceBetween(new LatLng(disFirstLat,disFirstLng),new LatLng(disFinalLat,disFinalLng)) > 300){
                    //mMap.addMarker(markerOptions.position(new LatLng(disFirstLat,disFirstLng)).title("Independent location"));
                    //如果下一個位置有相片的記錄的話就建立相片標記

                    if(cursor.getString(cursor.getColumnIndex("imgPath"))!=null){
                        //imgpath用來儲存資料庫在欄位imgPath的路徑字串
                        imgpath = cursor.getString(cursor.getColumnIndex("imgPath"));
                        //使用deccodeFile(imgpath)來解碼該路徑的照片
                        //去判斷照片是否存在，因為使用者有可能把照片刪除，或是這個字串不是照片
                        if(bitmapToString(imgpath) == null){
                            //如果等於null就代表這字串不是照片所以從資料庫刪除字串
                            database.delete(MyContract.MyTable1.TABLE_NAME,"imgpath=?",new String[]{imgpath});
                        }
                        else {
                            //else代表是一個可以解碼的照片
                            //bitmap = readBitMap(imgpath);
                            bitmap =bitmapToString(imgpath);
                            //使用createScaledBitmap從當前的圖片一定的比例創建一個新的位圖
                            smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                            //把這個點加入群集裡面因為這個點上面有拍照所以在點上建立一個照片的標記
                            mClusterManager.addItem(new MyItem(disFinalLat, disFinalLng, smallMarker, imgpath));
                            //mMap.addMarker(markerOptions.position(new LatLng(lat,lng)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        }
                    }else{
                        //如果下一個位置沒有拍照的話就建立一個紅色的預設標記
                        mMap.addMarker(markerOptions.position(new LatLng(disFinalLat,disFinalLng)).title("Independent location"));
                    }
                    //把traceOfMe陣列清空因為使用者已經離開上一個路徑，有可能接下來是新的一條路徑，所以清空繼續畫下一條新的路
                    if(!traceOfMe.isEmpty()){
                        traceOfMe.clear();
                    }
                }else{
                    //else代表下一個點還在路徑上所以繼續延續路線的繪畫
                    Log.i("brad","not if");
                    if(cursor.getString(cursor.getColumnIndex("imgPath"))!=null){
                        //imgpath用來儲存資料庫在欄位imgPath的路徑字串
                        imgpath = cursor.getString(cursor.getColumnIndex("imgPath"));
                        //使用deccodeFile(imgpath)來解碼該路徑的照片

                        //去判斷照片是否存在，因為使用者有可能把照片刪除，或是這個字串不是照片
                        if(getSmallBitmap(imgpath) == null){
                            //如果等於null就代表這字串不是照片所以從資料庫刪除字串
                            database.delete(MyContract.MyTable1.TABLE_NAME,"imgpath=?",new String[]{imgpath});
                        }
                        else {
                            //else代表是一個可以解碼的照片
                            //bitmap = readBitMap(imgpath);
                            bitmap =bitmapToString(imgpath);
                            Log.i("brad","imgpath = "+imgpath);
                            //Log.i("brad","bitmapToString(imgpath) = "+readBitMap(imgpath));
                            //使用createScaledBitmap從當前的圖片一定的比例創建一個新的位圖
                            smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                            //把這個點加入群集裡面因為這個點上面有拍照所以在點上建立一個照片的標記
                            mClusterManager.addItem(new MyItem(disFinalLat, disFinalLng, smallMarker, imgpath));
                            //mMap.addMarker(markerOptions.position(new LatLng(disFinalLat,disFinalLng)).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        }
                    }
                    //呼叫trackToMe把下一個地點的路線畫出
                    trackToMe(disFinalLat,disFinalLng);

                }
                //把地圖的鏡頭移動到下一個位置
                lastMarker.setPosition(new LatLng(disFinalLat,disFinalLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(disFinalLat,disFinalLng), 10));
                //兩個點都運算完了，把下一個位置當作第一個位置，繼續算兩點是否距離過長
                disFirstLat = disFinalLat;
                disFirstLng = disFinalLng;
                //將指標移至下一筆資料
            }while (cursor.moveToNext());
        }
        //群集管理聚集
        mClusterManager.cluster();
        //將標記物件關閉
        cursor.close();
        Log.i("brad","Now : loadDateRoute is running map is clear() and line is remove!");
        Log.i("brad","All :"+c.getCount()+" Record !");
       // Log.i("brad","Now : you select date have "+count+" Record !");
       // Log.i("brad","All    :"+"   "+c.getString(c.getColumnIndex("date"))+" Record !");
       // c.moveToNext();
        //Log.i("brad","All    :"+"   "+c.getString(c.getColumnIndex("date"))+" Record !");
        c.close();
        getActivity().setTitle(startTime);
    }

    //畫路線函式
    public static void trackToMe(double lat, double lng){
        //檢查ArrayList是否為null，是的話建立一個ArrayList為LatLng型態
        if (traceOfMe == null) {
            traceOfMe = new ArrayList<LatLng>();
        }
        //將傳入的精度跟緯度加入ArrayList裡面
        traceOfMe.add(new LatLng(lat, lng));
        PolylineOptions polylineOpt = new PolylineOptions();
        //將ArrayList裡面的經緯度都丟給polylineOpt裡面畫線
        for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }
        //設定線的顏色
        polylineOpt.color(Color.YELLOW);
        //將線加入地圖
        line = mMap.addPolyline(polylineOpt);
        //設定線的寬度
        line.setWidth(10);
    }

    //因為使用者可以隨時更改權限，所以使用此函式每次開啟程式的時候都可以檢查使用者是否有取消某個權限
    private boolean checkLocationPermission() {
        //字串permission來儲存權限
        String[] permission = {
                "android.permission.ACCESS_WIFI_STATE",
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_WIFI_STATE",
                //Android版本太低可以把這個註解，但是如果是9或10就要把註解弄掉
                //"android.permission.ACCESS_BACKGROUND_LOCATION",
                // "android.permission.FOREGROUND_SERVICE",
                // "android.permission.ACCESS_MEDIA_LOCATION",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.ACCESS_FINE_LOCATION",
        };
        //int res = this.checkCallingOrSelfPermission(permission);
        int res = 0;
        //使用for迴圈確認是否每個權限都已開放
        for(int i =0; i < permission.length;i++){
            //checkCallingOrSelfPermission(permissionName)可以檢查權限是否開放會回傳1跟-1(1代表開放-1代表沒有)
            //使用int變數res來儲存權限是否有開放(-1代表有權限沒有被開放)
            res = view.getContext().checkCallingOrSelfPermission(permission[i]);
            if(res == -1){
                Log.i("brad",permission[i]);
                //當有權限沒有開放時則break跳出迴圈回傳-1
                break;
            }
            //Log.i("brad","return "+res);
        }
        //boolean a = res == PackageManager.PERMISSION_GRANTED;
        //Log.i("brad","return "+a);
        //回傳res告知是否有權限尚未開放
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    //要求權限函式
    //ACCESS_BACKGROUND_LOCATION是只有安卓10再用
    // 如果使用8的話要把Manifest.permission.ACCESS_BACKGROUND_LOCATION
    // ,|| permissionACCESS_BACKGROUND_LOCATION != PackageManager.PERMISSION_GRANTED
    //這兩行註解掉才有辦法使用
    private void requestPermission(){
        //當程式一開啟時檢查使用者是否已經開放該程式所需要的權限，若尚未開放則會詢問使用者是否開放
        //checkSelfPermission()確認是否有權限還沒開放
        //使用int變數來接收checkSelfPermission()回傳的值
        int permissionREAD_EXTERNAL_STORAGE    = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWRITE_EXTERNAL_STORAGE   = ContextCompat.checkSelfPermission(view.getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionACCESS_FINE_LOCATION     = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionFOREGROUND_SERVICE       = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.FOREGROUND_SERVICE);
        int permissionRECEIVE_BOOT_COMPLETED   = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.RECEIVE_BOOT_COMPLETED);
        // int permissionACCESS_MEDIA_LOCATION    = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_MEDIA_LOCATION);
        int permissionACCESS_BACKGROUND_LOCATION = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        //詢問使用者是否讓此程式使用手機內部儲存空間
        //如果其中一個權限 != PackageManager.PERMISSION_GRANTED(權限開放)則使用requestPermissions()要求開放權限
        if(permissionREAD_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED
                || permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED
                //|| permissionACCESS_BACKGROUND_LOCATION != PackageManager.PERMISSION_GRANTED
                || permissionFOREGROUND_SERVICE != PackageManager.PERMISSION_GRANTED
                || permissionRECEIVE_BOOT_COMPLETED != PackageManager.PERMISSION_GRANTED
                //|| permissionACCESS_MEDIA_LOCATION != PackageManager.PERMISSION_GRANTED
                || permissionACCESS_FINE_LOCATION!= PackageManager.PERMISSION_GRANTED){
            //requestPermissions()跳出權限視窗讓使用者選擇是否要開放權限
            requestPermissions(
                    new String[]{
                            //允許應用訪問後台位置。
                            //Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            //允許應用訪問精確位置
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            //允許應用訪問有關Wi-Fi網絡的信息
                            Manifest.permission.ACCESS_WIFI_STATE,
                            //允許應用程序打開網絡
                            Manifest.permission.INTERNET,
                            //允許應用程序訪問有關網絡的信息
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            //允許使用常規應用程序Service.startForeground開放前景服務
                            //用來解決背景接收位置限制的問題
                            Manifest.permission.FOREGROUND_SERVICE,
                            //允許應用程式可以讀跟寫手機的儲存空間
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECEIVE_BOOT_COMPLETED,
                    },PERMISSMIONCODE);
        }

    }

    //建立jobInfo並且加入工作
    private void pollServer() {
        scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int jobId;
        jobId = 1;
        jobInfo = new JobInfo.Builder(jobId, jobService)
                //.setPeriodic(50000)
                // 設定任務執行最少延遲時間
                .setMinimumLatency(1000)
                // 設定deadline，若到期還沒有達到規定的條件則會開始執行
                .setOverrideDeadline(1000)
                // 設定網路條件
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                // 設定是否充電的條件
                .setRequiresCharging(false)
                // 設定手機是否空閒的條件
                .setRequiresDeviceIdle(false)
                .setPersisted(true)
                .build();
        //加入工作
        scheduler.schedule(jobInfo);
    }


    //此函式用於新增對話窗，確認使用者是否離開應用程式
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(view.getContext());
        ad.setTitle("離開");
        ad.setMessage("確定要離開此程式嗎?");
        ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                getActivity().finish();//關閉activity
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });
        ad.setNeutralButton("背景執行",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int i) {
                //新增一個返回桌面的意圖
                //當使用者選擇背景執行的時後返回桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                //執行意圖
                startActivity(intent);
            }
        });
        ad.show();//顯示對話框
    }

    //此函式會回傳目前使用者的地圖
    private GoogleMap getMap(){
        return mMap;
    }

    //此函式用來運算兩個經緯度之間的距離
    public  Double getDistanceBetween(LatLng latLon1, LatLng latLon2) {
        if (latLon1 == null || latLon2 == null)
            return null;
        float[] result = new float[1];
        //distanceBetween為Location物件的靜態函式所以可以直接呼叫來計算兩經緯度的距離
        //distanceBetween (double startLatitude,double startLongitude,double endLatitude,double endLongitude,float[] results)
        //計算出的距離存儲在results [0]中。如果結果的長度為2或更大，則初始方位將存儲在results [1]中。如果結果的長度為3或更大，則最終軸承將存儲在results [2]中。
        Location.distanceBetween(latLon1.latitude, latLon1.longitude,
                latLon2.latitude, latLon2.longitude, result);
        Log.i("brad","Distance = "+result[0]);
        //回傳運算之後的長度
        return (double) result[0];
    }

    //此函式用來設定群集，開李敬程式從這裡面改
    public void setRenderer(){
        //設定群集管理者，項目為MyItem然後getMap()回傳mMap讓管理者可以在地圖上面動作
        mClusterManager = new ClusterManager<MyItem>(view.getContext(), getMap());
        //宣告一個渲染的物件，用來改變原本標記的Icon
        MyItemRenderer myItemRenderer = new MyItemRenderer(view.getContext(),mMap,mClusterManager);
        //設定相機的監聽者
        mMap.setOnCameraIdleListener(mClusterManager);
        //設定當標記旁邊最少有幾個標記才會縮起來
        //預設是4個，使用setMinClusterSize來改變
        //目前只有標記旁有一個標記夠近的話就會縮為一個
        //當使用者放的夠大的話就會分開變為兩個標記
        myItemRenderer.setMinClusterSize(1);
        //管理者設定渲染物件為myItemRenderer
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            //當單張照片的標記被點擊的時候就會呼叫這個
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
                Log.i("brad","onClusterItemClick");
                //建立一個ArrayList來儲存標記裡面照片的路徑
                ArrayList<String> pathList = new ArrayList<String>();
                pathList.clear();
                //將那張照片的路徑存入ArrayList裡面
                pathList.add(myItem.myItemImgPath);
                //Bundle可以用來傳遞活動之間的資料
                Bundle bundle=new Bundle();
                Intent intent=new Intent(view.getContext(), imageall.class);//進入相簿
                bundle.putStringArrayList("ImgPositionArrList",pathList);//key:ImgPositionArrList 存入bundle為arrayListIMG 而到另一個活動時，須以同一把key才能取出這裡傳出 的arrayListIMG
                intent.putExtras(bundle);//將bundle載入intent
                startActivity(intent);//開始下一個相簿活動
                Log.i("brad","onClusterClick");
                return false;
            }
        });
        //設定群集標記的監聽者
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            //當多張照片的標記被點擊的時候就會呼叫這個
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                //建立一個ArrayList來儲存標記裡面的所有照片的路徑
                ArrayList<String> pathList = new ArrayList<String>();
                pathList.clear();
                //用for迴圈來一個一個加入ArrayList裡面
                //getItems()會回傳有幾個照片標記聚集了
                for (MyItem p : cluster.getItems()) {
                    pathList.add(p.myItemImgPath);
                }
                //Bundle可以用來傳遞活動之間的資料
                Bundle bundle=new Bundle();
                Intent intent=new Intent(view.getContext(),imageall.class);//進入相簿
                bundle.putStringArrayList("ImgPositionArrList",pathList);//key:ImgPositionArrList 存入bundle為arrayListIMG 而到另一個活動時，須以同一把key才能取出這裡傳出 的arrayListIMG
                intent.putExtras(bundle);//將bundle載入intent
                startActivity(intent);//開始下一個相簿活動
                Log.i("brad","onClusterClick");
                return false;
            }
        });
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(myItemRenderer);
    }

    //計算圖片的縮放值
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final  int height = options.outHeight;//照片高度
        final int width = options.outWidth;//照片寬度
        int inSampleSize = 1;//縮小比例
        //看哪個比較大if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

        return inSampleSize;
    }

    // 根據路徑獲得圖片並壓縮，返回bitmap用於顯示//480,800
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 50, 50);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    //把bitmap轉換成String
    public static Bitmap bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        return bm;
    }

    //用最省的方式讀去照片
    public static Bitmap readBitMap(String filePath){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = 250;
        return BitmapFactory.decodeFile(filePath ,opt );
    }

    //此函式用於新增對話窗，loading畫面
    public void buildloading() {

         progressDialog=new ProgressDialog(view.getContext());
         progressDialog.setMessage("loading...");
         progressDialog.setCanceledOnTouchOutside(false);
         progressDialog.show();
        /*LayoutInflater inflater = LayoutInflater.from(view.getContext());
        final View v = inflater.inflate(R.layout.loading, null);
        dd=new AlertDialog.Builder(view.getContext());
        alert = dd.create();
        mLoadingBar = (ProgressBar) v.findViewById(R.id.progressbar);
        mLoadingBar.setVisibility(View.VISIBLE);
        dd.setTitle("loading.....");
        dd.setView(v);
        dd.show();*/
    }

    private Runnable r1=new Runnable () {

        public void run() {
            //buildloading();
           drawloaddate();
            progressDialog.dismiss();
        }

    };
}