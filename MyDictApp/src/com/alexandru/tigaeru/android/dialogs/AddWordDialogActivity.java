package com.alexandru.tigaeru.android.dialogs;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.http.StatusLine;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.alexandru.tigaeru.android.utils.MyLinkedHashSet;
import com.alexandru.tigaeru.android.utils.NetworkUtils;

import de.androidpraxis.utilities.NetworkUtility;
import de.androidpraxis.utilities.NetworkUtilityMessageHandler;

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

	public void abbrechen(View v) {
		this.finish();
	}

	public void uebersetzungSuchen(View v) {
		// if no network tell the user the online search won't work
		if(NetworkUtils.isNetworkAvailable(this)){
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

	private void searchSync() {

		TranslationLoader englishLoader = new TranslationLoader(englishET);
		englishLoader.execute(nameET.getText().toString(), DbHelper.ENGLISH);

		TranslationLoader romanianLoader = new TranslationLoader(romanianET);
		romanianLoader.execute(nameET.getText().toString(), DbHelper.ROMANIAN);

		TranslationLoader antonymLoader = new TranslationLoader(antonymET);
		antonymLoader.execute(nameET.getText().toString(), DbHelper.ANTONYM);

		TranslationLoader relatedLoader = new TranslationLoader(relatedTermsET);
		relatedLoader.execute(nameET.getText().toString(), DbHelper.RELATED_TERMS);

		TranslationLoader flexionLoader = new TranslationLoader(flexionET);
		flexionLoader.execute(nameET.getText().toString(), DbHelper.FLEXION);
	}

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

	public void inDBApspeichern(View v) {
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

	private class TranslationLoader extends AsyncTask<String, Void, String> {

		private EditText mField;
		// this is the word, that we are looking for
		private String toTranslate;
		private static final String GLOSBE_RO = "http://glosbe.com/gapi/translate?from=de&dest=ro&format=xml&phrase=%s&pretty=true";
		private static final String GLOSBE_EN = "http://glosbe.com/gapi/translate?from=de&dest=en&format=xml&phrase=%s&pretty=true";
		private static final String GLOSBE_DE = "http://glosbe.com/gapi/translate?from=de&dest=de&format=xml&phrase=%s&pretty=true";
		private static final String GLOSBE_FR = "http://glosbe.com/gapi/translate?from=de&dest=fr&format=xml&phrase=%s&pretty=true";
		private static final String WIKTIONARY = "http://en.wiktionary.org/w/api.php?format=xml&action=query&titles=%s&rvprop=content&prop=revisions";

		final NetworkUtilityMessageHandler handler = new NetworkUtilityMessageHandler() {

			@Override
			public void onException(final Throwable exception) {

				((Activity) mContext).runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(mContext,
								exception.getLocalizedMessage() + " " + exception.toString(),
								Toast.LENGTH_LONG).show();
					}
				});
			}

			@Override
			public void onError(final StatusLine statusLine) {

				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(mContext, statusLine.getReasonPhrase(), Toast.LENGTH_LONG).show();
					}
				});
			}
		};

		public TranslationLoader(EditText editText) {
			mField = editText;
		}

		@Override
		protected String doInBackground(String... params) {
			toTranslate = params[0];
			String translation = "";

			if (params[1].equals(DbHelper.ENGLISH)) {
				translation = translate(DbHelper.ENGLISH);
			} else if (params[1].equals(DbHelper.ROMANIAN)) {
				translation = translate(DbHelper.ROMANIAN);
			} else if (params[1].equals(DbHelper.ANTONYM)) {
				translation = "";
			} else if (params[1].equals(DbHelper.FLEXION)) {
				translation = translate(DbHelper.FLEXION);
			} else if (params[1].equals(DbHelper.RELATED_TERMS)) {
				translation = translate(DbHelper.RELATED_TERMS);
			} else if (params[1].equals(DbHelper.FRENCH)) {
				translation = translate(DbHelper.FRENCH);
			}

			return translation;
		}

		private String translate(String translateTo) {
			String translation = "notFound";
			Set<String> myResult = loadTranslation(translateTo);

			translation = myResult.toString();

			return translation;

		}

		// translate just in romanian,english and related_terms for the moment
		private Set<String> loadTranslation(String translateTo) {
			Set<String> result = new MyLinkedHashSet<String>();
			String sUri = "";
			if (translateTo.equals(DbHelper.ROMANIAN)) {
				sUri = String.format(GLOSBE_RO, toTranslate);
			} else if (translateTo.equals(DbHelper.ENGLISH)) {
				sUri = String.format(GLOSBE_EN, toTranslate);
			} else if (translateTo.equals(DbHelper.RELATED_TERMS)) {
				sUri = String.format(GLOSBE_DE, toTranslate);
			} else if (translateTo.equals(DbHelper.FRENCH)) {
				sUri = String.format(GLOSBE_FR, toTranslate);
			} else {
				sUri = String.format(WIKTIONARY, toTranslate);
			}

			Uri uri = Uri.parse(sUri);
			NetworkUtility nwu = new NetworkUtility(handler);
			String daten = nwu.loadText(uri);

			TranslationXmlHandler contentHandler;

			if (translateTo.equals(DbHelper.FLEXION)) {
				contentHandler = new TranslationXmlHandler(result, toTranslate, 1);
			} else if (translateTo.equals(DbHelper.RELATED_TERMS)) {
				contentHandler = new TranslationXmlHandler(result, toTranslate, 7);
			} else {
				contentHandler = new TranslationXmlHandler(result, toTranslate, 3);
			}

			try {
				android.util.Xml.parse(daten, contentHandler);
			} catch (SAXException e) {
				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			mField.setText(result);
		}
	}

	public static class TranslationXmlHandler extends DefaultHandler {
		private Set<String> data;
		private String translation = "";
		private boolean isTranslation = false;
		private boolean isDeclension = false;
		private boolean isGerman = false;
		private String toTranslate;
		private int nrOfTranslations;

		public TranslationXmlHandler(Set<String> data, String toTranslate, int nrOfTranslations) {
			this.data = data;
			this.toTranslate = toTranslate;
			this.nrOfTranslations = nrOfTranslations;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			super.startElement(uri, localName, qName, attributes);

			if (localName.equals("text")) {
				isTranslation = true;
			} else {
				isTranslation = false;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);

			String currText = "";
			for (int i = start; i < start + length; i++) {
				currText += ch[i];
			}

			// Log.d("LINE out: ", curText);
			handleGlosbe(currText);
			handleWiki(currText);

		}

		private void handleWiki(String currText) {
			if (currText.equals("\n")) {
				// ignore
			} else {
				if (currText.trim().equals("==German==")) {
					isGerman = true;
				}
				if (isDeclension) {
					currText = currText.replace("{", "");
					currText = currText.replace("}", "");
					translation = currText.substring(currText.indexOf("|") + 1, currText.length());

					if (translation.contains("|s|")) {
						translation = translation.substring(0, translation.indexOf("|s|") + 2);
					}

					if (translation.contains("|h|")) {
						translation = translation.substring(0, translation.indexOf("|h|") + 2);
					}

					if (translation.contains("|hs|")) {
						translation = translation.substring(0, translation.indexOf("|hs|") + 3);
					}

					// translation = translation.replace("|", "-");

					removeGenitive();

					if (isGerman && data.size() < nrOfTranslations) {
						data.add(translation);
					}
				}

				if (currText.trim().equals("===Noun===") || currText.trim().equals("===Adjective===")
						|| currText.trim().equals("====Conjugation====")) {
					// next non emty line is the one we want
					isDeclension = true;
				} else {
					isDeclension = false;
				}
			}
		}

		private void handleGlosbe(String currText) {
			if (isTranslation) {

				if (currText.trim().equals("") || currText.trim().equals("\n")
						|| currText.trim().equals(toTranslate)) {
					// ignore empty and same word
				} else {
					translation = currText;
					if (data.size() < nrOfTranslations) {
						data.add(translation);
					}
				}
			}
		}

		private void removeGenitive() {
			List<String> tokens = new java.util.ArrayList<String>(Arrays.asList(translation.split(Pattern
					.quote("|"))));

			if (tokens != null && tokens.size() >= 2) {
				if (tokens.get(1).endsWith("s")) {
					tokens.remove(1);
				}
				if (tokens.size() >= 2 && tokens.get(1).startsWith("gen2")) {
					tokens.remove(1);
				}

				// remove unneeded dash after feminin
				if (tokens.size() >= 2 && tokens.get(0).equals("f")) {
					tokens.remove(1);
				}

				if (tokens.size() >= 2 && tokens.get(1).equals("")) {
					tokens.remove(1);
				}

				translation = tokens.toString();
				translation = translation.replace(", ", "-");
				translation = translation.replace("]", "");
				translation = translation.replace("[", "");
			}
		}
	}
}
