package com.randiw.synccontact.model;

import com.randiw.synccontact.model.repository.item.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class ContactStore {

	public static final String TAG = ContactStore.class.getSimpleName();
	private static final int LIMIT = 200;

	public static ContactStore instance = null;

	private ArrayList<String> contacts_id;
	private HashMap<String, Contact> tempAddress;
	private Vector<StoreHolder> stores;

	private ContactStore() {
		stores = new Vector<StoreHolder>();
	}

	public static ContactStore getInstance() {
		if (instance == null) {
			instance = new ContactStore();
		}
		return instance;
	}

    public void clear() {
        stores.clear();
        if(contacts_id != null) {
            contacts_id.clear();
        }
        if(tempAddress != null) {
            tempAddress.clear();
        }
    }

	public void addContact(String contact_id, Contact contact) {
		if (stores.size() == 0) {
			stores.add(new StoreHolder());
		}

		StoreHolder storeHolder = stores.get(0);
		if (storeHolder.getCount() == LIMIT) {
			StoreHolder newHolder = new StoreHolder();
			newHolder.addContact(contact_id, contact);
			stores.add(0, newHolder);
		} else {
			storeHolder.addContact(contact_id, contact);
		}
	}

	public int size(){
		return stores.size();
	}
	
	public boolean hasNext() {
		if (stores.size() > 0) {
			StoreHolder storeHolder = stores.remove(0);
			contacts_id = storeHolder.getContactsId();
			tempAddress = storeHolder.getTempAddress();
			return true;
		}
		return false;
	}

	public String[] getArrayOfId() {
		if (contacts_id == null) {
			contacts_id = stores.get(0).getContactsId();
		}
		String[] newStringArr = new String[contacts_id.size()];
		newStringArr = contacts_id.toArray(newStringArr);
		return newStringArr;
	}

	public HashMap<String, Contact> getMapOfAddress() {
		if (tempAddress == null) {
			tempAddress = stores.get(0).getTempAddress();
		}
		return tempAddress;
	}

	private class StoreHolder {

		private ArrayList<String> contacts_id;
		private HashMap<String, Contact> tempAddress;

		public StoreHolder() {
			contacts_id = new ArrayList<String>();
			tempAddress = new HashMap<String, Contact>();
		}

		public ArrayList<String> getContactsId() {
			return contacts_id;
		}

		public HashMap<String, Contact> getTempAddress() {
			return tempAddress;
		}

		public void addContact(String contact_id, Contact user) {
			contacts_id.add(contact_id);
			tempAddress.put(contact_id, user);
		}

		public int getCount() {
			return contacts_id.size();
		}
	}
}