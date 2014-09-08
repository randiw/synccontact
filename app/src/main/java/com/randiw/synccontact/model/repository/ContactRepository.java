package com.randiw.synccontact.model.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.randiw.synccontact.model.repository.item.Contact;

import static com.randiw.synccontact.model.repository.provider.ContactProvider.CONTENT_URI;

/**
 * Created by randiwaranugraha on 9/7/14.
 */
public class ContactRepository extends Repository {

    public static String TABLE_NAME = "contact";

    public static final String ID = "_id";
    public static final String CONTACT_ID = "contact_id";
    public static final String LOOKUP_KEY = "lookup_key";
    public static final String DISPLAY_NAME = "display_name";
    public static final String PHOTO_THUMB_URI = "photo_thumb_uri";
    public static final String DIRTY = "dirty";
    public static final String PHONES = "phones";
    public static final String EMAILS = "emails";

    public ContactRepository(Context context) {
        super(context);
    }

    public void save(Contact contact) {
        if(isExist(contact)){
            String selection = LOOKUP_KEY + "='" + contact.getLookupKey() + "'";
            resolver.update(CONTENT_URI, contact.getContentValues(), selection, null);
        } else {
            resolver.insert(CONTENT_URI, contact.getContentValues());
        }
    }

    public void saveAll(Contact[] contacts) {
        if(contacts == null || contacts.length <= 0){
            return;
        }

        ContentValues[] contentValueses = new ContentValues[contacts.length];
        for(int i = 0; i < contacts.length; i++) {
            contentValueses[i] = contacts[i].getContentValues();
        }

        resolver.bulkInsert(CONTENT_URI, contentValueses);
    }

    public Contact getContact(String lookupKey) {
        String selection = lookupKey + "='" + lookupKey + "'";
        Cursor cursor = resolver.query(CONTENT_URI, null, null, null, null);
        if(cursor == null || !cursor.moveToFirst()) {
            return null;
        }

        Contact contact = new Contact(cursor);
        cursor.close();
        return contact;
    }

    public boolean isExist(Contact contact) {
        boolean isExist = false;

        String selection = LOOKUP_KEY + "='" + contact.getLookupKey() + "'";
        Cursor cursor = resolver.query(CONTENT_URI, null, selection, null, null);
        if(cursor != null && cursor.moveToFirst()){
            isExist = true;
        }

        cursor.close();
        return isExist;
    }

    public static String createTable() {
        StringBuilder query = new StringBuilder();
        query.append(createTableIfNotExists(TABLE_NAME));
        query.append(primaryAutoID(ID));

        query.append(fieldInteger(CONTACT_ID));
        query.append(fieldVarchar(LOOKUP_KEY));
        query.append(fieldVarchar(DISPLAY_NAME));
        query.append(fieldVarchar(PHOTO_THUMB_URI));
        query.append(fieldVarchar(PHONES));
        query.append(fieldVarchar(EMAILS));
        query.append(fieldInteger(DIRTY));

        query.append(close());
        return query.toString();
    }

    public static String dropTable() {
        String query = "DROP TABLE IF EXISTS" + TABLE_NAME;
        return query;
    }
}