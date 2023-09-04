package com.example.whatstheweather.notifications;

import android.app.PendingIntent;

//TODO: faire pareil mais pour la notif quand la ville n'existe pas
// finalement pas besoin: la requête de setCities List fait la vérif
public class CurrentWeatherNotification extends BaseNotification {
    static private final String SESSION_EXPIRED_CHANNEL_ID = "1";

    public CurrentWeatherNotification(PendingIntent pendingIntent, String title, String content, String url) {
        super(SESSION_EXPIRED_CHANNEL_ID, title, content, pendingIntent, url);
    }
}
