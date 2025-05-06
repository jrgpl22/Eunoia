package com.example.moodtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ImageAdapter extends PagerAdapter {

    private final LayoutInflater inflater;
    private final List<Integer> imageList;

    public ImageAdapter(Context context, List<Integer> imageList) {
        this.inflater = LayoutInflater.from(context);
        this.imageList = imageList;
    }


    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // Inflate the generic image layout
        View view = inflater.inflate(R.layout.image_item, container, false);

        // Get the ImageView and set the correct image
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageResource(imageList.get(position));

        // Add the view to ViewPager
        container.addView(view);
        return view;
    }



    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
