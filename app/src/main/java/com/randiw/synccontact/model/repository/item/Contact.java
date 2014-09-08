package com.randiw.synccontact.model.repository.item;

import android.content.ContentValues;
import android.database.Cursor;

import com.randiw.synccontact.model.repository.ContactRepository;
import com.randiw.synccontact.tool.RepoTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by randiwaranugraha on 9/7/14.
 */
public class Contact {

    private long contact_id;
    private String lookupKey;
    private String displayName;
    private String photoThumbUri;
    private boolean dirty;
    private HashMap<String, String> emails;
    private HashMap<String, String> phones;

    public Contact() {

    }

    public Contact(Cursor cursor) {
        long contactId = RepoTools.getLong(cursor, ContactRepository.CONTACT_ID);
        setContactId(contactId);

        String lookupKey = RepoTools.getString(cursor, ContactRepository.LOOKUP_KEY);
        setLookupKey(lookupKey);

        String displayName = RepoTools.getString(cursor, ContactRepository.DISPLAY_NAME);
        setDisplayName(displayName);

        String photoThumbUri = RepoTools.getString(cursor, ContactRepository.PHOTO_THUMB_URI);
        setPhotoThumbUri(photoThumbUri);

        int dirty = RepoTools.getInt(cursor, ContactRepository.DIRTY);
        setDirty(dirty);

        String emails = RepoTools.getString(cursor, ContactRepository.EMAILS);
        setEmails(emails);

        String phones = RepoTools.getString(cursor, ContactRepository.PHONES);
        setPhones(phones);
    }

    public void setContactId(long contact_id) {
        this.contact_id = contact_id;
    }

    public long getContactId() {
        return contact_id;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }

    public String getLookupKey() {
        return lookupKey;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setPhotoThumbUri(String photoThumbUri) {
        this.photoThumbUri = photoThumbUri;
    }

    public String getPhotoThumbUri() {
        return photoThumbUri;
    }

    public boolean isDirty() {
        return dirty;
    }

    public int getDirty() {
        return dirty ? 1 : 0;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void setDirty(int dirty) {
        this.dirty = dirty > 0;
    }

    public void addEmail(String label, String email) {
        if (emails == null) {
            emails = new HashMap<String, String>();
        }
        if (emails.containsValue(email)) {
            String oldLabel = getLabel(emails, email);
            emails.remove(oldLabel);
        }
        emails.put(label, email);
    }

    public void addPhone(String label, String number) {
        if (phones == null) {
            phones = new HashMap<String, String>();
        }
        if(phones.containsValue(number)) {
            String oldLabel = getLabel(phones, number);
            phones.remove(oldLabel);
        }
        phones.put(label, number);
    }

    public String emailsToString() {
        JSONArray jsonArray = emailsToJSONArray();
        if(jsonArray == null) {
            return null;
        }
        return jsonArray.toString();
    }

    private JSONArray emailsToJSONArray() {
        if (emails == null || emails.size() == 0) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();

        try {
            Set<String> keys = emails.keySet();
            for (String label : keys) {
                String email = emails.get(label);
                JSONObject object = new JSONObject();
                object.put("label", label);
                object.put("email", email);
                jsonArray.put(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonArray;
    }

    public void setEmails(String emailsString) {
        if (emails == null) {
            emails = new HashMap<String, String>();
        }

        if (emailsString != null && emailsString.length() > 0) try {
            JSONArray jsonArray = new JSONArray(emailsString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                String label = object.getString("label");
                String email = object.getString("email");
                addEmail(label, email);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String phonesToString() {
        JSONArray jsonArray = phonesToJSONArray();
        if(jsonArray == null) {
            return null;
        }
        return jsonArray.toString();
    }

    private JSONArray phonesToJSONArray() {
        if (phones == null || phones.size() == 0) {
            return null;
        }

        JSONArray jsonArray = new JSONArray();

        try {
            Set<String> keys = phones.keySet();
            for (String label : keys) {
                String number = phones.get(label);
                JSONObject object = new JSONObject();
                object.put("label", label);
                object.put("number", number);
                jsonArray.put(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonArray;
    }

    public void setPhones(String phonesString) {
        if (emails == null) {
            emails = new HashMap<String, String>();
        }

        if (phonesString != null && phonesString.length() > 0) try {
            JSONArray jsonArray = new JSONArray(phonesString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                String label = object.getString("label");
                String phone = object.getString("number");
                addPhone(label, phone);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactRepository.CONTACT_ID, getContactId());
        contentValues.put(ContactRepository.LOOKUP_KEY, getLookupKey());
        contentValues.put(ContactRepository.DISPLAY_NAME, getDisplayName());
        contentValues.put(ContactRepository.PHOTO_THUMB_URI, getPhotoThumbUri());
        contentValues.put(ContactRepository.DIRTY, getDirty());
        contentValues.put(ContactRepository.EMAILS, emailsToString());
        contentValues.put(ContactRepository.PHONES, phonesToString());

        return contentValues;
    }

    private String getLabel(HashMap<String, String> dataMap, String value) {
        if (dataMap != null && dataMap.size() > 0) {
            for (String label : dataMap.keySet()) {
                String dataValue = dataMap.get(label);
                if (value.equals(dataValue)) {
                    return label;
                }
            }
        }

        return null;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("contact_id", getContactId());
        jsonObject.put("lookup_key", getLookupKey());
        jsonObject.put("display_name", getDisplayName());
        jsonObject.put("photo_thumb_uri", getPhotoThumbUri());
        jsonObject.put("dirty", getDirty());
        jsonObject.put("phones", phonesToJSONArray());
        jsonObject.put("emails", emailsToJSONArray());
        return jsonObject;
    }
}