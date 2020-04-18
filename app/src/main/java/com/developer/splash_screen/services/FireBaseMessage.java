package com.developer.splash_screen.services;

import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.TokenWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.developer.splash_screen.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireBaseMessage extends FirebaseMessagingService {

    NotificationCompat.Builder builder;
    int mNotificationPriority;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Log.d("TokenNew",s);
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("onMessageRecieved" ,"onMessageRecieved");
        showNotificatiton(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
    }

    public void showNotificatiton(String message, String title) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
             mNotificationPriority = NotificationManager.IMPORTANCE_DEFAULT;
        else
            mNotificationPriority = NotificationCompat.PRIORITY_DEFAULT;

        builder = new NotificationCompat.Builder(this, "MyNotifications")
                .setSmallIcon(R.drawable.fox)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(mNotificationPriority);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999, builder.build());

    }
}
