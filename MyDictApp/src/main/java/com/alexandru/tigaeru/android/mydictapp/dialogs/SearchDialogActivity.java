package com.alexandru.tigaeru.android.mydictapp.dialogs;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.TextView;
import android.widget.Toast;

import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.mydictapp.db.DbHelper;
import com.alexandru.tigaeru.android.mydictapp.db.WordsContentProvider;

/**
 * 
 * @author Alexandru_Tigaeru
 * 
 */
public class SearchDialogActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	private AutoCompleteTextView querryEditText;
	private ListView mlist;
	private TextView emptyTextView;
	private SimpleCursorAdapter mAdapter;
	private SimpleCursorAdapter mAdapter_autoComp;
	private String querry;
	private static final int LOADER_ID = 1;
	private LoaderManager lm;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.searchdialog_layout);
		querryEditText = ((AutoCompleteTextView) findViewById(R.id.wort_suche));
		mlist = (ListView) findViewById(R.id.words_list);
		emptyTextView = (TextView) findViewById(R.id.emptyListElem);

		this.setFinishOnTouchOutside(false);

		String[] from = { DbHelper.LESSON, DbHelper.NAME, DbHelper.ROMANIAN, DbHelper.ANTONYM,
				DbHelper.ENGLISH, DbHelper.FLEXION, DbHelper.RELATED_TERMS };
		int[] to = { R.id.list_lesson, R.id.list_name, R.id.list_rom, R.id.list_ant, R.id.list_engl,
				R.id.list_flex, R.id.list_related };

		mAdapter = new SimpleCursorAdapter(this, R.layout.list_row_search, null, from, to, 0);

		mlist.setAdapter(mAdapter);
		mlist.setEmptyView(emptyTextView);
		// TODO: keine treffer appers all the time
		emptyTextView.setText("Keine Treffer!");
		lm = getLoaderManager();

		// AutoCompletion adapter
		int[] to_AC = new int[] { android.R.id.text1 };
		String[] from_AC = new String[] { DbHelper.NAME };
		mAdapter_autoComp = new SimpleCursorAdapter(this, android.R.layout.simple_dropdown_item_1line, null,
				from_AC, to_AC, 0);
		// Set the query filter provider used to filter the current Cursor.
		mAdapter_autoComp.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				String selection = '%' + constraint.toString() + '%';
				String sql = DbHelper.NAME + " LIKE ?";
				return getContentResolver().query(WordsContentProvider.WORDS_URI,
						new String[] { DbHelper.C_ID, DbHelper.NAME }, sql, new String[] { selection }, null);
			}
		});

		mAdapter_autoComp.setCursorToStringConverter(new CursorToStringConverter() {

			@Override
			public CharSequence convertToString(Cursor c) {

				return c.getString(c.getColumnIndexOrThrow(DbHelper.NAME));

			}
		});

		querryEditText.setAdapter(mAdapter_autoComp);

	}

	public void cancel(View v) {
		this.finish();
	}

	public void search(View v) {
		querry = querryEditText.getText().toString();

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

		String sql = " upper(" + DbHelper.NAME + ") like upper(\"%" + querry + "%\") OR upper("
				+ DbHelper.ROMANIAN + ") like upper(\"%" + querry + "%\") OR upper(" + DbHelper.ENGLISH
				+ ") like upper(\"%" + querry + "%\") OR upper(" + DbHelper.FLEXION + ") like upper(\"%"
				+ querry + "%\") OR upper(" + DbHelper.COMMENTS + ") like upper(\"%" + querry
				+ "%\") OR upper(" + DbHelper.RELATED_TERMS + ") like upper(\"%" + querry + "%\")";
		return new CursorLoader(this, WordsContentProvider.WORDS_URI, columns, sql, null, null);

	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}
