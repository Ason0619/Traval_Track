package com.project.traveltrack;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;

import com.project.traveltrack.ui.dashboard.DashboardFragment;
import com.project.traveltrack.ui.home.HomeFragment;
import com.project.traveltrack.ui.home.imageall;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
//創立ListView
public class MyAdapter extends BaseAdapter implements View.OnClickListener {
    //上下文
    private Context context;
    //我要的行程名稱位置
    private int position;
    //把DashboardFragment view傳過來
    private View root;
    //数据项
    //全部行程的日期
    private List<String> data;
    //其中一個行程的名字
    private String Name;
    //search的頁面
    private DashboardFragment dashboardFragment;

    //建構子 分別為 全部行程的日期的List， 一個view ，和search的頁面
    public MyAdapter(List<String> data,View count, DashboardFragment dashboardFragment){
        this.root=count;
        this.data = data;
        this.dashboardFragment=dashboardFragment;

        Log.i("brad","MyAdapter data:"+data);
    }
    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(context == null)
            context = viewGroup.getContext();
        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem,null);
            viewHolder = new ViewHolder();
            viewHolder.mTv = (TextView)view.findViewById(R.id.show);
            viewHolder.mBtn = (ImageButton)view.findViewById(R.id.change1);
            viewHolder.mBtn1 = (ImageButton)view.findViewById(R.id.delete2);
            viewHolder.mBtn2 = (ImageButton)view.findViewById(R.id.imageButton);

            //viewHolder.photo=(ImageView)view.findViewById(R.id.travelphoto);
            view.setTag(viewHolder);
        }
        //获取viewHolder实例
        viewHolder = (ViewHolder)view.getTag();
        //要回傳的欄位(日期，行程名稱)
        String[] cs = {AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE,AnotherMyContract.AnotherMyTable1.SROKE_PATH};
        Cursor  c = DashboardFragment.anotherdatabase.query(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME, cs, "date=?", new String[]{data.get(i)}, null, null, null, null);
        int ct = c.getCount();
        Log.i("brad","  MyAdapter  c.getCount();    : "+ ct);
        c.moveToFirst();
        String s=data.get(i);
        int j=0;
        do {
            //假設有行程名稱資料時進入ct != 0
            if(ct != 0) {
                //將此日期用NN來接
                String NN=c.getString(c.getColumnIndex("date"));
                Log.i("brad", "i "+i+"   "+c.getString(c.getColumnIndex("date")));
                Log.i("brad",data.get(i));
                Log.i("brad","MyAdapter  c.getString(c.getColumnIndex(strokePath))  :"+c.getString(c.getColumnIndex("strokePath")));
                //如果日期相同代表找到我們要的行程名稱
                if (NN.equals(s)){
                    //將NAME帶入
                    Name = (i+1) + ". " + data.get(i) + "-" + c.getString(c.getColumnIndex("strokePath"));
                    break;
                }

            }
            //假設沒有行程名稱資料時進入else
            else{
                //代表沒有東西
                Name= (i+1)+". "+data.get(i);
                Log.i("brad","else   Name : "+ Name);
            }
            Log.i("brad","MyAdapter  j  : "+j);
            j++;
        }while (c.moveToNext());

        if (Name==null) {
            Name = (i + 1) + ". " + data.get(i);
        }
        c.close();

        /*String[] columns={MyContract.MyTable1.IMAGE_PATH};

        Cursor cursor = HomeFragment.database.query(
                MyContract.MyTable1.TABLE_NAME,columns,"date=?",new String[]{data.get(i)},
                null,null,null);
        int counter1 = cursor.getCount();
        Log.i("brad","counter  :"+counter1);
        String imgpath="123";
        if(cursor.moveToFirst())
        {
            //  Log.i("brad","cursor.getString(cursor.getColumnIndex(imgPath))  :"+cursor.getString(cursor.getColumnIndex("imgPath")));
            do {
                if(cursor.getString(cursor.getColumnIndex("imgPath"))!=null){
                    //imgpath用來儲存資料庫在欄位imgPath的路徑字串
                    imgpath = cursor.getString(cursor.getColumnIndex("imgPath"));
                    //使用deccodeFile(imgpath)來解碼該路徑的照片
                    Log.i("brad","MyAdapter  imgpath : "+ imgpath);
                    //去判斷照片是否存在，因為使用者有可能把照片刪除，或是這個字串不是照片
                    if(getSmallBitmap(imgpath) == null){
                        //如果等於null就代表這字串不是照片所以從資料庫刪除字串
                        HomeFragment.database.delete(MyContract.MyTable1.TABLE_NAME,"imgpath=?",new String[]{imgpath});
                    }
                    else {
                        Bitmap bitmap =bitmapToString(imgpath);
                        viewHolder.photo.setImageBitmap(bitmap);
                        Log.i("brad","MyAdapter  imgpath : "+ imgpath);
                        break;
                    }
                }
                else
                {
                    Log.i("brad","MyAdapter  imgpath : "+ imgpath);
                }
            }while (cursor.moveToNext());

            Log.i("brad","MyAdapter  imgpath : "+ imgpath);
            if(imgpath=="123")
            {
                Log.i("brad","NO PHOTO 123");
                Log.i("brad","MyAdapter  imgpath   123: "+ imgpath);
                viewHolder.photo.setImageResource(R.drawable.ic_card_travel_black);
            }

        }
        else {
            Log.i("brad","NO PHOTO ");
            viewHolder.photo.setImageResource(R.drawable.ic_card_travel_black);
        }
        cursor.close();*/
        //设置数据
        viewHolder.mTv.setText(Name);
        //设置监听事件
        viewHolder.mTv.setOnClickListener(this);
        //设置tag标记
        viewHolder.mBtn.setTag(R.id.change1,i);//添加此代码
        //设置tag标记
        viewHolder.mBtn1.setTag(R.id.delete2,i);//添加此代码

        viewHolder.mBtn2.setTag(R.id.imageButton,i);//添加此代码
        //设置数据
        //viewHolder.mBtn.setText("点我点我"+ i);
        viewHolder.mBtn.setOnClickListener(this);

        viewHolder.mBtn1.setOnClickListener(this);

        viewHolder.mBtn2.setOnClickListener(this);

        Log.i("brad", " i:   "+i);

        return view;

    }

    //設定監聽
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //更改名程
            case R.id.change1:
                int j = (int)view.getTag(R.id.change1);
                Log.d("brad", "R.id.change1_onClick: " + "view = " + view+"   j:"+j);
                Log.d("brad", "data.get(j) :  " + data.get(j));
               // Toast.makeText(context,"資料改寫   "+(j+1) + data.get(j),Toast.LENGTH_SHORT).show();
                position=j;
                LayoutInflater inflater = LayoutInflater.from(context);
                final View v = inflater.inflate(R.layout.alertdialog_use, null);
                //editText.getText().toString();
                buildchang();
                /*Bundle bundle=new Bundle();
                Intent intent=new Intent(view.getContext(), writein.class);//進入改寫頁面
                bundle.putString("data",(String) data.get(j));//放入改寫日期
                intent.putExtras(bundle);
                context.startActivity(intent);*/
                break;
                //刪除行程
            case R.id.delete2:
                int h = (int)view.getTag(R.id.delete2);
                Log.d("brad", "R.id.delete2_onClick: " + "view = " + view);
                //Toast.makeText(context,"確定刪除  ?"+(h+1),Toast.LENGTH_SHORT).show();
                position=h;
                ConfirmExit();
                //HomeFragment.database.delete(MyContract.MyTable1.TABLE_NAME,"date=?",new String[]{data.get(h)});
                break;
                //顯示路線
            case R.id.imageButton:
                int t = (int)view.getTag(R.id.imageButton);
                TextView tv;
                tv=(TextView) root.findViewById(R.id.showday);
                Log.d("brad", "R.id.delete2_onClick: " + "view = " + view);
                Toast.makeText(context,"按右上角的畫筆圖示開始繪畫行程",Toast.LENGTH_SHORT).show();
                //HomeFragment.database.delete(MyContract.MyTable1.TABLE_NAME,"date=?",new String[]{data.get(h)});
                HomeFragment.Name=data.get(t);
                DashboardFragment.starTime="Now Track:"+data.get(t);
                tv.setText("Now Track:"+data.get(t));
                NavHostFragment.findNavController(dashboardFragment) // NavHostFragment 方式获取 NavController
                    .navigate(R.id.navigation_home);// 根据 fragment 的 id 跳转
             //   MainActivity  mainActivity = (MainActivity) root.getContext();
               // mainActivity. gotoDownloadFragment ();
                break;
        }
    }


    //id名稱
    static class ViewHolder{
        TextView mTv;
        ImageButton mBtn;
        ImageButton mBtn1;
        ImageButton mBtn2;

    }

    //此函式用於新增對話窗，確認使用者是否刪除行程
    public void ConfirmExit(){//退出確認
        AlertDialog.Builder ad=new AlertDialog.Builder(context);
        ad.setTitle("確定要刪除 "+data.get(position)+" 旅程 ?");
        ad.setIcon(R.drawable.ic_delete_black_24dp);
        ad.setNegativeButton("是", new DialogInterface.OnClickListener() {//退出按鈕
            public void onClick(DialogInterface dialog, int i) {
                // TODO Auto-generated method stub

                HomeFragment.database.delete(MyContract.MyTable1.TABLE_NAME,"date=?",new String[]{data.get(position)});
                DashboardFragment.anotherdatabase.delete(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME, "date=?", new String[]{data.get(position)});
                data.remove(position);
                dashboardFragment.adapter.notifyDataSetChanged();
            }
        });
        ad.setPositiveButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不退出不用執行任何操作
            }
        });

        ad.show();//顯示對話框
    }

    //此函式用於新增對話窗，確認使用者是否更改名稱
    public void buildchang() {

        LayoutInflater inflater = LayoutInflater.from(context);
        final View v = inflater.inflate(R.layout.alertdialog_use, null);
        AlertDialog.Builder ad=new AlertDialog.Builder(context);
        EditText editText = (EditText) (v.findViewById(R.id.editText1));
        editText.setText(searchName(position));
        ad.setTitle("更改"+data.get(position)+"名稱");
        ad.setView(v);
        ad.setIcon(R.drawable.ic_create_black_24dp);
        ad.setPositiveButton("否",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                //不更改不用執行任何操作
            }
        });
        ad.setNegativeButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //editText.getText().toString();
                //editText.setText(searchName(position));
                String[] columns = {AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE};
                Cursor cursor = DashboardFragment.anotherdatabase.query(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME, columns, "date=?", new String[]{data.get(position)}, null, null, null, null);

                int count = cursor.getCount();

                Log.i("brad","MyAdapter  count:"+count);
                if(count==0)
                {
                    String travelName= editText.getText().toString()+"";
                    //將經緯度日期時間照片路徑丟到值裡面
                    ContentValues values = new ContentValues();

                    values.put(AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE,data.get(position));
                    values.put(AnotherMyContract.AnotherMyTable1.SROKE_PATH,travelName);
                    Log.i("brad","MyAdapter AnotherMyContract.AnotherMyTable1.SROKE_PATH :"+travelName);
                    //建立一個newRowId來接收新增的資料ID
                    long newRowId;
                    //databace.insert()把剛剛的values加入所建立的資料庫裏面
                    newRowId = DashboardFragment.anotherdatabase.insert(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME,null,values);
                    //假如回傳的不是-1而是一個正常的值則代表資料存入資料庫，如為-1則代表資料存入失敗
                    if(newRowId != -1){
                        Log.i("brad","checkListner data is save");
                    }else{
                        Log.i("brad","checkListner data is not save");
                    }
                }
                else {
                    long newRowId;
                    String travelName=editText.getText().toString()+"";
                    //將經緯度日期時間照片路徑丟到值裡面
                    ContentValues values = new ContentValues();

                    values.put(AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE,data.get(position));
                    values.put(AnotherMyContract.AnotherMyTable1.SROKE_PATH,travelName);
                    Log.i("brad","AnotherMyContract.AnotherMyTable1.SROKE_PATH :"+travelName);
                    DashboardFragment.anotherdatabase.delete(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME,"date=?",new String[]{data.get(position)});

                    newRowId = DashboardFragment.anotherdatabase.insert(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME,null,values);

                    //newRowId= DashboardFragment.anotherdatabase.update(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME,values,"strokePath",null);

                    if(newRowId != -1){
                        Log.i("brad","count  !=0 checkListner data is save");
                    }else{
                        Log.i("brad","count  !=0 checkListner data is not save");
                    }
                }
                //DashboardFragment.anotherdatabase.close();
                cursor.close();
                dashboardFragment.adapter.notifyDataSetChanged();
            }
        });
                ad.show();
    }

    //搜尋行程名稱 回傳 行程名稱
    public String searchName(int i) {
        String Name1="";
        String[] cs = {AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE,AnotherMyContract.AnotherMyTable1.SROKE_PATH};
        Cursor  c = DashboardFragment.anotherdatabase.query(true,AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME, cs, "date=?", new String[]{data.get(i)}, null, null, null, null);
        int ct = c.getCount();
        Log.i("brad","  MyAdapter  searchName   c.getCount();    : "+ ct);
        c.moveToFirst();
        String s=data.get(i);
        int j=0;
        do {
            if(ct != 0) {
                String NN=c.getString(c.getColumnIndex("date"));
                Log.i("brad", "i "+i+"   "+c.getString(c.getColumnIndex("date")));
                Log.i("brad",data.get(i));
                Log.i("brad","MyAdapter searchName  c.getString(c.getColumnIndex(strokePath))  :"+c.getString(c.getColumnIndex("strokePath")));
                if (NN.equals(s)){
                    Name1 =  c.getString(c.getColumnIndex("strokePath"));
                }

            }
            else{
                Name1= "";
                Log.i("brad","else searchName  Name : "+ Name1);
            }
            Log.i("brad","MyAdapter searchName j  : "+j);
            j++;
        }while (c.moveToNext());

        c.close();
        return Name1;
    }

}