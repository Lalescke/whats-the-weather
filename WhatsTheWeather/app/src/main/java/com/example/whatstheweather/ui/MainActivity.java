package com.example.whatstheweather.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.whatstheweather.R;
import com.example.whatstheweather.databinding.ActivityMainBinding;
import com.example.whatstheweather.httpEntity.ForecastDay;
import com.example.whatstheweather.httpEntity.Hour;
import com.example.whatstheweather.httpEntity.HttpEntity;
import com.example.whatstheweather.utils.AppSettings;
import com.example.whatstheweather.utils.Formats;
import com.example.whatstheweather.utils.HttpRequest;
//import com.example.whatstheweather.utils.HttpResponseMap;
//import com.example.whatstheweather.utils.VolleyCallBack;
import com.example.whatstheweather.utils.VolleyCallBackListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Formats formats = new Formats();
    HttpEntity httpEntity;
    ActivityMainBinding binding;
    private boolean isSearchOn = false;

    public MainActivity() {
        super();
        Log.e("MAIN ACTIVITY", "CONSTRUCTOR");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //isSearchOn = getIntent().getExtras().getBoolean("isSearchOn");
        //loadContent();

        binding.bottomNavigationViewList.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.home:
                    break;
                case R.id.list:
                    goToListActivity();
                    break;
                case R.id.search:
                    goToSearchActivity();
                    break;
            }
            return true;
        });

        Log.e("MAIN ACTIVITY", "onCreate()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("MAIN ACTIVITY", "onStart");
        /*
        if (getIntent().getExtras() != null){Log.e("ICIIIII", "bof");
            isSearchOn = getIntent().getExtras().getBoolean("isSearchOn");
        }
        Log.e("MAIN ACTIVITY", "isSearchOn: " + isSearchOn);
        if (isSearchOn)

         */
        binding.getRoot().setVisibility(View.GONE);
        if ( AppSettings.getLastSearched() != null && !AppSettings.getLastSearched().equals(""))
            loadContent();
        else
            goToSearchActivity();

        loadContent();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("MAIN ACTIVITY", "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MAIN ACTIVITY", "onResume");
        Log.e("MAIN ACTIVITY", "lastSearched city: " + AppSettings.getLastSearched());
    }

    public void goToSearchActivity() {
        Log.e("MAIN ACTIVITY","goToSearchActivity()");
        Intent searchActivity = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(searchActivity);
    }

    public void goToListActivity(){
        Log.e("MAIN ACTIVITY","goToListActivity()");
        Intent listActivity = new Intent(MainActivity.this, ListActivity.class);
        startActivity(listActivity);
    }

    public void loadContent(){
        String city = AppSettings.getLastSearched();
        if (city == null || city.equals("")){
            goToSearchActivity();
            return;
        }

        Log.e("MAIN ACTIVITY", "http avant");
        HttpRequest httpRequest = new HttpRequest(this, new VolleyCallBackListener() {
            @Override
            public void onSuccessResponse(HttpEntity response) {
                httpEntity = response;
                fillView();
                AppSettings.setLastSearched(httpEntity.location.name);
                if(AppSettings.getAutoSave()) {
                    AppSettings.deleteFromCitiesList(city);
                    AppSettings.setCitiesList(httpEntity.location.name);
                }
                Log.e("MAIN ACTIVITY", "http après");
            }
            @Override
            public void onErrorListener(String error) {
                Log.e("MAIN ACTIVITY", error);
                Toast.makeText(getApplicationContext(),error, Toast.LENGTH_SHORT).show();
                String cityToDelete = AppSettings.getLastSearched();
                AppSettings.deleteFromCitiesList(cityToDelete);
                goToSearchActivity();
            }
        });

        httpRequest.requestTest(10, city);
    }

    public void fillView() {
        TextView cityName = (TextView) findViewById(R.id.cityName);
        cityName.setText(httpEntity.location.name);

        // finalement on ne mets pas la date d'aujourd'hui
        //TextView currentDate = (TextView) findViewById(R.id.currentDate);
        //currentDate.setText(dateFormat(httpEntity.current.last_updated));

        TextView currentTemp = (TextView) findViewById(R.id.currentTemp);
        currentTemp.setText(""+ httpEntity.current.temp_c + "°C / " + httpEntity.current.temp_f + "°F");

        ImageView weatherCondition = (ImageView) findViewById(R.id.weatherCondition);
        Picasso.get().load("https:" + httpEntity.current.condition.icon).into(weatherCondition);

        TextView currentCondition = (TextView) findViewById(R.id.currentCondition);
        currentCondition.setText(httpEntity.current.condition.text);

        TextView currentWind = (TextView) findViewById(R.id.currentWind);
        currentWind.setText("- Wind: " + formats.kphToMs(httpEntity.current.wind_kph) + "m/s - " + formats.windDir(httpEntity.current.wind_dir));

        TextView currentTempMin = (TextView) findViewById(R.id.currentTempMin);
        currentTempMin.setText("- Min: " + httpEntity.forecast.get(0).day.mintemp_c + "°C / " + httpEntity.forecast.get(0).day.mintemp_f + "°F");

        TextView currentTempMax = (TextView) findViewById(R.id.currentTempMax);
        currentTempMax.setText("- Max: " + httpEntity.forecast.get(0).day.maxtemp_c + "°C / " + httpEntity.forecast.get(0).day.maxtemp_f + "°F");

        TextView currentPressure = (TextView) findViewById(R.id.currentPressure);
        currentPressure.setText("- Pressure: " + httpEntity.current.pressure_mb + "hPa");

        TextView currentHumidity = (TextView) findViewById(R.id.currentHumidity);
        currentHumidity.setText("- Humidity: " + httpEntity.current.humidity + "%");

        fillCurrentHours();
        fillFutureWeather();

        binding.getRoot().setVisibility(View.VISIBLE);
    }



    private void fillCurrentHours() {
        LinearLayout todaysWeatherLinear = findViewById(R.id.todaysWeatherLinear);

        if (todaysWeatherLinear.getChildCount() > 1) {
            todaysWeatherLinear.removeViews(1, todaysWeatherLinear.getChildCount() - 1);
        }
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        currentTime = currentTime.substring(0,2);
        Log.e("MAIN ACTIVITY", currentTime);
        for (Hour hour : httpEntity.forecast.get(0).hour) {Log.e("MAIN ACTIVITY", hour.time);

            if(!(Integer.parseInt(hour.time.substring(11,13)) < Integer.parseInt(currentTime))) {

                CurrentWeatherHourCard card = createCardHour();

                card.timeHour.setText(formats.hourFormat(hour.time));//hourFormat(hour.time)
                card.tempHour.setText("" + hour.temp_c + "°C");
                card.windSpeedHour.setText("" + formats.kphToMs(hour.wind_kph) + " m/s");
                Picasso.get().load("https:" + hour.condition.icon).into(card.conditionImageHour);
                card.conditionImageHour.setForegroundGravity(Gravity.CENTER_VERTICAL);

                todaysWeatherLinear.addView(card.cardHour);
            }
        }

        todaysWeatherLinear.getChildAt(0).setVisibility(View.GONE);
    }
    private CurrentWeatherHourCard createCardHour() {
        CurrentWeatherHourCard card = new CurrentWeatherHourCard(getApplicationContext());

        LinearLayout patternCard = findViewById(R.id.todaysWeatherTemplate);

        ViewGroup.LayoutParams cardLayout = patternCard.getLayoutParams();
        ViewGroup.LayoutParams timeHourLayout = patternCard.getChildAt(0).getLayoutParams();
        ViewGroup.LayoutParams tempHourLayout = patternCard.getChildAt(1).getLayoutParams();
        ViewGroup.LayoutParams conditionImageHourLayout = patternCard.getChildAt(2).getLayoutParams();
        ViewGroup.LayoutParams windSpeedHourLayout = patternCard.getChildAt(3).getLayoutParams();

        //ViewGroup.LayoutParams testIconLayout = new LinearLayout.LayoutParams(200,200);

        card.cardHour.setLayoutParams(cardLayout);
        card.timeHour.setLayoutParams(timeHourLayout);
        card.tempHour.setLayoutParams(tempHourLayout);
        card.windSpeedHour.setLayoutParams(windSpeedHourLayout);
        card.conditionImageHour.setLayoutParams(conditionImageHourLayout);

        return card;
    }

    static class CurrentWeatherHourCard {
        public LinearLayout cardHour;
        public TextView timeHour;
        public TextView tempHour;
        public TextView windSpeedHour;
        public ImageView conditionImageHour;

        public CurrentWeatherHourCard(Context context) {
            this.cardHour = new LinearLayout(context);

            this.timeHour = new TextView(context);
            this.tempHour = new TextView(context);
            this.windSpeedHour = new TextView(context);
            this.conditionImageHour = new ImageView(context);

            this.cardHour.setOrientation(LinearLayout.VERTICAL);
            this.cardHour.setGravity(Gravity.CENTER);
            this.cardHour.setBackgroundColor(Color.CYAN);
            /*
            this.timeHour.setGravity(Gravity.CENTER);
            //this.conditionImageHour.set
             */
            this.tempHour.setGravity(Gravity.CENTER);
            this.windSpeedHour.setGravity(Gravity.CENTER_HORIZONTAL);
            this.conditionImageHour.setForegroundGravity(Gravity.CENTER_VERTICAL);
            this.timeHour.setTextSize(15);
            this.tempHour.setTextSize(15);
            this.windSpeedHour.setTextSize(15);

            this.timeHour.setTextColor(ContextCompat.getColor(context,R.color.white));
            this.windSpeedHour.setTextColor(ContextCompat.getColor(context,R.color.white));
            this.tempHour.setTextColor(ContextCompat.getColor(context,R.color.white));

            this.timeHour.setTextSize(15);
            this.tempHour.setTextSize(15);
            this.windSpeedHour.setTextSize(15);
            this.cardHour.setBackground(ContextCompat.getDrawable(context,R.drawable.background_today_weather));
            this.cardHour.addView(this.timeHour);
            this.cardHour.addView(this.tempHour);
            this.cardHour.addView(this.windSpeedHour);
            this.cardHour.addView(this.conditionImageHour);
        }
    }

    private void fillFutureWeather() {
        LinearLayout containerFuture = findViewById(R.id.containerFuture);

        if (containerFuture.getChildCount() > 1) {
            containerFuture.removeViews(1, containerFuture.getChildCount() - 1);
        }
        boolean isCurrentDay = true;
        for (ForecastDay forecastDay : httpEntity.forecast) {
            if(!isCurrentDay) {
                FutureWeatherCard card = createCardFuture();


                card.timeFuture.setText(formats.dateFormat(forecastDay.date));// //dateFormat(forecastDay.date)
                card.tempFuture.setText("" + forecastDay.day.avgtemp_c + "°C / " + forecastDay.day.maxtemp_f + "°F");

                Picasso.get().load("https:" + forecastDay.day.condition.icon).into(card.conditionImageFuture);
                card.conditionImageFuture.setForegroundGravity(Gravity.CENTER_VERTICAL);

                containerFuture.addView(card.cardFuture);
            }
            isCurrentDay = false;
        }

        containerFuture.getChildAt(0).setVisibility(View.GONE);
    }
    private FutureWeatherCard createCardFuture() {
        FutureWeatherCard card = new FutureWeatherCard(getApplicationContext());

        LinearLayout patternCard = findViewById(R.id.containerTemplateFuture);
        LinearLayout TemplateLeftFuture = findViewById(R.id.TemplateLeftFuture);

        ViewGroup.LayoutParams cardLayout = patternCard.getLayoutParams();
        ViewGroup.LayoutParams LeftFutureLayout = TemplateLeftFuture.getLayoutParams();
        ViewGroup.LayoutParams timeFutureLayout = TemplateLeftFuture.getChildAt(0).getLayoutParams();
        ViewGroup.LayoutParams tempFutureLayout = TemplateLeftFuture.getChildAt(1).getLayoutParams();
        ViewGroup.LayoutParams conditionImageFutureLayout = patternCard.getChildAt(1).getLayoutParams();

        card.cardFuture.setLayoutParams(cardLayout);
        card.LeftFuture.setLayoutParams(LeftFutureLayout);
        card.timeFuture.setLayoutParams(timeFutureLayout);
        card.tempFuture.setLayoutParams(tempFutureLayout);
        card.conditionImageFuture.setLayoutParams(conditionImageFutureLayout);

        return card;
    }

    static class FutureWeatherCard {
        public LinearLayout cardFuture;
        public LinearLayout LeftFuture;
        public TextView timeFuture;
        public TextView tempFuture;
        public ImageView conditionImageFuture;

        public FutureWeatherCard(Context context) {
            this.cardFuture = new LinearLayout(context);

            this.LeftFuture = new LinearLayout(context);
            this.timeFuture = new TextView(context);
            this.tempFuture = new TextView(context);
            this.conditionImageFuture = new ImageView(context);

            this.cardFuture.setOrientation(LinearLayout.HORIZONTAL);
            this.cardFuture.setBackgroundColor(Color.CYAN);
            //this.cardFuture.setMinimumHeight(200);
            //this.cardFuture.setMinimumWidth(1000);

            this.LeftFuture.setOrientation(LinearLayout.VERTICAL);

            this.timeFuture.setGravity(Gravity.START);
            this.tempFuture.setGravity(Gravity.CENTER);

            this.tempFuture.setTextColor(ContextCompat.getColor(context,R.color.white));
            this.timeFuture.setTextColor(ContextCompat.getColor(context,R.color.white));
            this.cardFuture.setBackground(ContextCompat.getDrawable(context,R.drawable.background_future_weather));
            this.tempFuture.setTextSize(27);
            this.timeFuture.setTextSize(15);

            this.LeftFuture.addView(this.timeFuture);
            this.LeftFuture.addView(this.tempFuture);

            this.cardFuture.addView(this.LeftFuture);
            this.cardFuture.addView(this.conditionImageFuture);
        }
    }
}