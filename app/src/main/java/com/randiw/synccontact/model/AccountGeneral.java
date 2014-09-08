package com.randiw.synccontact.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.randiw.synccontact.MyApplication;

import static android.content.SharedPreferences.Editor;

/**
 * Created by randiwaranugraha on 9/8/14.
 */
public class AccountGeneral {

    public static final String TAG = AccountGeneral.class.getSimpleName();
    public static final String ACCOUNT_TYPE = "com.randiw.synccontact";

    private static Account activeAccount;

    public static void addAccount(String username, String password) {
        Account account = new Account(username, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager) MyApplication.getContext().getSystemService(Context.ACCOUNT_SERVICE);
        if(accountManager.addAccountExplicitly(account, password, null)){
            activeAccount = account;

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
            Editor editor = sharedPreferences.edit();
            editor.putString("username", username);
            editor.putString("password", password);
            editor.apply();
        }
    }

    public static Account getActiveAccount() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        String username = sharedPreferences.getString("username", null);
        if(username == null || username.length() == 0) {
            return null;
        }

        AccountManager accountManager = (AccountManager) MyApplication.getContext().getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        for(Account account : accounts) {
            if(activeAccount != null && activeAccount.name.equals(account.name)) {
                return account;
            }
            if(username.equals(account.name)) {
                activeAccount = account;
                return account;
            }
        }

        return activeAccount;
    }
}