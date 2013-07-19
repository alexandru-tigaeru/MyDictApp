package com.alexandru.tigaeru.android.mydictapp.main;

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

import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.mydictapp.db.DbHelper;
import com.alexandru.tigaeru.android.mydictapp.dialogs.AboutDialogActivity;
import com.alexandru.tigaeru.android.mydictapp.dialogs.AddWordDialogActivity;
import com.alexandru.tigaeru.android.mydictapp.dialogs.NewLessonNameDialogActivity;
import com.alexandru.tigaeru.android.mydictapp.dialogs.SearchDialogActivity;
import com.alexandru.tigaeru.android.mydictapp.utils.Backup;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

/**
 * Entrypoint in the app. Activity displaying the lesson numbers and the currently selected lesson words.
 * 
 * @author Alexandru_Tigaeru
 *
 */
public class MainActivity extends Activity implements LessonsFragment.OnLessonSelectedListener {

	private Context appContext;
	private LessonsFragment lessonsFragment;
	private WordsFragment wordsFrag;
	private int currentSelection = 0;
	private String currentTitleName = "";
	public static final int REQUEST_CODE = 100;
	private static final int REQUEST_LOAD = 101;
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
		
		//TODO: find a better way to differentiate between tablet/handset
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
			// Bundle myBundle = new Bundle();
			// myBundle.putInt(NR_OF_LESSONS, nrOfLessons);
			// lessonsFragment.setArguments(myBundle);

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

	
	////////////////////////////////////////////////
	//				ActionBar
	////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
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
		case R.id.menuitem_backup:
			backup();
			return true;
		case R.id.menuitem_restore:
			restore();
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

	private void backup() {
		DbHelper dbHelper = new DbHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String backupFile = Backup.saveDB(db, "");

		Toast.makeText(appContext, backupFile + " gesichert!", Toast.LENGTH_LONG).show();
	}

	private void restore() {

		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath()
				+ "/Download/MyDictApp");

		// can user select directories or not
		intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
		intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
		startActivityForResult(intent, REQUEST_LOAD);
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
			// all lessons were deleted
			Toast.makeText(appContext, getResources().getString(R.string.app_no_more_lessons), Toast.LENGTH_SHORT).show();
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
		// handle the answer from backup file selection dialog
		if (resultCode == RESULT_OK && requestCode == REQUEST_LOAD) {
			String backupFile = data.getStringExtra(FileDialog.RESULT_PATH);

			DbHelper dbHelper = new DbHelper(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			Backup.restoreDB(db, backupFile);

			// restart activity
			Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(
					getBaseContext().getPackageName());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			
			Toast.makeText(appContext, backupFile + " wurde geladen!", Toast.LENGTH_LONG).show();
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