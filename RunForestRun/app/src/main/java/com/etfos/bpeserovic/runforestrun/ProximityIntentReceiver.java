package com.etfos.bpeserovic.runforestrun;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Map;

import static android.content.Context.CAMERA_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Bobo on 22.6.2017..
 */

public class ProximityIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BORIS", "Broadcast recieved");

        if(intent.hasExtra(LocationManager.KEY_PROXIMITY_ENTERING)){
            if(intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING,false)){
                Log.d("BORIS", "Broadcast is for entering the perimeter.");

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(context);


                builder.setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(MapActivity.alertTitle)
                        .setContentText(MapActivity.alertText)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);

                intent = new Intent("android.media.action.IMAGE_CAPTURE");
// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
                final PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(resultPendingIntent);

                Notification notification = builder.build();
                notificationManager.notify(0,notification);
            }
        }
    }
}
