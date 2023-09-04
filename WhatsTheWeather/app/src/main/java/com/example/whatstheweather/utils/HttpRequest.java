package com.example.whatstheweather.utils;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.whatstheweather.WeatherApplication;
import com.example.whatstheweather.httpEntity.Condition;
import com.example.whatstheweather.httpEntity.Current;
import com.example.whatstheweather.httpEntity.Day;
import com.example.whatstheweather.httpEntity.ForecastDay;
import com.example.whatstheweather.httpEntity.Hour;
import com.example.whatstheweather.httpEntity.HttpEntity;
import com.example.whatstheweather.httpEntity.MLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HttpRequest {

    final String API_KEY = AppConfig.API_KEY;
    String baseUrl = "https://api.weatherapi.com/v1/forecast.json?key=";
    VolleyCallBackListener listener = null;
    Context context;
    RequestQueue requestQueue;

    public HttpRequest(Context context, VolleyCallBackListener listener){
        this.context = context;
        this.listener = listener;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void requestTest(int days, String city) {
        Log.e("HTTP", "before request");
        String url = baseUrl + API_KEY + "&q=" + city + "&days=" + days + "&aqui=no&alert=no";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("HTTP", "response");
                        try {
                            HttpEntity toReturn = resultMapping(response);
                            listener.onSuccessResponse(toReturn);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        listener.onErrorListener("nom de ville invalide");
                        Log.e("HTTP ERROR", "error: " + error.toString());
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    private HttpEntity resultMapping(JSONObject response) throws JSONException {

        HttpEntity httpEntity = new HttpEntity();
        MLocation mLocation = new MLocation();
        Current current = new Current();
        Condition condition = new Condition();
        httpEntity.forecast = new ArrayList<ForecastDay>();

        mLocation.name = response.getJSONObject("location").getString("name");
        mLocation.country = response.getJSONObject("location").getString("country");
        mLocation.localTime = response.getJSONObject("location").getString("localtime");

        httpEntity.location = mLocation;

        condition.text = response.getJSONObject("current").getJSONObject("condition").getString("text");
        condition.icon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
        current.condition = condition;
        current.wind_kph = response.getJSONObject("current").getDouble("wind_kph");
        current.wind_mph = response.getJSONObject("current").getDouble("wind_mph");
        current.humidity = response.getJSONObject("current").getDouble("humidity");
        current.last_updated = response.getJSONObject("current").getString("last_updated");
        current.precip_mm = response.getJSONObject("current").getDouble("precip_mm");
        current.pressure_mb = response.getJSONObject("current").getDouble("pressure_mb");
        current.temp_c = response.getJSONObject("current").getDouble("temp_c");
        current.temp_f = response.getJSONObject("current").getDouble("temp_f");
        current.uv = response.getJSONObject("current").getDouble("uv");
        current.wind_dir = response.getJSONObject("current").getString("wind_dir");

        httpEntity.current = current;

        JSONArray temp = response.getJSONObject("forecast").getJSONArray("forecastday");
        for (int i = 0; i < temp.length(); i++){
            ForecastDay forecastDay = new ForecastDay();
            Day day = new Day();
            Condition tempCondition = new Condition();

            JSONObject tmp = temp.getJSONObject(i);

            tempCondition.text = tmp.getJSONObject("day").getJSONObject("condition").getString("text");
            tempCondition.icon = tmp.getJSONObject("day").getJSONObject("condition").getString("icon");
            day.condition = tempCondition;

            day.daily_chance_of_rain = tmp.getJSONObject("day").getDouble("daily_chance_of_rain");
            day.mintemp_f = tmp.getJSONObject("day").getDouble("mintemp_f");
            day.avghumidity = tmp.getJSONObject("day").getDouble("avghumidity");
            day.avgtemp_c = tmp.getJSONObject("day").getDouble("avgtemp_c");
            day.avgtemp_f = tmp.getJSONObject("day").getDouble("avgtemp_f");
            day.daily_chance_of_snow = tmp.getJSONObject("day").getDouble("daily_chance_of_snow");
            day.daily_will_it_rain = tmp.getJSONObject("day").getDouble("daily_will_it_rain");
            day.daily_will_it_snow = tmp.getJSONObject("day").getDouble("daily_will_it_snow");
            day.maxtemp_c = tmp.getJSONObject("day").getDouble("maxtemp_c");
            day.maxtemp_f = tmp.getJSONObject("day").getDouble("maxtemp_f");
            day.maxwind_kph = tmp.getJSONObject("day").getDouble("maxwind_kph");
            day.maxwind_mph = tmp.getJSONObject("day").getDouble("maxwind_mph");
            day.mintemp_c = tmp.getJSONObject("day").getDouble("mintemp_c");
            day.totalprecip_mm = tmp.getJSONObject("day").getDouble("totalprecip_mm");
            day.uv = tmp.getJSONObject("day").getDouble("uv");

            forecastDay.day = day;
            forecastDay.date = tmp.getString("date");

            forecastDay.hour = new ArrayList<Hour>();

            JSONArray hourTmp = tmp.getJSONArray("hour");
            for (int l = 0; l < hourTmp.length(); l++){
                Hour hour = new Hour();
                Condition hourTmpCondition = new Condition();

                JSONObject hourInJson = hourTmp.getJSONObject(l);

                hourTmpCondition.text = hourInJson.getJSONObject("condition").getString("text");
                hourTmpCondition.icon = hourInJson.getJSONObject("condition").getString("icon");
                hour.condition = hourTmpCondition;

                hour.time = hourInJson.getString("time");
                hour.temp_c = hourInJson.getDouble("temp_c");
                hour.temp_f = hourInJson.getDouble("temp_f");
                hour.wind_mph = hourInJson.getDouble("wind_mph");
                hour.wind_kph = hourInJson.getDouble("wind_kph");
                hour.wind_degree = hourInJson.getDouble("wind_degree");
                hour.wind_dir = hourInJson.getString("wind_dir");
                hour.pressure_mb = hourInJson.getDouble("pressure_mb");
                hour.precip_mm = hourInJson.getDouble("precip_mm");
                hour.humidity = hourInJson.getDouble("humidity");
                hour.will_it_rain = hourInJson.getDouble("will_it_rain");
                hour.chance_of_rain = hourInJson.getDouble("chance_of_rain");
                hour.will_it_snow = hourInJson.getDouble("will_it_snow");
                hour.chance_of_snow = hourInJson.getDouble("chance_of_snow");
                hour.uv = hourInJson.getDouble("uv");

                forecastDay.hour.add(hour);
            }

            httpEntity.forecast.add(forecastDay);
        }

        return httpEntity;
    }
}
