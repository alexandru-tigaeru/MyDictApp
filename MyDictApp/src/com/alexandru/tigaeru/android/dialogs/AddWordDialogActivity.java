package com.alexandru.tigaeru.android.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alexandru.tigaeru.android.db.DbHelper;
import com.alexandru.tigaeru.android.main.Word;
import com.alexandru.tigaeru.android.main.WordsFragment;
import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.utils.NetworkUtils;
import com.alexandru.tigaeru.android.utils.TranslationLoader;

public class AddWordDialogActivity extends Activity {
	private int currentLesson;
	private EditText nameET;
	private EditText englishET;
	private EditText romanianET;
	private EditText antonymET;
	private EditText flexionET;
	private EditText relatedTermsET;
	private EditText commentsET;
	private EditText frenchET;
	Context mContext;

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.addworddialog_layout);
		Bundle extras = getIntent().getExtras();
		currentLesson = extras.getInt(WordsFragment.ARG_POSITION);
		TextView title = (TextView) findViewById(R.id.textViewAddWord);
		title.setText("Neues Wort zur Lektion " + currentLesson + " hinzufügen");

		mContext = this;
		this.setFinishOnTouchOutside(false);
		super.onCreate(arg0);
	}

	public void cancel(View v) {
		this.finish();
	}

	public void searchTranslation(View v) {
		// if no network tell the user the online search won't work
		if (NetworkUtils.isNetworkAvailable(this)) {
			// searchSync();
			searchAsync();
		} else {
			Intent dialogNoNetwork = new Intent(this, NoNetworkDialogActivity.class);
			startActivity(dialogNoNetwork);
		}
	}

	private void searchAsync() {
		TranslationLoader englishLoader = new TranslationLoader(englishET);
		englishLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameET.getText().toString(),
				DbHelper.ENGLISH);

		TranslationLoader romanianLoader = new TranslationLoader(romanianET);
		romanianLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameET.getText().toString(),
				DbHelper.ROMANIAN);

		TranslationLoader antonymLoader = new TranslationLoader(antonymET);
		antonymLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameET.getText().toString(),
				DbHelper.ANTONYM);

		TranslationLoader relatedLoader = new TranslationLoader(relatedTermsET);
		relatedLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameET.getText().toString(),
				DbHelper.RELATED_TERMS);

		TranslationLoader flexionLoader = new TranslationLoader(flexionET);
		flexionLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameET.getText().toString(),
				DbHelper.FLEXION);

		TranslationLoader frLoader = new TranslationLoader(frenchET);
		frLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nameET.getText().toString(),
				DbHelper.FRENCH);

	}

	// private void searchSync() {
	//
	// TranslationLoader englishLoader = new TranslationLoader(englishET);
	// englishLoader.execute(nameET.getText().toString(), DbHelper.ENGLISH);
	//
	// TranslationLoader romanianLoader = new TranslationLoader(romanianET);
	// romanianLoader.execute(nameET.getText().toString(), DbHelper.ROMANIAN);
	//
	// TranslationLoader antonymLoader = new TranslationLoader(antonymET);
	// antonymLoader.execute(nameET.getText().toString(), DbHelper.ANTONYM);
	//
	// TranslationLoader relatedLoader = new TranslationLoader(relatedTermsET);
	// relatedLoader.execute(nameET.getText().toString(), DbHelper.RELATED_TERMS);
	//
	// TranslationLoader flexionLoader = new TranslationLoader(flexionET);
	// flexionLoader.execute(nameET.getText().toString(), DbHelper.FLEXION);
	// }

	@Override
	protected void onResume() {
		super.onResume();

		nameET = ((EditText) findViewById(R.id.wort_zu_uebersetzen));
		englishET = ((EditText) findViewById(R.id.uebersetzung_engl));
		romanianET = ((EditText) findViewById(R.id.uebersetzung_rom));
		antonymET = ((EditText) findViewById(R.id.uebersetzung_antonyme));
		flexionET = ((EditText) findViewById(R.id.uebersetzung_flexion));
		relatedTermsET = ((EditText) findViewById(R.id.uebersetzung_verwandt));
		commentsET = ((EditText) findViewById(R.id.uebersetzung_anmerkungen));
		frenchET = ((EditText) findViewById(R.id.uebersetzung_fr));
	}

	public void persisData(View v) {
		DbHelper dbHelper = new DbHelper(this);

		String name = nameET.getText().toString();
		String english = englishET.getText().toString();
		String romanian = romanianET.getText().toString();
		String antonym = antonymET.getText().toString();
		String flexion = flexionET.getText().toString();
		String relatedTerms = relatedTermsET.getText().toString();
		String comments = commentsET.getText().toString();
		String french = frenchET.getText().toString();

		Word newWord = new Word(currentLesson, name, english, romanian, antonym, "", flexion, relatedTerms,
				comments, french, Word.Type.UNKNOWN);

		boolean result = dbHelper.insertData(newWord);

		if (result) {
			Toast.makeText(this, "Wort erfolgreich eingetragen", Toast.LENGTH_SHORT).show();
		}
		this.finish();
	}
}
