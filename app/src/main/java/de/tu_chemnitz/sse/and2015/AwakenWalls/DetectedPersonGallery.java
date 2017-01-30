package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import java.util.ArrayList;


/**
 * Created by mohammadasif on 19/01/2017.
 * Gallery to show the 5 images of the user detected by the device
 */

public class DetectedPersonGallery extends Activity {

    //cyclic pager
    private HorizontalInfiniteCycleViewPager detectedPersonGallery;

    //adapter for the cyclic pager
    private DetectedPersonGallerySliderAdapter adapter;


    //topic for the image request with an UUID
    final private String IMAGE_REQUEST_TOPIC = "/getImagesRequest";

    //this UUID is genetated when the raspberry detected any person and this is used to reference back
    //to get the images
    private String imagesUUID;

    //mqtt service
    private MqttDataRetrieveService mqttDataRetrieveService;

    private Boolean isBind = false;

    //text view to see how many packets has been reieved
    private TextView statusUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detected_person_gallery);

        //creating a bind service
        Intent intent = new Intent(this, MqttDataRetrieveService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        detectedPersonGallery = (HorizontalInfiniteCycleViewPager) findViewById(R.id.detectedPersonGallery);
        statusUpdate = (TextView) findViewById(R.id.statusUpdate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBind) {
            unbindService(mConnection);
            isBind = false;
        }

    }

    public void getImages(View view) {

        //once the UUID of the image is recieved by the Andriod app it store in Shared prefences and refrenced it back to
        //retrieve the images
        SharedPreferences sharedPreferences = DetectedPersonGallery.this.getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
        imagesUUID = sharedPreferences.getString(getString(R.string.IMAGES_UUID), "");
        mqttDataRetrieveService.publishMessage(IMAGE_REQUEST_TOPIC, imagesUUID);

        //setting the fetching dialog box
        mqttDataRetrieveService.setFetchDialogBox(this, statusUpdate);

    }


    public void showImages(View view) {

        //array of the images is retured by using UUID as the reference and then set the images
        //as a cyclic pager content
        DetectedImage detectedImage = mqttDataRetrieveService.getImages();
        setAdapterForSlider(detectedImage.returnImages());

    }

    public void setAdapterForSlider(ArrayList<Bitmap> detectedImages)

    {
        adapter = new DetectedPersonGallerySliderAdapter(this, detectedImages);
        detectedPersonGallery.setAdapter(adapter);
    }


    // service connection to access the service methods "MqttDataRetrieveService"

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MqttDataRetrieveService.LocalBinderService localBinderService = (MqttDataRetrieveService.LocalBinderService) iBinder;
            mqttDataRetrieveService = localBinderService.getService();
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBind = false;

        }
    };


}

