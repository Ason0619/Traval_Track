package com.project.traveltrack;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.maps.android.data.geojson.BiMultiMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class singal extends AppCompatActivity {

    private ImageView imageView;
    private ArrayList arrayListIMG = new ArrayList();
    private ArrayList arrayListIMGfromat = new ArrayList();
    private Bundle bundle;
    private Bitmap bitmap;
    private int position;
    private float x1, x2;
    private String IMGLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//設定螢幕不隨手機選轉
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//設定手機值向顯示
        build();
    }

    public void build() {
        imageView = (ImageView) findViewById(R.id.imageView);
        //textView=(TextView) findViewById(R.id.textView2);
        setImageView();
    }

    private void setImageView() {
        bundle = this.getIntent().getExtras();
        position = 0;
        arrayListIMG = bundle.getStringArrayList("ImgPositionArrList");
        //IMGLocation=bitmapToString(arrayListIMG.get(position) + "");
        bitmap = BitmapFactory.decodeFile(arrayListIMG.get(0)+"");
        this.getSupportActionBar().setTitle("相片總數:" + arrayListIMG.size() + "張");
        bitmap=rotateBitmap(bitmap,readPictureDegree(arrayListIMG.get(0)+""));
        //textView.setText("相片總數:"+arrayListIMG.size()+"張");
        imageView.setImageBitmap(bitmap);
        Toast.makeText(singal.this, "相片第" + (position + 1) + "張", Toast.LENGTH_SHORT).show();
        //showing(bitmap);
    }

    public boolean onTouchEvent(MotionEvent event) {
        //繼承了Activity的onTouchEvent方法，直接監聽點擊事件
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //當手指按下的時候
            x1 = event.getX();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //當手指離開時
            x2 = event.getX();
            if (x1 - x2 > 50) {
                if (position >= arrayListIMG.size() - 1) {
                    position = 0;
                } else {
                    position += 1;
                }
                Toast.makeText(singal.this, "相片第" + (position + 1) + "張", Toast.LENGTH_SHORT).show();
                bitmap = BitmapFactory.decodeFile(arrayListIMG.get(position) + "");
                bitmap=rotateBitmap(bitmap,readPictureDegree(arrayListIMG.get(position) + ""));

                imageView.setImageBitmap(bitmap);
                //showing(bitmap);
                //right();
                // imageView.setImageResource(R.drawable.ok);
            } else if (x2 - x1 > 50) {

                if (position <= 0) {
                    position = arrayListIMG.size() - 1;
                } else {
                    position -= 1;
                }
                Toast.makeText(singal.this, "相片第" + (position + 1) + "張", Toast.LENGTH_SHORT).show();
                bitmap = BitmapFactory.decodeFile(arrayListIMG.get(position) + "");
               bitmap=rotateBitmap(bitmap,readPictureDegree(arrayListIMG.get(position) + ""));
                imageView.setImageBitmap(bitmap);
                //showing(bitmap);
                //right();
                //imageView.setImageResource(R.drawable.pig);
            } else {
            }
        }
        return super.onTouchEvent(event);
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

    //計算圖片的縮放值
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    // 根據路徑獲得圖片並壓縮，返回bitmap用於顯示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    //把bitmap轉換成String
    public static String bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // quality
        bm.compress(Bitmap.CompressFormat.JPEG, 1, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
