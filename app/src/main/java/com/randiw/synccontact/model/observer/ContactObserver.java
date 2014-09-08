package com.randiw.synccontact.model.observer;

import android.accounts.Account;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.randiw.synccontact.model.AccountGeneral;
import com.randiw.synccontact.model.repository.provider.ContactProvider;

/**
 * Created by randiwaranugraha on 9/8/14.
 */
public class ContactObserver extends ContentObserver {

    public static final String TAG = ContactObserver.class.getSimpleName();

    public ContactObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d(TAG, "uri: " + uri);

        Account account = AccountGeneral.getActiveAccount();
        if (account != null) {
            Bundle settings = new Bundle();
            settings.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settings.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            settings.putBoolean("isPartial", true);
            ContentResolver.requestSync(account, ContactProvider.AUTHORITY, settings);
        }
    }
}