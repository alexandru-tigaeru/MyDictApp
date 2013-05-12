package com.alexandru.tigaeru.android.mydictapp.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.alexandru.tigaeru.android.mydictapp.BuildConfig;
import com.alexandru.tigaeru.android.mydictapp.main.Word;

public class DbHelper extends SQLiteOpenHelper {
	private Context mContext;

	public static final String DATABASE_NAME = "data";
	public static final String TABLE_NAME = "words_table";
	public static final String C_ID = "_id";
	public static final String NAME = "name";
	public static final String LESSON = "lesson";
	public static final String ENGLISH = "english";
	public static final String ROMANIAN = "romanian";
	public static final String ANTONYM = "antonym";
	public static final String SYNONYM = "synonym";
	public static final String FLEXION = "flexion";
	public static final String RELATED_TERMS = "related_terms";
	public static final String TYPE = "type";
	public static final String COMMENTS = "comments";
	public static final String FRENCH = "french";
	public static final int VERSION = 1;

	private static final String createDB = "create table if not exists " + TABLE_NAME + "(" + C_ID
			+ " integer primary key autoincrement, " + NAME + " text, " + LESSON + " text, " + SYNONYM
			+ " text, " + ROMANIAN + " text, " + ANTONYM + " text, " + ENGLISH + " text, " + FLEXION
			+ " text, " + RELATED_TERMS + " text, " + COMMENTS + " text, " + FRENCH + " text, " + TYPE
			+ " text); ";

	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(createDB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// upgrade tables
	}

	public boolean insertData(Word newWord) {
		ContentValues cv = new ContentValues();

		cv.put(DbHelper.NAME, newWord.getName());
		cv.put(DbHelper.LESSON, newWord.getLesson());
		cv.put(DbHelper.ENGLISH, newWord.getEnglish());
		cv.put(DbHelper.ROMANIAN, newWord.getRomanian());
		cv.put(DbHelper.ANTONYM, newWord.getAntonym());
		cv.put(DbHelper.SYNONYM, newWord.getSynonym());
		cv.put(DbHelper.FLEXION, newWord.getFlexion());
		cv.put(DbHelper.RELATED_TERMS, newWord.getRelatedTerms());
		cv.put(DbHelper.COMMENTS, newWord.getComments());
		cv.put(DbHelper.FRENCH, newWord.getFrench());
		cv.put(DbHelper.TYPE, newWord.getType().toString());

		// use ContentResolver
		if (BuildConfig.DEBUG) {
			Log.i("ContentResolver", "inserted!");
		}
		ContentResolver cr = mContext.getContentResolver();
		return (cr.insert(WordsContentProvider.WORDS_URI, cv) != null);
	}

	public int updateTranslation(Word newWord, int id) {
		ContentValues cv = new ContentValues();

		cv.put(DbHelper.NAME, newWord.getName());
		cv.put(DbHelper.LESSON, newWord.getLesson());
		cv.put(DbHelper.ENGLISH, newWord.getEnglish());
		cv.put(DbHelper.ROMANIAN, newWord.getRomanian());
		cv.put(DbHelper.ANTONYM, newWord.getAntonym());
		cv.put(DbHelper.SYNONYM, newWord.getSynonym());
		cv.put(DbHelper.FLEXION, newWord.getFlexion());
		cv.put(DbHelper.RELATED_TERMS, newWord.getRelatedTerms());
		cv.put(DbHelper.COMMENTS, newWord.getComments());
		cv.put(DbHelper.FRENCH, newWord.getFrench());
		cv.put(DbHelper.TYPE, newWord.getType().toString());

		// use ContentResolver
		if (BuildConfig.DEBUG) {
			Log.i("ContentResolver", "updated!");
		}
		ContentResolver cr = mContext.getContentResolver();
		String selectionClause = DbHelper.C_ID + "=" + id;
		return cr.update(WordsContentProvider.WORDS_URI, cv, selectionClause, null);
	}

	public boolean deleteWord(int id) {

		ContentResolver cr = mContext.getContentResolver();
		String where = "_id=\"" + id + "\"";
		cr.delete(WordsContentProvider.WORDS_URI, where, null);
		return true;
	}
}
