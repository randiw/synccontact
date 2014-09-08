package com.randiw.synccontact.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.randiw.synccontact.model.adapter.ContactSyncAdapter;

public class ContactSyncAdapterService extends Service {

    public static final String TAG = ContactSyncAdapterService.class.getSimpleName();

    private ContactSyncAdapter syncAdapter;
    private static final Object syncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new ContactSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}