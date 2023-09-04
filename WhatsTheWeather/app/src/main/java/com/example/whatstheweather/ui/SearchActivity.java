package com.example.whatstheweather.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.whatstheweather.R;
import com.example.whatstheweather.databinding.ActivityMainBinding;
import com.example.whatstheweather.databinding.ActivitySearchBinding;
import com.example.whatstheweather.httpEntity.HttpEntity;
import com.example.whatstheweather.notifications.PeriodicWeatherWorker;
import com.example.whatstheweather.utils.AppSettings;
import com.example.whatstheweather.utils.HttpRequest;
import com.example.whatstheweather.utils.VolleyCallBackListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SearchActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    ActivitySearchBinding binding;
    private HttpEntity httpEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.e("SEARCH ACTIVITY", "onCreate()");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        EditText textCity = (EditText) findViewById(R.id.textCitySearch);

        Switch autoSwitch = findViewById(R.id.switchAutoSetList);
        Switch workerSwitch = findViewById(R.id.switchWorker);
        Button startSearch = findViewById(R.id.startSearch);
        Button setCitiesList = findViewById(R.id.setCitiesListFromSearch);
        Button geolocationSearch = findViewById(R.id.geolocationSearch);

        autoSwitch.setChecked(isAutoChecked());
        if (isAutoChecked()) {
            setCitiesList.setVisibility(View.GONE);
        }
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.e("SEARCH ACTIVITY", "onCheckedChanged: " + isChecked);
                    setAutoChecked(true);
                    setCitiesList.setVisibility(View.GONE);
                } else {
                    Log.e("SEARCH ACTIVITY", "onCheckedChanged: " + isChecked);
                    setAutoChecked(false);
                    setCitiesList.setVisibility(View.VISIBLE);
                }
            }
        });
        workerSwitch.setChecked(AppSettings.getAcceptWorker());
        workerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.e("SEARCH ACTIVITY", "onCheckedChanged: " + isChecked);
                    setAcceptWorker(true);
                } else {
                    Log.e("SEARCH ACTIVITY", "onCheckedChanged: " + isChecked);
                    setAcceptWorker(false);
                    //normalement ça marche (j'ai testé mais on sait jamais)
                    WorkManager.getInstance().cancelUniqueWork(PeriodicWeatherWorker.PROCESSING_WORK_UNIQUE_NAME);
                }
            }
        });

        binding.bottomNavigationViewList.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.home:
                    goToMainActivity(false);
                    break;
                case R.id.list:
                    goToListActivity();
                    break;
                case R.id.search:
                    break;
            }
            return true;
        });

        startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchCity(textCity.getText().toString());
            }
        });

        setCitiesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = textCity.getText().toString();
                if (!isContent(city))
                    return;
                setCitiesList(city, true);
            }
        });

        geolocationSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLocationPermissionGranted();
            }
        });

    }

    private void goToMainActivity(boolean isSearchOn) {
        Log.e("SEARCH ACTIVITY", "goToMainActivity()");
        Intent mainActivity = new Intent(SearchActivity.this, MainActivity.class);
        mainActivity.putExtra("isSearchOn", isSearchOn);
        startActivity(mainActivity);
        finishActivity();
    }
    public void goToListActivity(){
        Log.e("MAIN ACTIVITY","goToListActivity()");
        Intent listActivity = new Intent(SearchActivity.this, ListActivity.class);
        startActivity(listActivity);
        finishActivity();
    }

    private void finishActivity() {
        Log.e("SEARCH ACTIVITY", "finishActivity()");
        this.finish();
    }

    private boolean isContent(String city){
        Toast toast= Toast.makeText(getApplicationContext(), "Please enter a city", Toast.LENGTH_SHORT);;
        if (city.equals("")){
            toast.show();
            return false;
        }
        toast.cancel();
        return true;
    }

    private void saveIfIsChecked(String city){
        if (!isContent(city))
            return;
        if (isAutoChecked()) {
            Log.e("SEARCH ACTIVITY", "startSearch: autoSave true, " + city);
            setCitiesList(city, false);
        } else {
            Log.e("SEARCH ACTIVITY", "startSearch: autoSave false, " + city);
        }
    }

    private void startSearchCity(String city) {
        saveIfIsChecked(city);
        AppSettings.setLastSearched(city);

        goToMainActivity(true);
    }

    private void startSearchGeolocation() {
        String city = getGeolocation();
        AppSettings.setLastSearched(city);
        if (city == null) {
            Toast.makeText(this, "Impossible de récupérer votre géolocalisation", Toast.LENGTH_SHORT).show();
            return;
        }
        if (city.equals("not found")) {
            Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
            return;
        }
        if (city.equals("geolocation null")) {
            Toast.makeText(this, "please activate your phone's geolocation", Toast.LENGTH_SHORT).show();
            return;
        }
        if (city.equals("geolocation not allowed")) {
            Toast.makeText(this, "geolocation not allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        saveIfIsChecked(city);

        goToMainActivity(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if (!(grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "access to your geolocation (even coarse) is needed for this feature!", Toast.LENGTH_LONG).show();
                return;
            } else if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "You chose coarse location only. The weather might not be exact ...", Toast.LENGTH_LONG).show();
            }
            startSearchGeolocation();
        }
    }

    private void isLocationPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
            return;
        }
        startSearchGeolocation();
    }

    private String getGeolocation() {
        String city = "not found";
        Location location = null;
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return "geolocation not allowed";
        }
        List<String> providers = locationManager.getProviders(true);
        for (int i=providers.size()-1; i>=0; i--) {
            location = locationManager.getLastKnownLocation(providers.get(i));
            if (location != null) break;
        }
        if (location == null){
            Log.e("SEARCH ACTIVITY", "location is null");
            return "geolocation null";
        }

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10);
            for (Address address : addresses){
                if (address != null && address.getLocality() != null && !address.getLocality().equals("")){
                    city = address.getLocality();
                    break;
                }
            }
        } catch (IOException e) {
            Log.e("SEARCH ACTIVITY", "Geolocation: " + e );
        }
        return city;
    }

    private void setCitiesList(String newCity, boolean isToast){
        HttpRequest httpRequest = new HttpRequest(this, new VolleyCallBackListener() {
            @Override
            public void onSuccessResponse(HttpEntity response) {
                httpEntity = response;
                AppSettings.setCitiesList(httpEntity.location.name);
                if (isToast)
                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onErrorListener(String error) {
                Toast.makeText(getApplicationContext(), "please enter a valid city", Toast.LENGTH_SHORT).show();
            }
        });

        httpRequest.requestTest(1, newCity);
    }

    private boolean isAutoChecked(){
        return AppSettings.getAutoSave();
    }
    private void setAutoChecked(boolean autoSave){
        AppSettings.setAutoSave(autoSave);
    }
    private void setAcceptWorker(boolean acceptWorker){AppSettings.setAcceptWorker(acceptWorker);}

}
