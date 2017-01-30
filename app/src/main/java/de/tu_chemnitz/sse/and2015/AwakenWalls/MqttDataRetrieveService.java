package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import cn.pedant.SweetAlert.SweetAlertDialog;
import org.json.JSONException;


/**
 * Created by mohammadasif on 28/01/2017.
 *
 * Backend service responsible for all the data transfers
 */

public class MqttDataRetrieveService extends Service {

    //mqttConnection object
    private MqttConnection DataRetrieveServiceConnection;

    //mqtt client
    public MqttAndroidClient DataRetrieveServiceClient;

    //topics to be subscribed
    private static final String topics[] = {"/user_login_info", "/light", "/temperature", "/humidity", "/distance", "/personDetected", "/imagesUUID", "/images"};

    //qos of the subscribed topics
    private static final int qos[] = {1, 1, 1, 1, 1, 1, 1, 1};

    //initial value for the different sensors
    private String lightValue = "0";
    private String temperatureValue = "0";
    private String humidityValue = "0";
    private String distanceValue = "0";

    //UUID for the images to be retrieved once detected
    private String imagesUUID;

    //notification when the user is outside and raspberry pi has detected the person
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;


    private final IBinder mbinder = new LocalBinderService();

    //String builder to store the images data
    private StringBuilder firstImageBundleString = new StringBuilder("");
    private StringBuilder secondImageBundleString = new StringBuilder("");
    private StringBuilder thirdImageBundleString = new StringBuilder("");
    private StringBuilder fourthImageBundleString = new StringBuilder("");

    //images String
    private ArrayList<String> images = new ArrayList<String>();

    //FILTER for the broadcast reciever
    final static String ACTION_SENSORS = "ACTION_SENSORS";
    final static String INTENT_SENSORS = "INTENT_SENSORS";

    //dialog box when app is fetching the images
    private SweetAlertDialog fetchingImageDialog;

    //detected images
    private DetectedImage detectedImage;

    //text view for the updates of number of packets recieved of the images
    private TextView statusUpdate;

    //dialog box when app is authenticating the user
    private SweetAlertDialog authenticationDialog;

    //actiivity context
    private Context activityContext;

