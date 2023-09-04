package com.example.whatstheweather.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.whatstheweather.R;
import com.example.whatstheweather.WeatherApplication;
import com.example.whatstheweather.databinding.ActivityListBinding;
import com.example.whatstheweather.httpEntity.HttpEntity;
import com.example.whatstheweather.notifications.PeriodicWeatherWorker;
import com.example.whatstheweather.utils.AppSettings;
import com.example.whatstheweather.utils.HttpRequest;
import com.example.whatstheweather.utils.VolleyCallBackListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListActivity extends AppCompatActivity {

    ActivityListBinding binding;
    HttpEntity httpEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.e("LIST ACTIVITY", "onCreate()");

        fillCitiesList();

        binding.bottomNavigationViewList.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.home:
                    goToMainActivity();
                    break;
                case R.id.list:
                    break;
                case R.id.search:
                    goToSearchActivity();
                    break;
            }
            return true;
        });

        EditText textCity = (EditText) findViewById(R.id.textCity);
        Button setCitiesList = (Button) findViewById(R.id.setCitiesList);
        setCitiesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCitiesList(textCity.getText().toString());
                textCity.getText().clear();
            }
        });
    }

    public void goToMainActivity(){
        Log.e("LIST ACTIVITY","goToMainActivity()");
        Intent mainActivity = new Intent(ListActivity.this, MainActivity.class);
        startActivity(mainActivity);
        finishActivity();
    }
    public void goToSearchActivity(){
        Log.e("LIST ACTIVITY","goToMainActivity()");
        Intent searchActivity = new Intent(ListActivity.this, SearchActivity.class);
        startActivity(searchActivity);
        finishActivity();
    }

    public void finishActivity(){
        Log.e("LIST ACTIVITY", "finishActivity()");
        this.finish();
    }

    private void fillCitiesList() {
        TableLayout citiesListTable = findViewById(R.id.citiesListTable);

        String[] citiesList = getCitiesList();

        if (citiesList == null || citiesList.length == 0) {
            findViewById(R.id.noCity).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.noCity).setVisibility(View.GONE);

            if (citiesListTable.getChildCount() > 2) {
                citiesListTable.removeViews(2, citiesListTable.getChildCount() - 2);
            }

            for (String city : citiesList) {
                CityListRow row = createCityListRow();

                row.cityName.setText(city);
                row.cityName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppSettings.setLastSearched(city);
                        goToMainActivity();
                    }
                });
                row.deleteCity.setText("X");
                row.deleteCity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFromCitiesList(city);
                        citiesListTable.removeView(row.cityRow);
                        fillCitiesList();
                    }
                });
                if (AppSettings.getAcceptWorker()) {
                    row.enableWorker.setText("Notify");
                    row.enableWorker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nMgr.cancelAll();
                            AppSettings.setWorkerCity(city);
                            startWorker();
                            Toast.makeText(getApplicationContext(), "You will get a notification shortly", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    row.enableWorker.setVisibility(View.GONE);
                }

                citiesListTable.addView(row.cityRow);
            }
        }
    }
    private CityListRow createCityListRow() {
        CityListRow cityListRow = new CityListRow(getApplicationContext());

        TableRow patternRow = findViewById(R.id.rowPatternCityList);

        ViewGroup.LayoutParams rowLayout = patternRow.getLayoutParams();
        ViewGroup.LayoutParams cityNameLayout = patternRow.getChildAt(0).getLayoutParams();
        ViewGroup.LayoutParams deleteCityLayout = patternRow.getChildAt(1).getLayoutParams();
        ViewGroup.LayoutParams enableWorkerLayout = patternRow.getChildAt(2).getLayoutParams();

        cityListRow.cityRow.setLayoutParams(rowLayout);
        cityListRow.cityName.setLayoutParams(cityNameLayout);
        cityListRow.deleteCity.setLayoutParams(deleteCityLayout);
        cityListRow.enableWorker.setLayoutParams(enableWorkerLayout);

        return cityListRow;
    }

    static class CityListRow {
        public TableRow cityRow;
        public TextView cityName;
        public Button deleteCity;
        public Button enableWorker;

        public CityListRow(Context context) {
            this.cityRow = new TableRow(context);

            this.cityName = new TextView(context);
            this.deleteCity = new Button(context);
            this.enableWorker = new Button(context);

            this.cityName.setTextColor(Color.WHITE);
            this.deleteCity.setTextColor(Color.RED);
            this.enableWorker.setTextColor(Color.WHITE);

            this.cityName.setGravity(Gravity.CENTER);
            this.deleteCity.setGravity(Gravity.CENTER);
            this.enableWorker.setGravity(Gravity.CENTER);

            this.deleteCity.setMinWidth(0);
            this.deleteCity.setMinimumWidth(0);

            this.enableWorker.setMinWidth(0);
            this.enableWorker.setMinimumWidth(0);

            this.cityRow.addView(this.cityName);
            this.cityRow.addView(this.deleteCity);
            this.cityRow.addView(this.enableWorker);
        }
    }

    private void startWorker(){
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(PeriodicWeatherWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        workManager.enqueueUniquePeriodicWork(PeriodicWeatherWorker.PROCESSING_WORK_UNIQUE_NAME, ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest);
    }

    private void setCitiesList(String newCity){
        Toast toast = Toast.makeText(getApplicationContext(), "Please enter a city", Toast.LENGTH_SHORT);
        if (newCity.equals("")) {
            toast.show();
            return;
        }

        HttpRequest httpRequest = new HttpRequest(this, new VolleyCallBackListener() {
            @Override
            public void onSuccessResponse(HttpEntity response) {
                httpEntity = response;
                AppSettings.setCitiesList(httpEntity.location.name);
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                fillCitiesList();
            }
            @Override
            public void onErrorListener(String error) {
                Toast.makeText(getApplicationContext(), "please enter a valid city", Toast.LENGTH_SHORT).show();
            }
        });

        httpRequest.requestTest(1, newCity);

    }
    private String[] getCitiesList(){
        String[] citiesList = AppSettings.getCitiesList();
        for (String s : citiesList) {
            Log.e("LIST ACTIVITY", s);
        }
        List<String> list = Arrays.asList(citiesList);
        Collections.reverse(list);
        citiesList = list.toArray(new String[0]);
        return citiesList;
    }
    private void deleteFromCitiesList(String city){AppSettings.deleteFromCitiesList(city);}
}
