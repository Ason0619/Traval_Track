package com.project.traveltrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.project.traveltrack.ui.dashboard.DashboardFragment;
import com.project.traveltrack.ui.home.HomeFragment;

public class writein extends AppCompatActivity {

    private EditText travelname;

    private ImageButton check;

    private ImageButton remove;

    private Bundle bundle;

    private String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writein);


        check = (ImageButton) findViewById(R.id.check);
        check.setOnClickListener(checkListner);

        remove = (ImageButton) findViewById(R.id.remove);
        remove.setOnClickListener(removeListner);

        travelname = (EditText) findViewById(R.id.editText);

        bundle = this.getIntent().getExtras();
        //date為點選日期
        date=bundle.getString("data");

        this.getSupportActionBar().setTitle(date+"新增旅行名稱");

        SpannableString s = new SpannableString(date);
        travelname.setHint(s);
    }

    //確認鍵的聆聽者
    private View.OnClickListener checkListner = new View.OnClickListener() {

        @Override
        //按下Button事件時會進入這個 function
        public void onClick(View v) {
            Toast.makeText(writein.this,"已更改"+travelname.getText() , Toast.LENGTH_SHORT).show();

            Log.i("brad","checkListner Name:"+date);
            String[] columns = {AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE};
            Cursor cursor = DashboardFragment.anotherdatabase.query(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME, columns, "date=?", new String[]{date}, null, null, null, null);

           /*Cursor cursor = DashboardFragment.anotherdatabase.query(
                    AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME,null,"date=?",new String[]{date},
                    null,null,null);*/

            int count = cursor.getCount();

            Log.i("brad","checkListner  count:"+count);
            if(count==0)
            {
                String travelName= travelname.getText()+"";
                //將經緯度日期時間照片路徑丟到值裡面
                ContentValues values = new ContentValues();

                values.put(AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE,date);
                values.put(AnotherMyContract.AnotherMyTable1.SROKE_PATH,travelName);
                Log.i("brad","AnotherMyContract.AnotherMyTable1.SROKE_PATH :"+travelName);
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
                String travelName=travelname.getText()+"";
                //將經緯度日期時間照片路徑丟到值裡面
                ContentValues values = new ContentValues();

                values.put(AnotherMyContract.AnotherMyTable1.ANOTHERLOCATION_DATE,date);
                values.put(AnotherMyContract.AnotherMyTable1.SROKE_PATH,travelName);
                Log.i("brad","AnotherMyContract.AnotherMyTable1.SROKE_PATH :"+travelName);
                DashboardFragment.anotherdatabase.delete(AnotherMyContract.AnotherMyTable1.ANOTHERTABLE_NAME,"date=?",new String[]{date});

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
            travelname.setText("");
            finish();
        }
    };

    //清除鍵的聆聽者
    private View.OnClickListener removeListner = new View.OnClickListener() {

        @Override
        //按下Button事件時會進入這個 function
        public void onClick(View v) {
            Toast.makeText(writein.this,"已清空" , Toast.LENGTH_SHORT).show();
            travelname.setText("");
        }
    };

}
