package com.example.whatstheweather.notifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.whatstheweather.R;
import com.example.whatstheweather.WeatherApplication;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class BaseNotification {
    private Notification notification;
    private final String channelId;
    private Bitmap mybitmap;

    public BaseNotification(String channelId, String title, String content, PendingIntent pendingIntent, String url) {
        this.channelId = channelId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(channelId, "Notification");
        }

        boolean setAutoCancel = false;
        boolean setOngoing = false;
        boolean setOnlyAlertOnce = true;

        //TODO: soit ça marche pas soit il faut faire en async
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mybitmap = bitmap;
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        Picasso.get().load(url).into(target);

        this.notification = new NotificationCompat.Builder(WeatherApplication.getContext(), channelId)
                .setContentTitle(title)
                .setTicker(title)
                .setContentText(content) //affiche le début du content avec "..."
                .setStyle(new NotificationCompat.BigTextStyle() //affiche tout le content
                        .bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.icon_v1)
                .setLargeIcon(mybitmap)
                .setContentIntent(pendingIntent) // use this to go to MainActivity when tapping on notification
                .setAutoCancel(setAutoCancel) // use this to auto cancel the notification when tapping on it
                .setOngoing(setOngoing) // permet d'empêcher de supprimer la notif tant que le service est actif
                .setVibrate(new long[] { 10000, 10000, 10000, 1000, 10000 })
                .setLights(Color.WHITE, 3000, 3000)
                .setOnlyAlertOnce(setOnlyAlertOnce) // doesn't relaunch notif if already launched
                .build();
    }

    //TODO: je sais plus si c'est utile
    public void n0tify() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(WeatherApplication.getContext());
        notificationManagerCompat.notify(Integer.parseInt(channelId), notification);
    }

    // peut être utile dans certains cas
    //@SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.O) //pas nécessaire à 100%: j'ai déjà mis la condition dans l'appel de la fonction
    static private void createChannel(String channelId, String description) {
        CharSequence name = "What's the Weather";
        NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(description);

        NotificationManager notificationManager = WeatherApplication.getContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
