package com.alexandru.tigaeru.android.mydictapp.main;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;
import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.mydictapp.db.DbHelper;

public class PopupActivity extends Activity {
	private int selectedWord;
	private SQLiteDatabase db;
	private DbHelper dbHelper;
	private SimpleCursorAdapter adapter;
	private ListView mListView;

	@Override
	protected void onCreate(Bundle arg0) {

		setContentView(R.layout.popup_layout);

		Bundle extras = getIntent().getExtras();
		selectedWord = extras.getInt(WordsFragment.SELECTED_WORD);

		if (db == null) {
			dbHelper = new DbHelper(this);
			db = dbHelper.getWritableDatabase();
		}

		String[] from = { DbHelper.NAME, DbHelper.ROMANIAN, DbHelper.ANTONYM, DbHelper.ENGLISH,
				DbHelper.FLEXION, DbHelper.RELATED_TERMS, DbHelper.COMMENTS, DbHelper.FRENCH };
		String[] columns = { DbHelper.C_ID, DbHelper.NAME, DbHelper.SYNONYM, DbHelper.LESSON,
				DbHelper.ROMANIAN, DbHelper.ANTONYM, DbHelper.ENGLISH, DbHelper.FLEXION,
				DbHelper.RELATED_TERMS, DbHelper.COMMENTS, DbHelper.FRENCH };
		Cursor caseSensitiveCursor = db.query(DbHelper.TABLE_NAME, columns, DbHelper.C_ID + "=\""
				+ selectedWord + "\"", null, null, null, null);
		// Cursor caseInsensitiveCursor = db.rawQuery(sql, null);

		int[] to = { R.id.list_row_name, R.id.list_row_rom, R.id.list_row_ant, R.id.list_row_engl,
				R.id.list_row_flex, R.id.list_row_related, R.id.list_row_comments, R.id.list_row_fr };

		adapter = new SimpleCursorAdapter(this, R.layout.list_row_popup, caseSensitiveCursor, from, to);

		mListView = (ListView) findViewById(R.id.popup_list);
		mListView.setAdapter(adapter);

		super.onCreate(arg0);
	}

	@Override
	protected void onStop() {
		db.close();
		super.onStop();
	}

	public void close(View v) {
		this.finish();
	}
}
