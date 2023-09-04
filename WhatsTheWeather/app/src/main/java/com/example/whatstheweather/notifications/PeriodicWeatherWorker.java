package com.example.whatstheweather.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.whatstheweather.WeatherApplication;
import com.example.whatstheweather.httpEntity.HttpEntity;
import com.example.whatstheweather.ui.MainActivity;
import com.example.whatstheweather.utils.AppSettings;
import com.example.whatstheweather.utils.HttpRequest;
import com.example.whatstheweather.utils.VolleyCallBackListener;

public class PeriodicWeatherWorker extends Worker {
        public static final String PROCESSING_WORK_UNIQUE_NAME = "WEATHER_CURRENT_WEATHER";
        public HttpEntity httpEntity;

        public PeriodicWeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {

            httpRequest();

            return Result.success();
        }

        public void httpRequest(){
            String city = AppSettings.getWorkerCity();
            if (city == null || city.equals(""))
                return;

            Log.e("WORKER", "http avant");
            HttpRequest httpRequest = new HttpRequest(WeatherApplication.getContext(), new VolleyCallBackListener() {
                @Override
                public void onSuccessResponse(HttpEntity response) {
                    httpEntity = response;
                    sendNotification();
                }
                @Override
                public void onErrorListener(String error) {
                    //TODO: mettre une notif "gros beta rentre une bonne ville"
                    // finalement pas besoin: la requête de setCitiesLIst fait la vérif
                    String cityToDelete = AppSettings.getLastSearched();
                    AppSettings.deleteFromCitiesList(cityToDelete);
                    AppSettings.setWorkerCity(null);
                }
            });

            httpRequest.requestTest(2, city);
        }

        public void sendNotification(){
            Context context = WeatherApplication.getContext();
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            String title = "" + httpEntity.current.temp_c + "° " + httpEntity.location.name;
            String content = "Maximale " + httpEntity.forecast.get(0).day.maxtemp_c + "° | Minimale " + httpEntity.forecast.get(0).day.mintemp_c + "°";
            if (httpEntity.forecast.get(0).day.daily_will_it_rain == 1){
                content += "\nProbabilités de pluie aujourd'hui: " + httpEntity.forecast.get(0).day.daily_chance_of_rain + "%";
            }
            if (httpEntity.forecast.get(0).day.daily_will_it_snow == 1){
                content += "\nProbabilités de neige aujourd'hui: " + httpEntity.forecast.get(0).day.daily_chance_of_snow + "%";
            }
            String url = "https:" + httpEntity.current.condition.icon;


            CurrentWeatherNotification mandatoryUpdateNotification = new CurrentWeatherNotification(pendingIntent, title, content, url);
            mandatoryUpdateNotification.n0tify();
        }


    }
