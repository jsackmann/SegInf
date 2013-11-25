package com.seginf.supersafeapp;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactsReader {
	private Context context;
	public ContactsReader(Context c){
		this.context = c;
	}
	public ArrayList<Contact> contacts() {
		ContentResolver contResv = this.context.getContentResolver();
		Cursor cursor = contResv.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
		ArrayList<Contact> allContacts = new ArrayList<Contact>();
		if (cursor.moveToFirst()) {
			do {
				String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = contResv.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);

					while (pCur.moveToNext()) {
						String contactName = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
						String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						allContacts.add(new Contact(contactName,contactNumber));
						break;
					}
					pCur.close();
				}

			} while (cursor.moveToNext());
		}
		return allContacts;
	}

}
