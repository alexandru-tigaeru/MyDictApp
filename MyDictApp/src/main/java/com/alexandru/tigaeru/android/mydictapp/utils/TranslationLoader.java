package com.alexandru.tigaeru.android.mydictapp.utils;

import java.io.IOException;
import java.util.Set;

import org.xml.sax.SAXException;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.alexandru.tigaeru.android.mydictapp.db.DbHelper;

/**
 * 
 * @author Alexandru_Tigaeru
 *
 */
public class TranslationLoader extends AsyncTask<String, Void, String> {

	private EditText mField;
	// this is the word, that we are trying to translate
	private String toTranslate;
	private static final String GLOSBE_RO = "http://glosbe.com/gapi/translate?from=de&dest=ro&format=xml&phrase=%s&pretty=true";
	private static final String GLOSBE_EN = "http://glosbe.com/gapi/translate?from=de&dest=en&format=xml&phrase=%s&pretty=true";
	private static final String GLOSBE_FR = "http://glosbe.com/gapi/translate?from=de&dest=fr&format=xml&phrase=%s&pretty=true";
	private static final String WIKTIONARY = "http://en.wiktionary.org/w/api.php?format=xml&action=query&titles=%s&rvprop=content&prop=revisions";

	// private String daten;

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
			translation = translate(DbHelper.ANTONYM);
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

	// translate just in romanian, english, french and find related_terms and flexion
	private Set<String> loadTranslation(String translateTo) {
		Set<String> result = new MyLinkedHashSet<String>();
		String sUri = "";
		if (translateTo.equals(DbHelper.ROMANIAN)) {
			sUri = String.format(GLOSBE_RO, toTranslate);
		} else if (translateTo.equals(DbHelper.ENGLISH)) {
			sUri = String.format(GLOSBE_EN, toTranslate);
		} else if (translateTo.equals(DbHelper.FRENCH)) {
			sUri = String.format(GLOSBE_FR, toTranslate);
		} else {
			// related_terms and flexion and antonyms
			sUri = String.format(WIKTIONARY, toTranslate);
		}

		String daten = "";
		try {
			// Glosbe has broken encoding. use Appache HttpClient instead
			if (sUri.contains("glosbe.com")
					&& (toTranslate.contains("ö") || toTranslate.contains("ä") || toTranslate.contains("ü") || toTranslate
							.contains("ß"))) {

				Uri uri = Uri.parse(sUri);
				daten = NetworkUtils.loadText(uri);
			} else {
				daten = NetworkUtils.downloadUrl(sUri);
			}

		} catch (IOException e1) {
			Log.e("Da stimmt was nicht!", e1.toString());
			e1.printStackTrace();
		}

		TranslationXmlHandler contentHandler;

		if (translateTo.equals(DbHelper.FLEXION)) {
			contentHandler = TranslationXmlHandler.createAsDeclension(result, toTranslate, 1);
		} else if (translateTo.equals(DbHelper.RELATED_TERMS)) {
			contentHandler = TranslationXmlHandler.createAsRelated(result, toTranslate, 5);
		} else if (translateTo.equals(DbHelper.ANTONYM)) {
			contentHandler = TranslationXmlHandler.createAsAntonym(result, toTranslate, 3);
		} else {
			contentHandler = TranslationXmlHandler.createAsTranslation(result, toTranslate, 3);
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
		// TextView test = (TextView) findViewById(R.id.testTranslation);
		// test.setText(daten);
	}
}