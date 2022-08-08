package com.project.traveltrack.ui.dashboard;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CustomImageAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.model.LatLng;
import com.project.traveltrack.AnotherMyContract;
import com.project.traveltrack.AnotherMydbHelper;
import com.project.traveltrack.MyAdapter;
import com.project.traveltrack.MyContract;
import com.project.traveltrack.MydbHelper;
import com.project.traveltrack.R;
import com.project.traveltrack.ui.home.HomeFragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//查詢所有行程以及更換活動名稱的活動
public class DashboardFragment extends Fragment {
    //ListView的選擇器
    public MyAdapter adapter;
    //條列式顯示紀錄時間
    public ListView listView;
    //查詢的按鈕
    private ImageButton search;
    //查詢的按鈕
    private Button search2;

    //儲存查詢的日期
    public static ArrayList date=new ArrayList();
    //所有的行程日期
    public  ArrayList<String> datename;
    //顯示你要刪除的日期
    private TextView showdate;
    //顯示要出現在textview上的日期
    public static String starTime="";
    //anothermydbHelperr可以用來輔助資料庫物件，例如建立資料庫或更新資料庫版本等等
    public static AnotherMydbHelper anothermydbHelper;
    //anotherdatabase資料庫用來儲存資料(日期，日期名稱)
    public static SQLiteDatabase anotherdatabase;
    //回傳Fragment的VIEW
    public  View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        //宣告listView Layout lisitem.xml
        listView = (ListView) root.findViewById(R.id.listView);
        //search按鈕
        search = (ImageButton) root.findViewById(R.id.search);
        //search按鈕聆聽者
        search.setOnClickListener(searchListner);
        //search按鈕
        search2=(Button) root.findViewById(R.id.showdate);
        //search按鈕聆聽者
        search2.setOnClickListener(searchListner);

        anothermydbHelper = new AnotherMydbHelper(root.getContext(),1);

        anotherdatabase=anothermydbHelper.getReadableDatabase();

        HomeFragment.mydbHelper = new MydbHelper(root.getContext(), 1);
        Log.i("brad", "DashboardFragment    MydbHelper Call");

        HomeFragment.database = HomeFragment.mydbHelper.getReadableDatabase();

        showdate = (TextView) root.findViewById(R.id.showday);
        //假設StarTime=="" 代表你沒選擇行程
        if(starTime=="")
        {

        }
        else
        {
            showdate.setText(starTime);
        }

        searchhelp();
        adapter = new MyAdapter(datename,root,this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onClickListView);
        //deletdate.setOnClickListener(deletListner);
        //check = (ImageButton) root.findViewById(R.id.check);
        //check.setOnClickListener(checkListenr);
        //ArrayAdapter <CharSequence> adEduList=ArrayAdapter.createFromResource(root.getContext(),date,);

        return root;

    }

    //搜尋所有行程的日期
    public void searchhelp() {
        //要回傳的日期
        String[] columns = {MyContract.MyTable1.LOCATION_DATE};
        //String[] cs = {AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE};
        //收尋資料庫裡有的日期
        Cursor cursor = HomeFragment.database.query(true, MyContract.MyTable1.TABLE_NAME, columns, null, new String[]{}, null, null, null, null);
           /* Cursor c = anotherdatabase.query(
                    AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME,null,"date=?",new String[]{},
                    null,null,null);*/
        //String[] cs = {AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE};
        //Cursor  c = DashboardFragment.anotherdatabase.query(true,AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME, cs, null, new String[]{}, null, null, null, null);
        //count = c.getCount();
        //int cc=c.getCount();
      //  Log.i("brad", "searchhelp   anotherdatabase  c.getCount() : " + cc);
       // c.close();
        //所有日期的數量
        int count = cursor.getCount();
        //宣告一個儲存所有日期的ArrayList
        datename=new ArrayList<String>();
        //開始儲存
        cursor.moveToFirst();
        if(count!=0)
        {
            Log.i("brad", " searchhelp  cursor.getCount() : " + count);
            do {
                datename.add(cursor.getString(cursor.getColumnIndex("date")));
                Log.i("brad", "searchhelp cursor.getString(cursor.getColumnIndex()) : " + cursor.getString(cursor.getColumnIndex("date")));
                //Log.i("brad", "Frist   searchListner   :" + "   " + cursor.getString(cursor.getColumnIndex("date"))+"----"+Name.get(i) + "   Record !");
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    //查詢鍵的聆聽者
    private View.OnClickListener searchListner = new View.OnClickListener() {

        @Override
        //按下Button事件時會進入這個 function
        public void onClick(View v) {
            String[] columns = {MyContract.MyTable1.LOCATION_DATE};
            Cursor cursor = HomeFragment.database.query(true, MyContract.MyTable1.TABLE_NAME, columns, null, new String[]{}, null, null, null, null);
            int count = cursor.getCount();
            Toast.makeText(getContext(), "行程總共數量:"+count+"個", Toast.LENGTH_SHORT).show();
            cursor.close();
            //adapter = new MyAdapter(datename,root);
            //listView.setAdapter(adapter);
            //listView.setOnItemClickListener(onClickListView);
            //listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, datename);
            //listView.setAdapter(listAdapter);
            //listView.setOnItemClickListener(onClickListView);
            //startTime = "";
            Log.i("brad", " searchListner   onClick  search!    "+datename.size()+"");
        }

    };

    //點選日期
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            //Nameposition = position;
            //SpannableString s = new SpannableString((String) datename.get(position));
            //travelname.setHint(s);
            Toast.makeText(getContext(), "點選第 " + (position + 1) + " 個 \n內容： " + datename.get(position), Toast.LENGTH_SHORT).show();
            Log.i("brad", "datename.get(position) : "+datename.get(position));
            // travelname.setText(datename[position]);
            //showdate.setText((String) datename.get(position));
           // startTime = (String) datename.get(position);
            //travelname.setText("");
        }
    };

    //結束活動時會做的
    @Override
    public void onDestroy() {
//        starTime=(String)showdate.getText();
       // HomeFragment.mydbHelper.close();
       // HomeFragment.database.close();
//        Log.i("brad", "showdate.toString() "+showdate.getText());
        Log.i("brad", "DashboardFragment   onDestroy  is called ");
        super.onDestroy();
    }


}