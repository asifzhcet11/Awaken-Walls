package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;



/**
 * Created by mohammadasif on 16/01/2017.
 * User at anytime can stream and view his/her room or area
 */

public class StreamVideo extends Activity {


    private MqttDataRetrieveService mqttDataRetrieveService;
    private boolean isBind = false;

    //topic to tell if the user wants to stream or not
    private static final String VIDEO_TOPIC = "/video";

    //this image is to be clicked after going online to watch the live feed
    private ImageView launchImage;

    //info text for the user
    private TextView infoText;

    //info dialog box
    private SweetAlertDialog infoDialog;

    //check if user already went online or not
    private boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_video);

        launchImage = (ImageView)findViewById(R.id.launch);
        infoText = (TextView)findViewById(R.id.infoText);


        //bind service created
        Intent intent = new Intent(this, MqttDataRetrieveService.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }




    @Override
    protected void onStop() {
        super.onStop();
        if(isBind)
        {
            unbindService(mConnection);
            isBind = false;
        }

    }

    public void goOnline(View view)
    {
           //publishing ON to go online
           mqttDataRetrieveService.publishMessage(VIDEO_TOPIC, "ON");

           //updating the launch image
           launchImage.setImageResource(R.drawable.launch);

            //updating the info text
           infoText.setText("Click to view now or you can go offline.");

            //updating the online status
           isOnline = true;


    }

    public void goOffline(View view)
    {
            //publishing OFF to go offline
            mqttDataRetrieveService.publishMessage(VIDEO_TOPIC, "OFF");

            //updating the launch image
            launchImage.setImageResource(R.drawable.cantlaunch);

            //updating the info text
            infoText.setText("You are offline \n Please go online to watch");

            //updating the online status
            isOnline = false;

    }

    //launch the stream after the user goes online
    public void launch(View view)
    {
        if (isOnline)
                {
                    infoDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
                    infoDialog.getProgressHelper().setBarColor(Color.parseColor("#383838"));
                    infoDialog.setTitleText("Fetching From Server");
                    infoDialog.setContentText("\n Please Wait \n");
                    infoDialog.setCancelable(false);
                    infoDialog.show();

                    //usually this is the rough amount of time raspberry pi takes to publish the video data
                    new CountDownTimer(30000, 30000) {

                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {

                            infoDialog.cancel();
                            String url = "http://www.ustream.tv/channel/ERYysFDNNgK";
                            Intent launchIntent= new Intent(Intent.ACTION_VIEW);
                            launchIntent.setData(Uri.parse(url));
                            startActivity(launchIntent);
                        }
                    }.start();


                }

        else    {
                        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("ERROR")
                                .setContentText("Please go online before viewing.")
                                .setConfirmText("OK")
                                .show();
                }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MqttDataRetrieveService.LocalBinderService localBinderService = (MqttDataRetrieveService.LocalBinderService)iBinder;
            mqttDataRetrieveService = localBinderService.getService();
            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBind = false;
        }
    };


    public void goToMainActivity(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


}
