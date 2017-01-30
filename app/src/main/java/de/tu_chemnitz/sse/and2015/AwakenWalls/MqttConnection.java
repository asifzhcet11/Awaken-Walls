package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.content.Context;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;



/**
 * Created by mohammadasif on 10/01/2017.
 *
 * This is used to create a custom mqtt connection and it returns the
 * mqtt client by using init() function
 */

public class MqttConnection {

    private Context context;
    private MqttAndroidClient client;

    //Detaials for the broker
    private final String USERNAME = "zkgdqfep";
    private final String PASSWORD = "ufch_ScZ4crM";
    private final String HOSTNAME = "tcp://m21.cloudmqtt.com:17498";

    //array of topic to be subcscribed
    private String topics[];

    //arary of the quality of service for the subscribed topic
    private int qos[];

    //clientID for the mqtt client
    private String clientId;


    public MqttConnection(Context context, String topics[], int qos[]) {
        this.context = context;
        this.topics = topics;
        this.qos = qos;
    }

    public MqttAndroidClient init() {

        MqttConnectOptions options = new MqttConnectOptions();

        //connect to client with the username and password
        options.setUserName(USERNAME);
        options.setAutomaticReconnect(true);
        options.setPassword(PASSWORD.toCharArray());

        clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(this.context, HOSTNAME, clientId);

        try {


            IMqttToken token = client.connect(options);

            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Toast.makeText(context, "Connected..", Toast.LENGTH_SHORT).show();

                    IMqttToken subToken = null;


                    try {


                        subToken = client.subscribe(topics, qos);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    subToken.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {

                            Toast.makeText(context, "Success Subscription..", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken,
                                              Throwable exception) {


                            Toast.makeText(context, "Failed Subscription..", Toast.LENGTH_SHORT).show();

                        }


                    });

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {


                    Toast.makeText(context, "Failed Connection..", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        return client;

    }


}
