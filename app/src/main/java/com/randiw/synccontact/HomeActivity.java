package com.randiw.synccontact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.randiw.synccontact.model.AccountGeneral;
import com.randiw.synccontact.service.ContactMonitorService;


public class HomeActivity extends Activity {

    public static final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (AccountGeneral.getActiveAccount() == null) {
            String password = "randi123";
            String username = "randi.waranugraha@gmail.com";
            AccountGeneral.addAccount(username, password);
        }

        Intent contactMonitorIntent = new Intent(this, ContactMonitorService.class);
        startService(contactMonitorIntent);
    }
}