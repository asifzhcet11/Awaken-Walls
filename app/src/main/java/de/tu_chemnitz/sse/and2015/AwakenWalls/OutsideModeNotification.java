package de.tu_chemnitz.sse.and2015.AwakenWalls;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

/**
 * Created by mohammadasif on 28/01/2017.
 * This is used to create the notification when the any intruder is detected
 */

public class OutsideModeNotification {

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private MqttDataRetrieveService mqttDataRetrieveService;

    public OutsideModeNotification(NotificationCompat.Builder notificationBuilder, NotificationManager notificationManager, MqttDataRetrieveService mqttDataRetrieveService) {
        this.notificationBuilder         = notificationBuilder;
        this.notificationManager         = notificationManager;
        this.mqttDataRetrieveService     = mqttDataRetrieveService;
    }

    public void CreateNotification(){

        Intent ImageActivityIntent = new Intent(this.mqttDataRetrieveService, StreamVideo.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.mqttDataRetrieveService);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(ImageActivityIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        this.notificationBuilder.setContentIntent(pendingIntent);
        this.notificationBuilder.setSmallIcon(R.drawable.app_logo);
        this.notificationBuilder.setContentTitle("Alert!!! someone in the room");
        this.notificationBuilder.setContentText("Please click to view Photos or Live Stream");
        this.notificationBuilder.setAutoCancel(true);
        this.notificationManager.notify(1, notificationBuilder.build());
    }
}
