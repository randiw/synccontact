package com.randiw.synccontact.model.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.randiw.synccontact.model.ContactStore;
import com.randiw.synccontact.model.repository.ContactRepository;
import com.randiw.synccontact.model.repository.item.Contact;
import com.randiw.synccontact.model.repository.provider.ContactProvider;
import com.randiw.synccontact.tool.RepoTools;
import com.randiw.synccontact.tool.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.Contacts;
import static android.provider.ContactsContract.Data;
import static android.provider.ContactsContract.RawContacts;

/**
 * Created by randiwaranugraha on 9/7/14.
 */
public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = ContactSyncAdapter.class.getSimpleName();

    private static final String SELECTION = (Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME) + "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1" + " AND " + Contacts.HAS_PHONE_NUMBER + "=1";

    private static final String SORT_ORDER = Utils.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;

    private static final String[] PROJECTION = {Contacts._ID, Contacts.LOOKUP_KEY,
            Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,
            Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI : Contacts._ID, SORT_ORDER};

    private Context context;
    private ContactStore contactStore;
    private ContactRepository contactRepository;
    private RequestQueue queue;

    public ContactSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        contactStore = ContactStore.getInstance();
        contactRepository = new ContactRepository(context);
        queue = Volley.newRequestQueue(context);
    }

    public ContactSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        this.context = context;
        contactStore = ContactStore.getInstance();
        contactRepository = new ContactRepository(context);
        queue = Volley.newRequestQueue(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        boolean isPartial = bundle.getBoolean("isPartial", false);
        if (isPartial) {
            partialSyncContact();
        } else {
            fullSyncContact();
        }
    }

    private void partialSyncContact() {
        ArrayList<String> ids = findDirty();
        if (ids != null) {
            updateData(ids);
            ArrayList<Contact> contacts = findUpdatedData();
            if (contacts != null && contacts.size() > 0) try {
                postContact(contacts);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<String> findDirty() {
        String projection[] = new String[]{RawContacts._ID};
        String selection = RawContacts.DIRTY + "=1";
        Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, projection, selection, null, null);
        if (!isRowAvailable(cursor)) {
            return null;
        }

        ArrayList<String> ids = new ArrayList<String>();

        do {
            long id = RepoTools.getLong(cursor, RawContacts._ID);
            ids.add(Long.toString(id));
        } while (cursor.moveToNext());

        cursor.close();
        return ids;
    }

    private void updateData(ArrayList<String> ids) {
        if (ids.size() == 0) {
            return;
        }

        String[] arrayId = (String[]) ids.toArray();
        String placeHolder = RepoTools.makePlaceHolders(arrayId.length);
        String[] projection = new String[]{Data.CONTACT_ID, Data.RAW_CONTACT_ID, Data.DISPLAY_NAME_PRIMARY, Data.PHOTO_THUMBNAIL_URI, Data.LOOKUP_KEY, Data.MIMETYPE, Data.DATA1, Data.DATA2, Data.DATA3};
        String selection = "(" + Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "' OR " + Data.MIMETYPE + " = '" + Email.CONTENT_ITEM_TYPE + "') AND " + Data.RAW_CONTACT_ID + " IN (" + placeHolder + ")";

        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, projection, selection, arrayId, null);
        if (!isRowAvailable(cursor)) {
            return;
        }

        do {
            long contactId = RepoTools.getLong(cursor, Data.CONTACT_ID);
            String displayName = RepoTools.getString(cursor, Data.DISPLAY_NAME_PRIMARY);
            String photoThumbUri = RepoTools.getString(cursor, Data.PHOTO_THUMBNAIL_URI);
            String lookupKey = RepoTools.getString(cursor, Data.LOOKUP_KEY);

            Contact contact = contactRepository.getContact(lookupKey);
            contact.setContactId(contactId);
            contact.setDisplayName(displayName);
            contact.setPhotoThumbUri(photoThumbUri);
            contact.setDirty(true);

            String mimeType = RepoTools.getString(cursor, Data.MIMETYPE);
            if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                String number = RepoTools.getString(cursor, Phone.NUMBER);
                int type = RepoTools.getInt(cursor, Phone.TYPE);
                String label = RepoTools.getString(cursor, Phone.LABEL);
                contact.addPhone(getLabel(label, type), number);
            } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                String address = RepoTools.getString(cursor, Email.ADDRESS);
                int type = RepoTools.getInt(cursor, Email.TYPE);
                String label = RepoTools.getString(cursor, Email.LABEL);
                contact.addEmail(getLabel(label, type), address);
            }

            contactRepository.save(contact);
        } while (cursor.moveToNext());

        cursor.close();
    }

    private ArrayList<Contact> findUpdatedData() {
        String selection = ContactRepository.DIRTY + "=1";
        Cursor cursor = context.getContentResolver().query(ContactProvider.CONTENT_URI, null, selection, null, null);
        if (!isRowAvailable(cursor)) {
            return null;
        }

        ArrayList<Contact> contacts = new ArrayList<Contact>();
        do {
            Contact contact = new Contact(cursor);
            contacts.add(contact);
        } while (cursor.moveToNext());

        cursor.close();
        return contacts;
    }

    private void fullSyncContact() {
        initContact();
        while (contactStore.hasNext()) {
            ArrayList<Contact> contacts = retrieveContactData(contactStore.getArrayOfId(), contactStore.getMapOfAddress());
            if (contacts != null && contacts.size() > 0) {
                contactRepository.saveAll((Contact[]) contacts.toArray());
                try {
                    postContact(contacts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initContact() {
        contactStore.clear();
        Cursor cursor = context.getContentResolver().query(Contacts.CONTENT_URI, PROJECTION, SELECTION, null, SORT_ORDER);
        if (!isRowAvailable(cursor)) {
            return;
        }

        do {
            long contactId = RepoTools.getLong(cursor, Contacts._ID);
            String lookupKey = RepoTools.getString(cursor, Contacts.LOOKUP_KEY);
            String displayName = RepoTools.getString(cursor, Contacts.DISPLAY_NAME_PRIMARY);
            String photoThumbUri = RepoTools.getString(cursor, Contacts.PHOTO_THUMBNAIL_URI);

            Contact contact = new Contact();
            contact.setContactId(contactId);
            contact.setLookupKey(lookupKey);
            contact.setDisplayName(displayName);
            contact.setPhotoThumbUri(photoThumbUri);

            contactStore.addContact(Long.toString(contactId), contact);
        } while (cursor.moveToNext());

        cursor.close();
    }

    private ArrayList<Contact> retrieveContactData(String[] arrayOfId, HashMap<String, Contact> mapOfContact) {
        if (arrayOfId.length == 0) {
            return null;
        }

        String placeHolder = RepoTools.makePlaceHolders(arrayOfId.length);
        String[] projection = new String[]{Data.CONTACT_ID, Data.MIMETYPE, Data.DATA1, Data.DATA2, Data.DATA3};
        String selection = "(" + Data.MIMETYPE + " = '" + Phone.CONTENT_ITEM_TYPE + "' OR " + Data.MIMETYPE + " = '" + Email.CONTENT_ITEM_TYPE + "') AND " + Data.CONTACT_ID + " IN (" + placeHolder + ")";

        Cursor cursor = context.getContentResolver().query(Data.CONTENT_URI, projection, selection, arrayOfId, null);
        if (!isRowAvailable(cursor)) {
            return null;
        }

        do {
            long contactId = RepoTools.getLong(cursor, Data.CONTACT_ID);
            String mimeType = RepoTools.getString(cursor, Data.MIMETYPE);
            Contact contact = mapOfContact.get(Long.toString(contactId));
            if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                String number = RepoTools.getString(cursor, Phone.NUMBER);
                int type = RepoTools.getInt(cursor, Phone.TYPE);
                String label = RepoTools.getString(cursor, Phone.LABEL);
                contact.addPhone(getLabel(label, type), number);
            } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                String address = RepoTools.getString(cursor, Email.ADDRESS);
                int type = RepoTools.getInt(cursor, Email.TYPE);
                String label = RepoTools.getString(cursor, Email.LABEL);
                contact.addEmail(getLabel(label, type), address);
            }
        } while (cursor.moveToNext());

        cursor.close();

        ArrayList<Contact> contacts = new ArrayList<Contact>();
        for (String key : mapOfContact.keySet()) {
            Contact contact = mapOfContact.get(key);
            contacts.add(contact);
        }

        return contacts;
    }

    private void postContact(ArrayList<Contact> contacts) throws JSONException {
        String url = "";
        JSONObject jsonRequest = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Contact contact : contacts) {
            jsonArray.put(contact.toJSONObject());
        }
        jsonRequest.put("body", jsonArray);
        JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.POST, url, jsonRequest, postResponse, errorResponse);
        queue.add(request);
    }

    private Response.Listener<JSONObject> postResponse = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

        }
    };

    private Response.ErrorListener errorResponse = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }
    };

    private String getLabel(String label, int type) {
        if (label == null) switch (type) {
            case Email.TYPE_HOME:
                label = "home";
                break;

            case Email.TYPE_WORK:
                label = "work";
                break;

            case Email.TYPE_MOBILE:
                label = "mobile";
                break;

            default:
                label = "other";
                break;
        }
        return label;
    }

    private boolean isRowAvailable(Cursor cursor) {
        boolean isRowAvailable = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isRowAvailable = true;
            } else {
                cursor.close();
            }
        }

        return isRowAvailable;
    }
}