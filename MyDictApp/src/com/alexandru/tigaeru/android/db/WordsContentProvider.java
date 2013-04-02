package com.alexandru.tigaeru.android.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class WordsContentProvider extends ContentProvider {

	private static final String AUTH = "com.alexandru.tigaeru.android.db.WordsContentProvider";
	public static final Uri WORDS_URI = Uri.parse("content://" + AUTH + "/" + DbHelper.TABLE_NAME);
	final static int WORDS = 1;
	SQLiteDatabase db;
	DbHelper dbHelper;

	private static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTH, DbHelper.TABLE_NAME, WORDS);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rowsDeleted = 0;
		db = dbHelper.getWritableDatabase();

		if (uriMatcher.match(uri) == WORDS) {
			db.delete(DbHelper.TABLE_NAME, selection, selectionArgs);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		db.close();
		return rowsDeleted;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues cv) {
		db = dbHelper.getWritableDatabase();

		if (uriMatcher.match(uri) == WORDS) {
			db.insert(DbHelper.TABLE_NAME, null, cv);
		}

		db.close();

		// notity the ContentResolver that the data has been changed
		getContext().getContentResolver().notifyChange(uri, null);

		return uri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;
		db = dbHelper.getReadableDatabase();

		if (uriMatcher.match(uri) == WORDS) {
			cursor = db.query(DbHelper.TABLE_NAME, projection, selection, selectionArgs, null, null,
					sortOrder);
		}

		if (cursor != null) {
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rowsUpdated = 0;

		db = dbHelper.getWritableDatabase();

		if (uriMatcher.match(uri) == WORDS) {
			rowsUpdated = db.update(DbHelper.TABLE_NAME, values, selection, selectionArgs);
		}

		db.close();

		// notity the ContentResolver that the data has been changed
		getContext().getContentResolver().notifyChange(uri, null);

		return rowsUpdated;
	}
}
