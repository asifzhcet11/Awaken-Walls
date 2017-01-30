package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity {

    // sensor values
    private String light = "0";
    private String temperature = "0";
    private String humidity = "0";
    private String distance = "0";

    // sensor values object
    private DataFromServer dataFromServer;

    //broadcast reciever for the brodcasted sensor values by the service "MqttDataRetrieveService"
    private SensorValuesReceiver sensorValuesReceiver;

    //View Pager for the sensor values
    private HorizontalInfiniteCycleViewPager horizontalInfiniteCycleViewPager;

    // Adapter for setting the View Pager for the sensor values
    private SensorValuesSliderAdapter adapter;

    //object for the user address information after logging in
    private UserAddress userAddress;

    //object for the user configuration for eg live stream video address etc etc;
    private UserConfiguration userConfiguration;

    // user profile using the user address,user configuration and other user info taken from the database
    private UserProfile userProfile;

    //andriod resources
    private Resources resources;

    //main menu
    private BoomMenuButton menu;

    //array of menu icons and menu names
    private TypedArray menu_icons;
    private TypedArray menu_names;

    // outsideMode to be set true when the user wants to activate the outside mode
    private boolean outsideMode = false;

    //Topic to publish the outside mode status
    private final String OUTSIDE_MODE = "/outside_mode";

    //tag to retrieve the values of the sensors after brodcasted by the service "MqttDataRetrieveService"
    final static String INTENT_SENSORS = "INTENT_SENSORS";

    //MqttDataRetrieveService object
    private MqttDataRetrieveService mqttDataRetrieveService;

    //status for the service is bind to current activity or not
    private Boolean isBind = false;

    //values for the user info form "UserLogin Activity"
    private String image,
            username,
            address,
            city,
            postcode,
            country;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //getting all the values for the user info form "UserLogin Activity"
        image = getIntent().getStringExtra("USER_IMAGE");
        username = getIntent().getStringExtra("USER_NAME");
        address = getIntent().getStringExtra("USER_ADDRESS");
        city = getIntent().getStringExtra("USER_CITY");
        postcode = getIntent().getStringExtra("USER_POSTCODE");
        country = getIntent().getStringExtra("USER_COUNTRY");


        setContentView(R.layout.activity_main);


        //creating the bind service for the mqtt send and recieve values
        Intent intent = new Intent(this, MqttDataRetrieveService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        // starting the service
          //startService(intent);

        // setting the menu
        resources = getResources();
        menu_icons = resources.obtainTypedArray(R.array.menu_icons);
        menu_names = resources.obtainTypedArray(R.array.menu_icons_name);
        menu = (BoomMenuButton) findViewById(R.id.bmb);
        menu.setBackground(this.getDrawable(R.drawable.rect));


        //creating the menu items on click listener on the menu items
        createMenuItems();

    }

    @Override
    protected void onStart() {
        super.onStart();


        //setting the broadcast reciever for the sensor value to be received from the Service
        sensorValuesReceiver = new SensorValuesReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MqttDataRetrieveService.ACTION_SENSORS);
        registerReceiver(sensorValuesReceiver, intentFilter);


        //retrieving the values from the Shared prefrences if user has already activated the outside mode or not
        SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
        outsideMode = sharedPreferences.getBoolean(String.valueOf(R.bool.OUTSIDE_MODE), false);

        if (outsideMode) {
            Drawable menu_icon = menu_icons.getDrawable(2); //image for the activated state
            TextOutsideCircleButton.Builder builder =
                    (TextOutsideCircleButton.Builder) menu.getBuilders().get(2); //text for the activated state
            builder.normalImageDrawable(menu_icon);

        } else {
            Drawable menu_icon = menu_icons.getDrawable(4); //image for the activated state
            TextOutsideCircleButton.Builder builder =
                    (TextOutsideCircleButton.Builder) menu.getBuilders().get(2); //text for the activated state
            builder.normalImageDrawable(menu_icon);

        }


        //setting the user Profile

        userAddress = new UserAddress(address, city, postcode, country);
        userConfiguration = new UserConfiguration("1", "4", "http://www.ustream.tv/channel/ERYysFDNNgK");
        userProfile = new UserProfile(this, username, userAddress, image, userConfiguration);

        userProfile.setProfile();
        userProfile.setUserConfiguration();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBind) {
            unbindService(mConnection);
            isBind = false;
        }
        unregisterReceiver(sensorValuesReceiver);
    }


    public void goToWebView(View view) {
        Intent intent = new Intent(this, StreamVideo.class);
        startActivity(intent);
    }

    public void DetectedPersonGalleryActivity(View view) {
        Intent intent = new Intent(this, DetectedPersonGallery.class);
        startActivity(intent);
    }

    public void SetOutsideMode(boolean status)

    {
        if (status) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("ACTIVATED")
                    .setContentText("Going Outside? \n\n No problem \n We will take care of the house!!")
                    .setConfirmText("OK")
                    .show();
        } else {

            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("DEACTIVATED")
                    .setContentText("Welcome Home !!! \n\n Hope you had a blast outside!!")
                    .setConfirmText("OK")
                    .show();
        }

        String statusTobeSent = Boolean.toString(status);
        mqttDataRetrieveService.publishMessage(OUTSIDE_MODE, statusTobeSent);

    }

    private class SensorValuesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String[] sensorValues = intent.getStringArrayExtra(INTENT_SENSORS);
            temperature = sensorValues[0];
            humidity = sensorValues[1];
            light = sensorValues[2];
            distance = sensorValues[3];

            dataFromServer = new DataFromServer(temperature, humidity, light, distance);
            MainActivity.this.setAdapter(dataFromServer);
        }
    }

    public void setAdapter(DataFromServer dataFromServer)

    {

        horizontalInfiniteCycleViewPager = (HorizontalInfiniteCycleViewPager) findViewById(R.id.hicvp);
        adapter = new SensorValuesSliderAdapter(MainActivity.this, dataFromServer);
        horizontalInfiniteCycleViewPager.setAdapter(adapter);

    }

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


    public void createMenuItems() {
        for (int i = 0; i < menu.getPiecePlaceEnum().pieceNumber(); i++) {
            Drawable menu_icon = menu_icons.getDrawable(i);
            String menu_text = menu_names.getText(i).toString();
            TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
                    .normalImageDrawable(menu_icon)
                    .normalText(menu_text)
                    .imagePadding(new Rect(15, 15, 15, 15))
                    .textSize(15)
                    .rippleEffect(true)
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            SharedPreferences sharedPreferences = MainActivity.this.getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            switch (index) {
                                case 0:
                                    Intent streamVideoIntent = new Intent(MainActivity.this, DetectedPersonGallery.class);
                                    startActivity(streamVideoIntent);
                                    break;
                                case 1:
                                    Intent detectedPersonGalleryIntent = new Intent(MainActivity.this, StreamVideo.class);
                                    startActivity(detectedPersonGalleryIntent);
                                    break;
                                case 2:

                                    if (outsideMode) {
                                        Drawable menu_icon = menu_icons.getDrawable(4);
                                        TextOutsideCircleButton.Builder builder =
                                                (TextOutsideCircleButton.Builder) menu.getBuilders().get(index);
                                        builder.normalImageDrawable(menu_icon);
                                        outsideMode = false;
                                        editor.putBoolean(String.valueOf(R.bool.OUTSIDE_MODE), outsideMode);
                                        editor.commit();
                                    } else {
                                        Drawable menu_icon = menu_icons.getDrawable(2);
                                        TextOutsideCircleButton.Builder builder =
                                                (TextOutsideCircleButton.Builder) menu.getBuilders().get(index);
                                        builder.normalImageDrawable(menu_icon);
                                        outsideMode = true;
                                        editor.putBoolean(String.valueOf(R.bool.OUTSIDE_MODE), outsideMode);
                                        editor.commit();

                                    }

                                    SetOutsideMode(!outsideMode);
                                    break;

                                case 3:

                                    Intent intent = new Intent(MainActivity.this, UserLogin.class);
                                    editor.clear();
                                    editor.commit();
                                    startActivity(intent);
                                    break;

                                default:
                                    Toast.makeText(MainActivity.this, "Selected None", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
            menu.addBuilder(builder);
        }

    }
}