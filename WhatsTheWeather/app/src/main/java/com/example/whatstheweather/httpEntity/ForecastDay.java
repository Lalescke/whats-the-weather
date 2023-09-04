package com.example.whatstheweather.httpEntity;

import org.json.JSONObject;

import java.util.ArrayList;

public class ForecastDay {
    public ForecastDay(){}

    public String date;
    public Day day;
    public ArrayList<Hour> hour;
}
