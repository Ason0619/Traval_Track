package com.project.traveltrack.ui.home;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.project.traveltrack.ImageAdapter;
import com.project.traveltrack.R;

public class imageall extends AppCompatActivity {

    private GridView gridView;
    private Bundle bundle;
    private ArrayList arrayListIMG = new ArrayList();
    private ImageView imageView;
    private List<String> thumbs;  //存放縮圖的id
    private List<String> imagePaths;  //存放圖片的路徑
    private ImageAdapter imageAdapter;  //用來顯示縮圖
    private int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view;
        setContentView(R.layout.activity_imageall);
        gridView = (GridView) findViewById(R.id.gridView1);
        imageView = (ImageView) findViewById(R.id.imageView1);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//設定螢幕不隨手機選轉
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//設定手機值向顯示
        build();
       // ContentResolver cr = getContentResolver();
       // String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};

        //查詢SD卡的圖片
        //Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        //        projection, null, null, null);

        thumbs = new ArrayList<String>();
        imagePaths = new ArrayList<String>();

        imagePaths=arrayListIMG;
        for (int i = 0; i < imagePaths.size(); i++) {

           /* cursor.moveToPosition(i);
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Images.Media._ID));// ID
            thumbs.add(id + "");

            String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));//抓路徑

            imagePaths.add(filepath);*/

            thumbs.add(i + "");
        }
        //cursor.close();
        imageAdapter = new ImageAdapter(imageall.this, thumbs,imagePaths);
        gridView.setAdapter(imageAdapter);
        imageAdapter.notifyDataSetChanged();


        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                imageView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
            }

        });
        imageView.setVisibility(View.GONE);

    }


    public void build()
    {
        bundle = this.getIntent().getExtras();
        position = 0;
        arrayListIMG = bundle.getStringArrayList("ImgPositionArrList");
        this.getSupportActionBar().setTitle("相片總數:" + arrayListIMG.size() + "張");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        return true;
    }
    public void setImageView(int position){
        Bitmap bm = BitmapFactory.decodeFile(imagePaths.get(position));
        int degree=readPictureDegree(imagePaths.get(position));
        bm =rotateBitmap(bm,degree);
        imageView.setImageBitmap(bm);
        imageView.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.GONE);
    }
    //判斷角度
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;

    }
    //旋轉照片
    public static Bitmap rotateBitmap(Bitmap bitmap,int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }
}
