package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



/**
 * Created by mohammadasif on 08/01/2017.
 *
 * Custom Pager adapter to show the values of the sensors.
 */

public class SensorValuesSliderAdapter extends PagerAdapter {

    private String[] titles = {"TEMPERATURE","HUMIDITY","LIGHT","DISTANCE"};
    private Context context;
    private LayoutInflater layoutInflater;
    DataFromServer data;


    public SensorValuesSliderAdapter(Context context, DataFromServer data) {
        this.context = context;
        this.data = data;
        }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final View view;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.sensor_dispaly_card, container, false);
        TextView title = (TextView)view.findViewById(R.id.txt_item);
        CardView card = (CardView)view.findViewById(R.id.card);
        title.setText(titles[position]);

        //setting the max value so that we can get the percentage.
        float dataValue = 0;
        float maxValue = 100;

        //setting values depending on the type
        switch (position) {
            case 0:
                dataValue = data.getTemperature();
                card.setCardBackgroundColor(view.getResources().getColor(R.color.temperature));
                maxValue = 100;
                break;
            case 1:
                dataValue = data.getHumidity();
                card.setCardBackgroundColor(view.getResources().getColor(R.color.humidity));
                maxValue = 200;
                break;
            case 2:
                dataValue = data.getLight();
                card.setCardBackgroundColor(view.getResources().getColor(R.color.light));
                maxValue = 3500;
                break;
            case 3:
                dataValue = data.getDistance();
                card.setCardBackgroundColor(view.getResources().getColor(R.color.distance));
                maxValue = 400;
                break;
            default:
                card.setCardBackgroundColor(view.getResources().getColor(R.color.distance));
                break;
        }

        //setting the circular bar for the values to be shown
        CircleDisplay mCircleDisplay = (CircleDisplay)view.findViewById(R.id.display);
        mCircleDisplay.setAnimDuration(4000);
        mCircleDisplay.setValueWidthPercent(20f);
        mCircleDisplay.setFormatDigits(1);
        mCircleDisplay.setDimAlpha(80);
        mCircleDisplay.setColor(view.getResources().getColor(R.color.colorAccent));
        mCircleDisplay.setUnit("");
        mCircleDisplay.setStepSize(0.5f);
        mCircleDisplay.showValue(dataValue, maxValue, true);


        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((View) object);
    }

}