    //remember me status of the user login
    private CheckBox remember_me;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }


    public class LocalBinderService extends Binder {

        MqttDataRetrieveService getService() {

            return MqttDataRetrieveService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "Started...", Toast.LENGTH_LONG).show();

        //noticfication builder for generating the notification
        notificationBuilder = new NotificationCompat.Builder(MqttDataRetrieveService.this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //connecting the mqtt client
        DataRetrieveServiceConnection = new MqttConnection(this.getApplicationContext(), topics, qos);
        DataRetrieveServiceClient = DataRetrieveServiceConnection.init();
        DataRetrieveServiceClient.setCallback(new MqttCallback() {

            //data recieved on the respective topics
            @Override
            public void messageArrived(String topic, MqttMessage msg) throws Exception {

                String messageBody = new String(msg.getPayload());

                switch (topic) {
                    case "/light":
                        lightValue = messageBody;
                        break;
                    case "/temperature":
                        temperatureValue = messageBody;
                        break;
                    case "/humidity":
                        humidityValue = messageBody;
                        break;
                    case "/distance":
                        distanceValue = messageBody;
                        break;
                    case "/personDetected":
                        Toast.makeText(getApplicationContext(), messageBody, Toast.LENGTH_SHORT).show();
                        Boolean isPersonDetected = Boolean.parseBoolean(messageBody);

                        //if the person is detected then show the notification
                        if (isPersonDetected) {
                            OutsideModeNotification outsideModeNotification = new OutsideModeNotification(notificationBuilder, notificationManager, MqttDataRetrieveService.this);
                            outsideModeNotification.CreateNotification();
                        }
                        break;

                    case "/imagesUUID":
                        imagesUUID = messageBody;
                        // saving the UUIDs of the images so that when the user request it can return the images
                        SharedPreferences sharedPreferences = MqttDataRetrieveService.this.getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.IMAGES_UUID), imagesUUID);
                        editor.commit();
                        break;

                    case "/images":

                        // getting the images as encoded Base 64 strings and being decocded
                        if (messageBody.equals(null)) {
                            Toast.makeText(getApplicationContext(), "NO data recieved", Toast.LENGTH_LONG).show();
                        }
                        JSONObject imageData = new JSONObject(messageBody);
                        String imageDataString = imageData.getString("data");
                        String numberOfPackets = imageData.getString("numberOfPackets");
                        String position = imageData.getString("position");
                        int imageNumber = Integer.parseInt(imageData.getString("imageNumber"));
                        getImagesData(imageNumber, imageDataString, numberOfPackets, position);
                        break;

                    case "/user_login_info":
                        //after authenticating setting the profile for the user
                        authenticationDialog.cancel();
                        String userInfo = messageBody;
                        PostAuthenticationResult(userInfo);
                        break;
                    default:
                        break;
                }

                String[] SENSOR_VALUES = {temperatureValue, humidityValue, lightValue, distanceValue};
                //Broadcasting the sensor values
                BrodcastData(ACTION_SENSORS, INTENT_SENSORS, SENSOR_VALUES);


            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // TODO Auto-generated method stub

            }

            @Override
            public void connectionLost(Throwable exception) {
                // TODO Auto-generated method stub

            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Stoped...", Toast.LENGTH_LONG).show();
        super.onDestroy();
        DataRetrieveServiceClient.unregisterResources();
        DataRetrieveServiceClient.close();
    }

    //Broadcasting the intent data by setting the action Filter and intent filter
    public void BrodcastData(String actionFilter, String intentFilter, String[] intentData) {
        Intent intent = new Intent();
        intent.setAction(actionFilter);
        intent.putExtra(intentFilter, intentData);
        sendBroadcast(intent);
    }

    //publish messaging by using the mqtt andriod client
    public void publishMessage(String topic, String message) {
        byte[] encodedPayload = new byte[0];

        try {
            encodedPayload = message.getBytes("UTF-8");
            MqttMessage mqttMessage = new MqttMessage(encodedPayload);
            DataRetrieveServiceClient.publish(topic, mqttMessage);

        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    //gettoing the encoded images and decoding is done and then being shown to the user
    public void getImagesData(int imageNumber, String imageDataString, String numberOfPackets, String position) {

        switch (imageNumber) {
            case 0:
                firstImageBundleString.append(imageDataString);
                break;
            case 1:
                secondImageBundleString.append(imageDataString);
                break;
            case 2:
                thirdImageBundleString.append(imageDataString);
                break;
            case 3:
                fourthImageBundleString.append(imageDataString);
                break;
            default:
                break;
        }

        //checks if all the images has been recieved
        if ((numberOfPackets).equals(position) && ((String.valueOf(imageNumber)).equals("3"))) {
            images.add(0, firstImageBundleString.toString());
            images.add(1, secondImageBundleString.toString());
            images.add(2, thirdImageBundleString.toString());
            images.add(3, fourthImageBundleString.toString());
            detectedImage = new DetectedImage(images);
            cancelFetchDialogBox();
            Toast.makeText(getApplicationContext(), "Click Show to View", Toast.LENGTH_LONG).show();


        } else {
            // showing the packets recieved of the images
            this.statusUpdate.setText("Image # : " + String.valueOf(imageNumber + 1) + "      " + " Fetched Packet :  " + position + "/" + numberOfPackets + "");
        }

    }


    public DetectedImage getImages() {

        //returning the array of images object "DetectedImage"
        return detectedImage;
    }


    //setting the dialog box for fetching the images
    public void setFetchDialogBox(Context context, TextView statusUpdate) {
        fetchingImageDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        fetchingImageDialog.getProgressHelper().setBarColor(Color.parseColor("#383838"));
        fetchingImageDialog.setTitleText("Fetching From Server");
        fetchingImageDialog.setContentText("\n Please Wait \n");
        fetchingImageDialog.setCancelable(false);
        fetchingImageDialog.show();
        this.statusUpdate = statusUpdate;
    }

    //canceling the fetching dialog box
    public void cancelFetchDialogBox() {
        fetchingImageDialog.cancel();
    }

    //setting the dialog box for authentication
    public void setAuthenticationDialog(Context context) {
        authenticationDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        authenticationDialog.getProgressHelper().setBarColor(Color.parseColor("#383838"));
        authenticationDialog.setTitleText("Authenticating");
        authenticationDialog.setCancelable(false);
        authenticationDialog.show();
    }


    //setting the user profile for the after the authentication process is complete and shows error if
    // any occured.
    public void PostAuthenticationResult(String userInfo) {
        if (userInfo.equals("Incorrect_Username") || userInfo.equals("Incorrect_Password"))

        {
            new SweetAlertDialog(this.activityContext, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("ERROR...")
                    .setContentText(userInfo + " Please check and try again")
                    .setConfirmText("Try Again")
                    .show();

        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            try {
                JSONObject User = new JSONObject(userInfo);
                intent.putExtra("USER_IMAGE", User.getString("image"));
                intent.putExtra("USER_NAME", User.getString("name"));
                intent.putExtra("USER_ADDRESS", User.getString("address"));
                intent.putExtra("USER_CITY", User.getString("city"));
                intent.putExtra("USER_COUNTRY", User.getString("country"));
                intent.putExtra("USER_POSTCODE", User.getString("postcode"));
            } catch (JSONException e) {
                Toast.makeText(this.activityContext, "cannot create JSON object", Toast.LENGTH_SHORT).show();
            }


            SharedPreferences sharedPreferences = this.activityContext.getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (remember_me.isChecked()) {

                try {
                    JSONObject User = new JSONObject(userInfo);
                    editor.putString(getString(R.string.USER_NAME), User.getString("name"));
                    editor.putString(getString(R.string.USER_ADDRESS), User.getString("address"));
                    editor.putString(getString(R.string.USER_CITY), User.getString("city"));
                    editor.putString(getString(R.string.USER_POSTCODE), User.getString("postcode"));
                    editor.putString(getString(R.string.USER_COUNTRY), User.getString("country"));
                    editor.putString(getString(R.string.USER_IMAGE), User.getString("image"));
                    editor.putString(getString(R.string.REMEMBER_ME), "Checked");
                    editor.commit();
                    Toast.makeText(this.activityContext, "REMEMBER ME ACTIVATED", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(this.activityContext, "cannot create JSON object", Toast.LENGTH_SHORT).show();
                }


            } else {
                editor.putString(getString(R.string.REMEMBER_ME), "UnChecked");
                editor.commit();
            }
            startActivity(intent);


        }


    }

    //setting the user profile if the user already have selected remember me
    public void setIntent(String rememberMe) {
        SharedPreferences sharedPreferences = this.activityContext.getSharedPreferences(getString(R.string.PREF_FILE), MODE_PRIVATE);
        //if the user previously has set the remember me then it will automatically take the user to the main screen
        if (rememberMe.equals("Checked")) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra("USER_IMAGE", sharedPreferences.getString(getString(R.string.USER_IMAGE), "USER_IMAGE"));
            intent.putExtra("USER_NAME", sharedPreferences.getString(getString(R.string.USER_NAME), "USER_NAME"));
            intent.putExtra("USER_ADDRESS", sharedPreferences.getString(getString(R.string.USER_ADDRESS), "USER_ADDRESS"));
            intent.putExtra("USER_CITY", sharedPreferences.getString(getString(R.string.USER_CITY), "USER_CITY"));
            intent.putExtra("USER_POSTCODE", sharedPreferences.getString(getString(R.string.USER_POSTCODE), "USER_POSTCODE"));
            intent.putExtra("USER_COUNTRY", sharedPreferences.getString(getString(R.string.USER_COUNTRY), "USER_COUNTRY"));
            startActivity(intent);
        }
    }


    //setting the context of the activity
    public void setActivityContext(Context context) {
        this.activityContext = context;
    }

    public void setRemember_me(CheckBox remember_me) {
        this.remember_me = remember_me;
    }
}
