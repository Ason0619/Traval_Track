package com.project.traveltrack.ui.notifications;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.project.traveltrack.MainActivity;
import com.project.traveltrack.R;

import java.util.ArrayList;

import static com.project.traveltrack.ui.notifications.NotificationsFragment.pageview;
import static com.project.traveltrack.ui.notifications.NotificationsFragment.tips;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private ViewPager viewPager;

    //三個view
    private View view1;
    //private View view2;
    private View view3,view4,view5,view6,view7,view8,view9,view10,view11,view12,view13,view14,view15;

    //用來存放view並傳遞給viewPager的介面卡。
    public static ArrayList<View> pageview;


    //用來存放圓點，沒有寫第四步的話，就不要定義一下三個變量了。
    public static ImageView[] tips = new ImageView[3];

    private ImageView imageView;

    //圓點組的物件
    private ViewGroup group;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        viewPager = (ViewPager)root.findViewById(R.id.viewPager);
        view1 = getLayoutInflater().inflate(R.layout.view1,null);
       // view2 = getLayoutInflater().inflate(R.layout.view2,null);
        view3 = getLayoutInflater().inflate(R.layout.view3,null);
        view4 = getLayoutInflater().inflate(R.layout.view4,null);
        view5 = getLayoutInflater().inflate(R.layout.view5,null);
        view6 = getLayoutInflater().inflate(R.layout.view6,null);
        view7 = getLayoutInflater().inflate(R.layout.view7,null);
        view8 = getLayoutInflater().inflate(R.layout.view8,null);
        view9 = getLayoutInflater().inflate(R.layout.view9,null);
        view10 = getLayoutInflater().inflate(R.layout.view10,null);
        view11 = getLayoutInflater().inflate(R.layout.view11,null);
        view12= getLayoutInflater().inflate(R.layout.view12,null);
        view13 = getLayoutInflater().inflate(R.layout.view13,null);
        view14 = getLayoutInflater().inflate(R.layout.view14,null);
        view15 = getLayoutInflater().inflate(R.layout.view15,null);
        pageview = new ArrayList<View>();
        pageview.add(view1);
       // pageview.add(view2);
        pageview.add(view3);
        pageview.add(view4);
        pageview.add(view5);
        pageview.add(view6);
        pageview.add(view7);
        pageview.add(view8);
        pageview.add(view9);
        pageview.add(view10);
        pageview.add(view11);
        pageview.add(view12);
        pageview.add(view13);
        pageview.add(view15);
        pageview.add(view14);



        //viewPager下面的圓點，ViewGroup
        group = (ViewGroup)root.findViewById(R.id.viewGroup);
        tips = new ImageView[pageview.size()];
        for(int i =0;i<pageview.size();i++){
            imageView = new ImageView(root.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(20,20));
            imageView.setPadding(20, 0, 20, 0);
            tips[i] = imageView;

            //預設第一張圖顯示為選中狀態
            if (i == 0) {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_focused);
            } else {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
            }

            group.addView(tips[i]);
        }
        //這裡的mypagerAdapter是第三步定義好的。
        viewPager.setAdapter(new mypagerAdapter(pageview));
        //這裡的GuiPageChangeListener是第四步定義好的。
        viewPager.addOnPageChangeListener(new GuidePageChangeListener());

        return root;
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

}

class mypagerAdapter extends PagerAdapter {
    private ArrayList<View> pageview1;
    public mypagerAdapter(ArrayList<View> pageview1){
        this.pageview1 = pageview1;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.d("MainActivityDestroy",position+"");
        if (pageview1.get(position)!=null) {
            container.removeView(pageview1.get(position));
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(pageview1.get(position));
        Log.d("MainActivityInstanti",position+"");
        return pageview1.get(position);
    }

    @Override
    public int getCount() {
        return pageview1.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object==view;
    }
}


class GuidePageChangeListener implements ViewPager.OnPageChangeListener{
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    //切換view時，下方圓點的變化。
    public void onPageSelected(int position) {
        tips[position].setBackgroundResource(R.mipmap.page_indicator_focused);
        //這個圖片就是選中的view的圓點
        for(int i=0;i<pageview.size();i++){
            if (position != i) {
                tips[i].setBackgroundResource(R.mipmap.page_indicator_unfocused);
                //這個圖片是未選中view的圓點
            }
        }
    }
}