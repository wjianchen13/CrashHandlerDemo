package com.example.crashhandlerdemo;

import android.app.Application;

public class MyApplication extends Application {
    MyExceptionHandler handler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = MyExceptionHandler.getInstance();
        handler.init(getApplicationContext());
    }
}