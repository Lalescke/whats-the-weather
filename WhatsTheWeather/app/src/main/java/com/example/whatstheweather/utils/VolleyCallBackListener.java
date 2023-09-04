package com.example.whatstheweather.utils;

import com.example.whatstheweather.httpEntity.HttpEntity;

public interface VolleyCallBackListener {
    void onSuccessResponse(HttpEntity result);
    void onErrorListener(String error);
}
