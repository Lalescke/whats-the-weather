package com.example.whatstheweather;

import android.app.Application;
import android.content.Context;

public class WeatherApplication extends Application {
    private static WeatherApplication instance;

    // pas utilisé ici mais j'ai cru comprendre que c'était important
    public static WeatherApplication getInstance(){
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }
}
