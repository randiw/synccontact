package com.randiw.synccontact.model.repository.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.randiw.synccontact.model.repository.ContactRepository;
import com.randiw.synccontact.model.repository.DatabaseHelper;

/**
 * Created by randiwaranugraha on 9/7/14.
 */
public class ContactProvider extends ContentProvider {

    public static final String TAG = ContactProvider.class.getSimpleName();

    private DatabaseHelper databaseHelper;

    public static final String AUTHORITY = "com.randiw.synccontact.model.repository.provider.ContactProvider";
    private static final String TABLE_NAME = ContactRepository.TABLE_NAME;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    public static final int CONTACT = 0;
    public static final int CONTACT_ID = 1;

    private static final UriMatcher sURI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURI_MATCHER.addURI(AUTHORITY, TABLE_NAME, CONTACT);
        sURI_MATCHER.addURI(AUTHORITY, TABLE_NAME + "/#", CONTACT_ID);
    }

    public ContactProvider() {
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_NAME);

        Cursor cursor;

        int uriType = sURI_MATCHER.match(uri);
        switch (uriType) {
            case CONTACT_ID:
                queryBuilder.appendWhere(ContactRepository.ID + "=" + uri.getLastPathSegment());
                break;

            case CONTACT:
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }

        cursor = queryBuilder.query(databaseHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int uriType = sURI_MATCHER.match(uri);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        long id;
        switch (uriType) {
            case CONTACT:
                id = database.insert(TABLE_NAME, null, contentValues);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(TABLE_NAME + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURI_MATCHER.match(uri);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsDeleted;

        switch (uriType) {
            case CONTACT:
                rowsDeleted = database.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case CONTACT_ID:
                String id = uri.getLastPathSegment();
                if (selection == null || selection.length() == 0) {
                    rowsDeleted = database.delete(TABLE_NAME, ContactRepository.ID + "=" + id, null);
                } else {
                    rowsDeleted = database.delete(TABLE_NAME, ContactRepository.ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int uriType = sURI_MATCHER.match(uri);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int rowsUpdated;

        switch (uriType) {
            case CONTACT:
                rowsUpdated = database.update(TABLE_NAME, contentValues, selection, selectionArgs);
                break;

            case CONTACT_ID:
                String id = uri.getLastPathSegment();
                if (selection == null || selection.length() == 0) {
                    rowsUpdated = database.update(TABLE_NAME, contentValues, ContactRepository.ID + "=" + id, null);
                } else {
                    rowsUpdated = database.update(TABLE_NAME, contentValues, ContactRepository.ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}