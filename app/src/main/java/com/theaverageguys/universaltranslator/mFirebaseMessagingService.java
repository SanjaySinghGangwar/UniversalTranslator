package com.theaverageguys.universaltranslator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.theaverageguys.universaltranslator.activity.Splash;
import com.theaverageguys.universaltranslator.activity.donate;

import static com.theaverageguys.universaltranslator.sevices.utils.foregrounded;

public class mFirebaseMessagingService extends FirebaseMessagingService {

    Intent intentToOpen;
    PendingIntent intent;
    String TAG = "FCM ";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i("FCM ", "onMessageReceived: " + remoteMessage.getData());
        if (remoteMessage.getData() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            receiveData(remoteMessage);
        }
    }


    private void receiveData(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().containsKey("link")) {
            if (!remoteMessage.getData().get("link").isEmpty()) {
                if (foregrounded()) {
                    Log.i(TAG, "receiveData: inside Link");
                    intentToOpen = new Intent(Intent.ACTION_VIEW);
                    intentToOpen.setData(Uri.parse(remoteMessage.getData().get("link")));
                } else {
                    Log.i(TAG, "receiveData: inside Link Else");
                    intentToOpen = new Intent(this, donate.class);
                }

            }
        } else {
            Log.i(TAG, "receiveData: inside else");
            intentToOpen = new Intent(this, Splash.class);
        }
        intent = PendingIntent.getActivities(this, 1, new Intent[]{intentToOpen}, PendingIntent.FLAG_UPDATE_CURRENT);
        caeateNotfication(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

    }

    public void caeateNotfication(String title, String body) {
        NotificationManager notificationManager = null;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = getString(R.string.default_notification_channel_id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "With this you will be receiving the notification", NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel.
            notificationChannel.setDescription("All Notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            /*notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);*/
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.transparent_image)
                .setTicker(getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(intent)
                .setContentInfo("zero");
        notificationManager.notify(1, notificationBuilder.build());
    }

}
