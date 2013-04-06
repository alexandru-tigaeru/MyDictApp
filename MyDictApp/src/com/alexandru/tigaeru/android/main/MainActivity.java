package com.alexandru.tigaeru.android.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alexandru.tigaeru.android.db.DbHelper;
import com.alexandru.tigaeru.android.dialogs.AboutDialogActivity;
import com.alexandru.tigaeru.android.dialogs.AddWordDialogActivity;
import com.alexandru.tigaeru.android.dialogs.NewLessonNameDialogActivity;
import com.alexandru.tigaeru.android.dialogs.SearchDialogActivity;
import com.alexandru.tigaeru.android.mydictapp.R;

public class MainActivity extends Activity implements LessonsFragment.OnLessonSelectedListener {

	private Context appContext;
	private LessonsFragment lessonsFragment;
	private WordsFragment wordsFrag;
	private int currentSelection = 0;
	private String currentTitleName = "";
	public static final int REQUEST_CODE = 100;
	public static final String NR_OF_LESSONS = "nr_of_lessons";
	public static final String TITLE = "title";
	private int nrOfLessons;
	public static final String PREFS_NAME = "MyPrefsFile";
	private SharedPreferences prefs;
	private boolean isTablet = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// disable landscape
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// allow network access on the gui thread
		// StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		// StrictMode.setThreadPolicy(policy);

		appContext = getApplicationContext();

		// load nr of lessons
		loadPreferences();

		setContentView(R.layout.main_layout);
		isTablet = findViewById(R.id.fragment_container) == null;

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first fragment
		if (!isTablet) {
			// PHONE
			// However, if we're being restored from a previous state, then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of LessonsFragment
			lessonsFragment = new LessonsFragment();
//			Bundle myBundle = new Bundle();
//			myBundle.putInt(NR_OF_LESSONS, nrOfLessons);
//			lessonsFragment.setArguments(myBundle);

			// Add the fragment to the 'fragment_container' FrameLayout
			getFragmentManager().beginTransaction().add(R.id.fragment_container, lessonsFragment).commit();
			lessonsFragment.setNrOfLessons(nrOfLessons);
		} else {
			// TABLET
			lessonsFragment = (LessonsFragment) getFragmentManager().findFragmentById(R.id.lessons_fragment);

			lessonsFragment.setNrOfLessons(nrOfLessons);
			lessonsFragment.loadLessons();
		}
	}

	public void onLessonSelected(int position) {
		// The user selected a lesson from the LessonsFragment

		// Capture the words fragment from the activity layout
		wordsFrag = (WordsFragment) getFragmentManager().findFragmentById(R.id.words_fragment);

		currentSelection = position;

		if (isTablet && wordsFrag != null) {
			// TABLET
			wordsFrag.updateLessonView(position, getTitle(currentSelection));

		} else {
			// PHONE
			// If the frag is not available, we're in the one-pane layout and must swap frags...
			// Create fragment and give it an argument for the selected lesson
			wordsFrag = new WordsFragment();
			Bundle args = new Bundle();
			args.putInt(WordsFragment.ARG_POSITION, position);
			args.putString(WordsFragment.ARG_TITLE, getTitle(currentSelection));
			wordsFrag.setArguments(args);
			FragmentTransaction transaction = getFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			transaction.replace(R.id.fragment_container, wordsFrag);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_search:
			searchWord();
			return true;
		case R.id.menuitem_addLesson:
			addNewLesson();
			return true;
		case R.id.menuitem_addWord:
			addWord(lessonsFragment.getSelectedLesson());
			return true;
		case R.id.menuitem_removeLesson:
			removeLastLesson();
			return true;
		case R.id.menuitem_feedback:
			Toast.makeText(appContext, "feedback", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.menuitem_saveDB:
			if (saveDB())
				return true;
		case R.id.menuitem_about:
			about();
			return true;
		case R.id.menuitem_quit:
			quit();
			return true;
		}
		return false;
	}

	private boolean saveDB() {
		DbHelper dbHelper = new DbHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String pathToDB = db.getPath();
		db.close();
		String backupFile = "";

		try {
			InputStream input = new FileInputStream(pathToDB);
			File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/MyDictApp");
			dir.mkdir();

			Calendar calendar = Calendar.getInstance();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int date = calendar.get(Calendar.DATE);
			int hours = calendar.get(Calendar.HOUR_OF_DAY);
			int minutes = calendar.get(Calendar.MINUTE);
			int seconds = calendar.get(Calendar.SECOND);
			String timeStamp = year + "." + (month + 1) + "." + date + "_" + hours + "-" + minutes + "-"
					+ seconds;

			// Path to the external backup
			backupFile = Environment.getExternalStorageDirectory().getPath() + "/Download/MyDictApp/db_"
					+ timeStamp;
			OutputStream output = new FileOutputStream(backupFile);

			// transfer bytes from the Input File to the Output File
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			input.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Toast.makeText(appContext, backupFile + " gesichert!", Toast.LENGTH_LONG).show();

		return true;
	}

	private void about() {
		Intent dialogAbout = new Intent(this, AboutDialogActivity.class);
		startActivity(dialogAbout);
	}

	private void removeLastLesson() {
		if (lessonsFragment.removeLastLesson()) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("lessons", Lesson.getNumberOfLessons());
			editor.remove(Lesson.getNumberOfLessons() + "");
			editor.apply();
			nrOfLessons--;
		} else {
			Toast.makeText(appContext, "da gibt's nichts mehr zu löschen...", Toast.LENGTH_SHORT).show();
		}
	}

	private void addWord(int lesson) {
		Intent dialogAddWord = new Intent(this, AddWordDialogActivity.class);
		// lesson nr übergeben
		dialogAddWord.putExtra(WordsFragment.ARG_POSITION, lesson);
		startActivity(dialogAddWord);
	}

	private void searchWord() {
		Intent dialogSearchWord = new Intent(this, SearchDialogActivity.class);
		startActivity(dialogSearchWord);
	}

	private void quit() {
		// TODO make a dialog and ask for confirmation
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		// this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void onLessonTitleClicked(View v) {
		Intent nameLesson = new Intent(this, NewLessonNameDialogActivity.class);
		nameLesson.putExtra(TITLE, wordsFrag.getTitle());
		startActivityForResult(nameLesson, REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			if (data.hasExtra(NewLessonNameDialogActivity.NEWNAME)) {
				currentTitleName = data.getExtras().getString(NewLessonNameDialogActivity.NEWNAME);
				wordsFrag.updateLessonView(currentSelection, currentTitleName);

				SharedPreferences.Editor editor = prefs.edit();
				editor.remove(currentSelection + "");
				editor.putString(currentSelection + "", currentTitleName);
				editor.apply();
			}
		}
	}

	private void loadPreferences() {
		prefs = getSharedPreferences(PREFS_NAME, 0);
		// get nr of lessons
		nrOfLessons = prefs.getInt("lessons", 1);
	}

	private String getTitle(int position) {
		return prefs.getString(position + "", "");
	}

	private void addNewLesson() {
		lessonsFragment.addNewLesson();

		// save the new number of lessons
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("lessons", Lesson.getNumberOfLessons());
		editor.apply();
		nrOfLessons++;
	}

	public boolean isTablet() {
		return isTablet;
	}
}