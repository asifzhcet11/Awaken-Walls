package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;

/**
 * Created by mohammadasif on 31/01/2017.
 */

public class Initializer extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);

        Intent intentService = new Intent(this,MqttDataRetrieveService.class);
        startService(intentService);

        new CountDownTimer(6000, 6000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

               Intent userLoginIntent = new Intent(Initializer.this, UserLogin.class);
                startActivity(userLoginIntent);
            }
        }.start();




    }
}
