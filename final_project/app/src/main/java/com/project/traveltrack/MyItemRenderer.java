package com.project.traveltrack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

public class MyItemRenderer extends DefaultClusterRenderer<MyItem> {
    //宣告一個整數變數來設定標記的大小
    private static final int MARKER_DIMENSION = 80;
    //建立一個ImageView放單張照片用於在呈現群集項目之前
    private final ImageView markerImageView;
    //建立一個ImageView放多張照片用於呈現群集項目
    private final ImageView cluseterView;
    //建立一個圖標生成器來把圖片(bitmap)轉成圖片標示(用於還沒呈現聚集的時候，也有是只有一張照片)
    private final IconGenerator iconGenerator;
    //建立一個圖標生成器來把多張圖片(bitmap)轉成圖片標示(用於呈現聚集的時候，也有是多張照片)
    private final IconGenerator mClusterIconGenerator;
    //建立一個Context物件來儲存MapsActivity建立物件時所傳過來的this
    //MyItemRenderer myItemRenderer = new MyItemRenderer(this,mMap,mClusterManager);
    //Context功能:
    //啟動Activity (startActivity) 啟動Service (startService)
    //彈出Toast 發送廣播(sendBroadcast), 註冊廣播接收者(registerReceiver)
    //獲取ContentResolver (getContentResolver) 獲取類加載器(getClassLoader)
    //打開或創建 資料庫(openOrCreateDatabase)
    //獲取資源(getResources)
    //也可用於設定生命週期Activity，Activity摧毀它才摧毀
    private final Context mContext;

    //建構子，當物件建立時會自動執行
    public MyItemRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
        //Context設為跟建立物件時所傳過來的this
        mContext = context;
        iconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);
        markerImageView = new ImageView(context);
        cluseterView = new ImageView(context);
        //設定ImageView的參數，而參數建立一個新的ViewGroup
        // ViewGroup是個特殊的View
        // 它繼承於Android.view.View。
        // 它的功能就是裝載和管理下一層的View對象或ViewGroup對象
        // 也就說他是一個容納其它元素的的容器。ViewGroup是佈局管理器（layout）及view容器的基類。
        // ViewGroup中，還定義了一個嵌套類ViewGroup.LayoutParams。
        // 這個類定義了一個顯示對象的位置、大小等屬性，view通過LayoutParams中的這些屬性值來告訴父級，它們將如何放置。
        // 而ViewGroup的長寬參數就設為靜態變數MARKER_DIMENSION
        markerImageView.setLayoutParams(new ViewGroup.LayoutParams(MARKER_DIMENSION, MARKER_DIMENSION));
        cluseterView.setLayoutParams(new ViewGroup.LayoutParams(MARKER_DIMENSION, MARKER_DIMENSION));
        //設置圖片標示的內容為ImageView
        mClusterIconGenerator.setContentView(cluseterView);
        iconGenerator.setContentView(markerImageView);
    }
    //在呈現群集項目之前呼叫此函式
    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
        //使用createScaledBitmap把item存放的bitmap物件依照一定的比例創建一個新的位圖
        Bitmap clusterMarker = Bitmap.createScaledBitmap(item.mbitmap, 200, 200, false);
        //把圖片放進ImageView
        markerImageView.setImageBitmap(clusterMarker);
        //把圖標整個轉換成一個Bitmap物件
        Bitmap icon = iconGenerator.makeIcon();
        //把Marker的標示改為圖片標示
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }
    /*這裡我不知道怎麼解釋只是這裡是群集時會執行的，如果你想改它群集時的圖片就改這裡*/
    @Override
    protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        //
        List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(1, cluster.getSize()));
        for (MyItem p : cluster.getItems()) {
            // Draw 4 at most.
            if (profilePhotos.size() == 1) break;
            Bitmap bitmapCluster = Bitmap.createScaledBitmap(p.mbitmap, 200, 200, false);
            Drawable drawable = new BitmapDrawable(mContext.getResources(), bitmapCluster);
            drawable.setBounds(0, 0, 80, 80);
            profilePhotos.add(drawable);
        }

        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
        multiDrawable.setBounds(0, 0, 0, 0);

        cluseterView.setImageDrawable(multiDrawable);
        Bitmap icon = mClusterIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }
}
