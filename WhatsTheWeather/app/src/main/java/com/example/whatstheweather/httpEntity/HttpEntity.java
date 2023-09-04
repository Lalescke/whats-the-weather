package com.example.whatstheweather.httpEntity;

import java.util.ArrayList;
import java.util.List;

public class HttpEntity {
    public HttpEntity(){}

    public MLocation location;
    public Current current;
    public ArrayList<ForecastDay> forecast;
}
