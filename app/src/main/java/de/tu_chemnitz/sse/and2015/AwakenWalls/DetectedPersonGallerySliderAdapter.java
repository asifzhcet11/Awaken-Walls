package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import java.util.ArrayList;

/**
 * Created by mohammadasif on 25/01/2017.
 * This is custom adapter to set the contents of the cyclic pager
 */

public class DetectedPersonGallerySliderAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<Bitmap> detectedImages;

    public DetectedPersonGallerySliderAdapter(Context context, ArrayList<Bitmap> detectedImages) {
        this.context = context;
        this.detectedImages = detectedImages;
    }

    @Override
    public int getCount() {
        return this.detectedImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View view;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.image_card, container, false);
        CardView card = (CardView)view.findViewById(R.id.imageCard);
        card.setCardBackgroundColor(view.getResources().getColor(R.color.imageBackground));
        ImageView imageView = (ImageView)view.findViewById(R.id.image);
        imageView.setImageBitmap(this.detectedImages.get(position));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }

}
