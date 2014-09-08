package com.randiw.synccontact.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.randiw.synccontact.model.StubAuthenticator;

public class AuthenticatorService extends Service {

    private StubAuthenticator stubAuthenticator;

    @Override
    public void onCreate() {
        stubAuthenticator = new StubAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stubAuthenticator.getIBinder();
    }
}