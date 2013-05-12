package com.alexandru.tigaeru.android.mydictapp.dialogs;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.mydictapp.db.DbHelper;
import com.alexandru.tigaeru.android.mydictapp.db.WordsContentProvider;

public class SearchDialogActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	private EditText querryET;
	private ListView list;
	private TextView empty;
	private SimpleCursorAdapter loaderAdapter;
	private String querry;
	private static final int LOADER_ID = 1;
	private LoaderManager lm;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.searchdialog_layout);
		querryET = ((EditText) findViewById(R.id.wort_suche));
		list = (ListView) findViewById(R.id.words_list);

		empty = (TextView) findViewById(R.id.emptyListElem);

		this.setFinishOnTouchOutside(false);

		String[] from = { DbHelper.LESSON, DbHelper.NAME, DbHelper.ROMANIAN, DbHelper.ANTONYM,
				DbHelper.ENGLISH, DbHelper.FLEXION, DbHelper.RELATED_TERMS };
		int[] to = { R.id.list_lesson, R.id.list_name, R.id.list_rom, R.id.list_ant, R.id.list_engl,
				R.id.list_flex, R.id.list_related };

		loaderAdapter = new SimpleCursorAdapter(this, R.layout.list_row_search, null, from, to, 0);

		list.setAdapter(loaderAdapter);
		list.setEmptyView(empty);
		// TODO: keine treffer appers all the time
		empty.setText("Keine Treffer!");
		lm = getLoaderManager();
	}

	public void abbrechen(View v) {
		this.finish();
	}

	public void suchen(View v) {
		querry = querryET.getText().toString();

		if (querry.equals("")) {
			Toast.makeText(this, "Leere Eingabe! Das kann nicht klappen...", Toast.LENGTH_SHORT).show();
		}

		lm.destroyLoader(LOADER_ID);
		lm.initLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] columns = { DbHelper.C_ID, DbHelper.NAME, DbHelper.SYNONYM, DbHelper.LESSON,
				DbHelper.ROMANIAN, DbHelper.ANTONYM, DbHelper.ENGLISH, DbHelper.FLEXION,
				DbHelper.RELATED_TERMS };
		// String select = DbHelper.NAME + "=\"" + querry + "\"";
		String sql = " upper(" + DbHelper.NAME + ") like upper(\"%" + querry + "%\") OR upper("
				+ DbHelper.ROMANIAN + ") like upper(\"%" + querry + "%\") OR upper(" + DbHelper.ENGLISH
				+ ") like upper(\"%" + querry + "%\") OR upper(" + DbHelper.FLEXION + ") like upper(\"%"
				+ querry + "%\") OR upper(" + DbHelper.COMMENTS + ") like upper(\"%" + querry
				+ "%\") OR upper(" + DbHelper.RELATED_TERMS + ") like upper(\"%" + querry + "%\")";

		CursorLoader cursorLoader = new CursorLoader(this, WordsContentProvider.WORDS_URI, columns, sql,
				null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		loaderAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		loaderAdapter.swapCursor(null);
	}
}
