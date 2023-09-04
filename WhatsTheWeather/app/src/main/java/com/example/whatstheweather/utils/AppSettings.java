package com.example.whatstheweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.whatstheweather.WeatherApplication;

import org.json.JSONArray;

import java.lang.reflect.Array;

// AppSettings: enregistrer des infos sous la forme clef-valeur
public class AppSettings {

    /*
    Liste des shared preferences utilisés:
        - cities list, key: "CitiesList"
        - auto set list on search, key: "autoSave"
        - last searched city, key: "lastSearched"
        - boolean accept notifications, key: "acceptWorker"
     */


    public static String[] getCitiesList() { // un peu compliqué pour rien, c'est con
        String cities = "";
        String[] citiesList = {};
        try {
            JSONArray jsonArray = new JSONArray(getString("CitiesList", "[]"));
            cities = jsonArray.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!cities.equals("[]")) {
            cities = cities.substring(2, cities.length() - 2);
            citiesList = cities.split("\",\"");
        }
        return citiesList;
    }

    public static void setCitiesList(String newCity) {
        JSONArray jsonArray = new JSONArray();
        boolean writeNewCity=true;

        String[] citiesList = getCitiesList();
        for (String s : citiesList) {
            jsonArray.put(s);
            if (s.equals(newCity))
                writeNewCity=false;
        }
        if (writeNewCity)
            jsonArray.put(newCity);
        setString("CitiesList", jsonArray.toString());
    }

    public static void deleteFromCitiesList(String city) {
        JSONArray jsonArray = new JSONArray();

        String[] citiesList = getCitiesList();
        for (String s : citiesList) {
            if (!s.equals(city))
                jsonArray.put(s);
        }
        setString("CitiesList", jsonArray.toString());
    }

    public static boolean getAutoSave() {
        return getBoolean("autoSave", false);
    }
    public static void setAutoSave(boolean autoSave) {
        setBoolean("autoSave", autoSave);
    }

    public static String getLastSearched() {
        return getString("lastSearched", null);
    }
    public static void setLastSearched(String lastSearched) {
        setString("lastSearched", lastSearched);
    }

    public static boolean getAcceptWorker() {
        return getBoolean("acceptWorker", false);
    }
    public static void setAcceptWorker(boolean acceptWorker) {setBoolean("acceptWorker", acceptWorker);}

    public static String getWorkerCity() {
        return getString("workerCity", null);
    }
    public static void setWorkerCity(String workerCity) {
        setString("workerCity", workerCity);
    }


    /** partie set/get **/


    private static SharedPreferences getPreferences() {
        return WeatherApplication
                .getContext()
                .getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE);
    }

    private static boolean getBoolean(String key, boolean defaultValue) {
        return getPreferences().getBoolean(key, defaultValue);
    }

    private static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static int getInteger(String key, int defaultValue) {
        return getPreferences().getInt(key, defaultValue);
    }

    private static void setInteger(String key, int value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private static long getLongInteger(String key, long defaultValue) {
        return getPreferences().getLong(key, defaultValue);
    }

    private static void setLongInteger(String key, long value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putLong(key, value);
        editor.apply();
    }

    private static String getString(String key, String defaultValue) {
        return getPreferences().getString(key, defaultValue);
    }

    private static void setString(String key, String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }
}
