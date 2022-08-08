package com.project.traveltrack;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.traveltrack.ui.dashboard.DashboardFragment;
import com.project.traveltrack.ui.home.HomeFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    //ComponentName 組件名稱，透過調用Intent中的setComponent方法，我們可以打開另外一個活動或服務
    public ComponentName jobService;

    //JobInfo是用來包裝工作的條件、開始時間、截止時間、具體的工作等等要設定的參數
    public JobInfo jobInfo;

    //JobScheduler 處理 APP 在後台時資料更新，且可規定只有在裝置街上插頭電源和連接 wifi 時在進行資料下載更新
    public static JobScheduler scheduler;

    public static boolean Mutex=true;

    public static boolean lock=true;

    private FragmentManager fmanager;

    private FragmentTransaction ftransaction;
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        //service = new Intent(this, MyJobService.class);
    }

    public void gotoDownloadFragment() {    //去下载页面
        fmanager = getSupportFragmentManager();
        ftransaction = fmanager.beginTransaction();
        Fragment homeFragment = new HomeFragment();
        ftransaction.replace(R.id.navigation_home, homeFragment);
        ftransaction.commit();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //當使用者按下返回件時呼叫ConfirmExit()，確認使用者是否結束應用程式
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //按返回鍵，則執行退出確認
            Log.i("brad: ","onKeyDown  Call !    KeyEvent.KEYCODE_BACK ");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            //執行意圖
            startActivity(intent);
            //ConfirmExit();
            return true;
        }
        Log.i("brad: ","onKeyDown  Call !");
        return super.onKeyDown(keyCode, event);
    }

    //此函式用於新增對話窗，確認使用者是否離開應用程式
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(this);
        ad.setTitle("離開");
        ad.setMessage("確定要離開此程式嗎?");
        ad.setNegativeButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub
                stopService(HomeFragment.service);
//                scheduler.cancelAll();
                //onDestroy();
                finish();//關閉activity
            }
        });

        ad.setPositiveButton("否",new DialogInterface.OnClickListener() {
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


    //當程式結束執行時呼叫onDestroy()關閉程式執行並關閉資料庫並且取消背景服務工作
    @Override
    public void onDestroy() {
        Log.i("brad","MainActivity onDestroy is called ");
        //關閉資料庫
        HomeFragment.mydbHelper.close();
        HomeFragment.database.close();
        if(DashboardFragment.anothermydbHelper!=null&&DashboardFragment.anotherdatabase!=null)
        {
            DashboardFragment.anothermydbHelper.close();
            DashboardFragment.anotherdatabase.close();
        }
        if(scheduler!=null)
        {
            scheduler.cancelAll();
        }
        stopService(HomeFragment.service);

    //       HomeFragment.scheduler.cancelAll();
        //取消所有工作
       //finish();
        super.onDestroy();
        /*在Logcat標籤為brad跳出提示訊息告訴開發人員程式已經停止執行、資料庫已關閉*/
        Log.i("brad","MainActivity prog is Destroy and removeLocationUpdates database mydbHelper is close");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.stop://監聽選單按鈕
                if(checkLocationPermission()) {
                    if(Mutex==true)
                    {
                        Mutex=false;
                        if(lock==false)
                        {
                            Toast.makeText(this, "暫停", Toast.LENGTH_SHORT).show();
                            stopService(HomeFragment.service);
                            // HomeFragment.mydbHelper.close();
                            // HomeFragment.database.close();
                            scheduler.cancelAll();
                            lock=true;
                        }
                        else
                        {
                            Toast.makeText(this, "旅途尚未開始", Toast.LENGTH_SHORT).show();
                        }
                        Mutex=true;
                    }

                }
                else
                {
                    Toast.makeText(this, "你還沒有開啟權限", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.begin:
                if(checkLocationPermission()) {
                    if(Mutex==true)
                    {
                        Mutex=false;
                    if(lock!=true)
                    {
                        Toast.makeText(this, "旅途已開始", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(this, "開始", Toast.LENGTH_SHORT).show();
                        //HomeFragment.mydbHelper = new MydbHelper(this, 1);
                        HomeFragment.database = HomeFragment.mydbHelper.getReadableDatabase();
                        jobService=new ComponentName(this, MyJobService.class);
                        pollServer();
                        startService(HomeFragment.service);
                        lock=false;
                    }
                        Mutex=true;
                    }

                }
                else
                {
                    Toast.makeText(this, "你還沒有開啟權限", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.leave:
                //Toast.makeText(this,"確定要結束程式 ?", Toast.LENGTH_SHORT).show();
                ConfirmExit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //建立jobInfo並且加入工作
    private void pollServer() {
        scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
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

    private boolean checkLocationPermission() {
        //字串permission來儲存權限
        String[] permission = {
                "android.permission.ACCESS_WIFI_STATE",
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_WIFI_STATE",
                //Android版本太低可以把這個註解，但是如果是9或10就要把註解弄掉
                //"android.permission.ACCESS_BACKGROUND_LOCATION",
                //"android.permission.FOREGROUND_SERVICE",
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
            res = this.checkCallingOrSelfPermission(permission[i]);
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

}
