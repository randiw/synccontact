package com.randiw.synccontact.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.randiw.synccontact.model.observer.ContactObserver;

import static android.provider.ContactsContract.Contacts;

public class ContactMonitorService extends Service {

    public static final String TAG = ContactMonitorService.class.getSimpleName();

    private ContactObserver contactObserver;

    @Override
    public void onCreate() {
        super.onCreate();
        contactObserver = new ContactObserver(new Handler());
        getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, contactObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(contactObserver);
        super.onDestroy();
    }
}