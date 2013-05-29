package com.alexandru.tigaeru.android.mydictapp.dialogs;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.mydictapp.db.DbHelper;
import com.alexandru.tigaeru.android.mydictapp.db.WordsContentProvider;
import com.alexandru.tigaeru.android.mydictapp.main.Word;
import com.alexandru.tigaeru.android.mydictapp.main.WordsFragment;

/**
 * 
 * @author Alexandru_Tigaeru
 *
 */
public class EditWordDialogActivity extends Activity {
	private int currentWordID;
	private TextView wordET;
	private EditText englishET;
	private EditText romanianET;
	private EditText antonymET;
	private EditText flexionET;
	private EditText relatedTermsET;
	private EditText commentsET;
	private EditText frenchET;
	private int lesson;

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.editworddialog_layout);
		Bundle extras = getIntent().getExtras();
		currentWordID = extras.getInt(WordsFragment.SELECTED_WORD);
		wordET = (TextView) findViewById(R.id.word_to_edit);
		englishET = ((EditText) findViewById(R.id.uebersetzung_engl));
		romanianET = ((EditText) findViewById(R.id.uebersetzung_rom));
		antonymET = ((EditText) findViewById(R.id.uebersetzung_antonyme));
		flexionET = ((EditText) findViewById(R.id.uebersetzung_flexion));
		relatedTermsET = ((EditText) findViewById(R.id.uebersetzung_verwandt));
		commentsET = ((EditText) findViewById(R.id.uebersetzung_anmerkungen));
		frenchET = ((EditText) findViewById(R.id.uebersetzung_fr));
		searchTranslation(currentWordID);

		// wordET.setText(word);

		this.setFinishOnTouchOutside(false);
		super.onCreate(arg0);
	}

	public void abbrechen(View v) {
		this.finish();
	}

	private void searchTranslation(int id) {

		String[] columns = { DbHelper.C_ID, DbHelper.LESSON, DbHelper.NAME, DbHelper.SYNONYM, DbHelper.LESSON,
				DbHelper.ROMANIAN, DbHelper.ANTONYM, DbHelper.ENGLISH, DbHelper.FLEXION,
				DbHelper.RELATED_TERMS, DbHelper.COMMENTS, DbHelper.FRENCH };
		Cursor mCursor = getContentResolver().query(WordsContentProvider.WORDS_URI, columns,
				DbHelper.C_ID + "=\"" + id + "\"", null, null);

		if (mCursor != null) {
			while (mCursor.moveToNext()) {

				wordET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.NAME)));
				englishET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.ENGLISH)));
				romanianET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.ROMANIAN)));
				antonymET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.ANTONYM)));
				flexionET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.FLEXION)));
				relatedTermsET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.RELATED_TERMS)));
				commentsET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.COMMENTS)));
				frenchET.setText(mCursor.getString(mCursor.getColumnIndex(DbHelper.FRENCH)));
				lesson = mCursor.getInt(mCursor.getColumnIndex(DbHelper.LESSON));
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public void inDBApspeichern(View v) {
		DbHelper dbHelper = new DbHelper(this);

		String name = wordET.getText().toString();
		String english = englishET.getText().toString();
		String romanian = romanianET.getText().toString();
		String antonym = antonymET.getText().toString();
		String flexion = flexionET.getText().toString();
		String relatedTerms = relatedTermsET.getText().toString();
		String comments = commentsET.getText().toString();
		String french = frenchET.getText().toString();

		Word newWord = new Word(lesson, name, english, romanian, antonym, "", flexion, relatedTerms,
				comments, french, Word.Type.UNKNOWN);

		dbHelper.updateTranslation(newWord, currentWordID);
		
		this.finish();
	}
}
