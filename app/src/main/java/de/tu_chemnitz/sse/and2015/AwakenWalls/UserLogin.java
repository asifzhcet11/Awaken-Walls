package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mohammadasif on 17/01/2017.
 * USER LOGIN and AUTHENTICATION
 */

public class UserLogin extends Activity {

    final private String USER_AUTHORIZATION_TOPIC = "/user_login";

    private EditText user_email;
    private EditText user_password;
    private CheckBox remember_me;
    private String rememberMe;
    private String userEmail;
    private String userPassword;
    private SharedPreferences sharedPreferences;
    private MqttDataRetrieveService mqttDataRetrieveService;
    private Boolean isBind = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        user_email = (EditText) findViewById(R.id.user_email);
        user_password = (EditText) findViewById(R.id.user_password);
        remember_me = (CheckBox) findViewById(R.id.remember_me);

        //checking if the remember me was checked or not
        sharedPreferences = UserLogin.this.getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
        rememberMe = sharedPreferences.getString(getString(R.string.REMEMBER_ME), "Unchecked");

        //creating the bind service for the mqtt send and recieve values
        Intent intent = new Intent(this, MqttDataRetrieveService.class);


        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onStart() {
        super.onStart();
        //creating the bind service for the mqtt send and recieve values


    }


    @Override
    protected void onStop() {
        super.onStop();
        if(isBind)
        {
            unbindService(mConnection);
            isBind = false;
        }

    };


    public void Authorize(View view)

    {

            //setting the authntication progress bar
            mqttDataRetrieveService.setAuthenticationDialog(this);

            //getting the user email and password
            userEmail = user_email.getText().toString();
            userPassword = user_password.getText().toString();

            // creating the JSON object of the user email and password to be sent
           JSONObject UserDetails = new JSONObject();
            try {
                UserDetails.put("username", userEmail);
                UserDetails.put("password", userPassword);
            }
            catch (JSONException e) {
                            e.printStackTrace();
                    }

            //publishing the message to broker for authentication
            mqttDataRetrieveService.publishMessage(USER_AUTHORIZATION_TOPIC,UserDetails.toString());


    }



    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            MqttDataRetrieveService.LocalBinderService localBinderService = (MqttDataRetrieveService.LocalBinderService) iBinder;
            mqttDataRetrieveService = localBinderService.getService();
            mqttDataRetrieveService.setActivityContext(UserLogin.this);
            mqttDataRetrieveService.setRemember_me(remember_me);

            //setting the intent if the user has previously selected the remember me
            mqttDataRetrieveService.setIntent(rememberMe);

            isBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBind = false;
        }
    };



}
