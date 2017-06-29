package com.etfos.bpeserovic.runforestrun;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Bobo on 22.6.2017..
 */

public class ProximityIntentReceiver extends BroadcastReceiver {

//    private static final int NOTIFICATION_ID = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        String key = LocationManager.KEY_PROXIMITY_ENTERING;
        Boolean entering = intent.getBooleanExtra(key, false);


        if (entering) {
            Log.d(getClass().getSimpleName(), "entering");
        } else {
            Log.d(getClass().getSimpleName(), "exiting");
        }

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                context,0,null,PendingIntent.FLAG_UPDATE_CURRENT);
// Compat builder should be used to create the notification when working
// with api level 15 and lower
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setAutoCancel(true)
                .setContentTitle(MapActivity.alertTitle)
                .setContentText(MapActivity.alertText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(notificationPendingIntent)
                .setLights(Color.BLUE, 2000, 1000)
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        Notification notification = notificationBuilder.build();
// When you have a notification, call notify on the notification manager object
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
// When the notification is sent, this activity is no longer neccessary
//        this.finish();

        //todo drugi način?
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, null, 0);
//        Notification notification = createNotification();
//
//        notificationManager.notify(NOTIFICATION_ID, notification);
//    }
//
//    private Notification createNotification() {
//        Notification notification = new Notification();
//
//        notification.when = System.currentTimeMillis();
//
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
//
//        notification.defaults |= Notification.DEFAULT_VIBRATE;
//        notification.defaults |= Notification.DEFAULT_LIGHTS;
//
//        return notification;
//    }
    }
}
