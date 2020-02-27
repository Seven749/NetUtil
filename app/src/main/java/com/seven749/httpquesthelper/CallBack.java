package com.seven749.httpquesthelper;

public interface CallBack {

    void onResponse(String response);

    void onFailed(Exception e);
}
