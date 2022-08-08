package com.project.traveltrack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.project.traveltrack.ui.home.imageall;

public class ImageAdapter extends BaseAdapter {

    private ViewGroup layout;
    private Context context;
    private List coll;
    private List path;
    private Bitmap bm;

    public ImageAdapter(Context context, List coll,List path) {

        super();
        this.context = context;
        this.coll = coll;
        this.path=path;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowview = inflater.inflate(R.layout.item_photo, parent, false);
        layout = (ViewGroup) rowview.findViewById(R.id.rl_item_photo);
        ImageView imageView = (ImageView) rowview.findViewById(R.id.imageView1);
        int degree=readPictureDegree((String) (path.get(position)));
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float dd = dm.density;
        float px = 25 * dd;
        float screenWidth = dm.widthPixels;
        int newWidth = (int) (screenWidth - px) / 4; // 一行顯示四個縮圖
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inJustDecodeBounds = true;
            option.inPurgeable = true;
            layout.setLayoutParams(new GridView.LayoutParams(newWidth, newWidth));
            imageView.setId(position);

            bm = BitmapFactory.decodeFile((String) (path.get(position)), option);

            int yRatio = (int) Math.ceil(option.outHeight / 200);
            int xRatio = (int) Math.ceil(option.outWidth / 200);
            if (yRatio > 1 || xRatio > 1) {
                if (yRatio > xRatio) {
                    option.inSampleSize = yRatio;
                } else {
                    option.inSampleSize = xRatio;
                }
            }
            option.inJustDecodeBounds = false;
           bm = BitmapFactory.decodeFile((String) (path.get(position)), option);


        Bitmap newBit = Bitmap.createScaledBitmap(bm, newWidth, newWidth,
        true);

     /*  Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(context
                        .getApplicationContext().getContentResolver(), Long
                        .parseLong((String) coll.get(position)),
                MediaStore.Images.Thumbnails.MICRO_KIND, null);*/
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newBit.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        newBit=rotateBitmap(newBit,degree);
        imageView.setImageBitmap(newBit);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        //點擊照片
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "photo:" + position, Toast.LENGTH_SHORT)
                        .show();

                ((imageall)context).setImageView(position);
            }

        });

        return rowview;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return coll.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return coll.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
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