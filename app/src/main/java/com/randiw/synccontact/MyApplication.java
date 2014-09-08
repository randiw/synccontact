package com.randiw.synccontact;

import android.app.Application;
import android.content.Context;

/**
 * Created by randiwaranugraha on 9/8/14.
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getContext() {
        return MyApplication.context;
    }
}