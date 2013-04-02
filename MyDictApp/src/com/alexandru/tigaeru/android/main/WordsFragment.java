package com.alexandru.tigaeru.android.main;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alexandru.tigaeru.android.db.DbHelper;
import com.alexandru.tigaeru.android.db.WordsContentProvider;
import com.alexandru.tigaeru.android.dialogs.EditWordDialogActivity;
import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.utils.MySimpleCursorAdapter;

public class WordsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String ARG_POSITION = "position";
	public static final String ARG_TITLE = "title";
	public static final String SELECTED_WORD = "selectedWord";
	private int mCurrentPosition = -1;
	private DbHelper dbHelper;
	private View view;
	private ListView list;
	private TextView title;
	private TextView extraTitle;
	private SimpleCursorAdapter adapter;
	private ActionMode mActionMode;
	private int longSelected;
	// retain list state when returning to it
	private static final String LIST_STATE = "listState";
	private Parcelable mListState = null;
	private int selectedLesson;
	private int currentLoaderId;
	private LoaderManager lm;
	private Loader<Cursor> currentLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// If activity recreated (such as from screen rotate), restore
		// the previous lesson selection set by onSaveInstanceState().
		// This is primarily necessary when in the two-pane layout.
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
			updateLessonView(mCurrentPosition, savedInstanceState.getString(ARG_TITLE));
		}

		// Inflate the layout for this fragment
		view = inflater.inflate(R.layout.wordsfragment_layout, container, false);
		list = (ListView) view.findViewById(R.id.wordsfragment_list);
		title = (TextView) view.findViewById(R.id.title);
		extraTitle = (TextView) view.findViewById(R.id.extra_title);
		lm = getLoaderManager();
		
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		//PHONE UI
		// During startup, check if there are arguments passed to the fragment. onStart is a good place to do this because
		// the layout has already been applied to the fragment at this point so we can safely call the
		// method below that sets the lesson text.
		Bundle args = getArguments();
		if (args != null) {
			// Set article based on argument passed in
			updateLessonView(args.getInt(ARG_POSITION), args.getString(ARG_TITLE, "Kein Titel"));
		} 
//			else if (mCurrentPosition != -1) {
//			// Set article based on saved instance state defined during onCreateView
//			updateLessonView(mCurrentPosition, mCurrentTitleName);
//		}

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {

				if (mActionMode != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined above
				mActionMode = getActivity().startActionMode(mActionModeCallback);
				view.setSelected(true);
				longSelected = (int) id;
				return true;
			}
		});

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
				// Toast.makeText(getActivity(), "Item with id " + id+" clicked!", Toast.LENGTH_SHORT).show();
				Intent popup = new Intent(getActivity(), PopupActivity.class);
				popup.putExtra(WordsFragment.SELECTED_WORD, (int) id);
				startActivity(popup);
			}
		});
	}

	public void updateLessonView(int position, String name) {
		// add 1 because of offset
		selectedLesson = position + 1;
		if (name == null || name.equals("")) {
			title.setText("Lektion " + selectedLesson);
			extraTitle.setText("");
		} else {
			title.setText("Lektion " + selectedLesson + " - ");
			extraTitle.setText(name);
		}



		String[] from = { DbHelper.NAME, DbHelper.ROMANIAN, DbHelper.FLEXION };
		int[] to = { R.id.list_name, R.id.list_rom, R.id.list_flex };

		adapter = new MySimpleCursorAdapter(getActivity(), R.layout.list_row_words, null, from, to);
		list.setAdapter(adapter);

		//create a new loader with a new lesson
		if(currentLoader!= null && currentLoader.getId()!=selectedLesson){
			lm.destroyLoader(currentLoaderId);
		}
		
		currentLoader= lm.initLoader(selectedLesson, null, this);
		currentLoaderId = selectedLesson;
		
		mCurrentPosition = position;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the current article selection in case we need to recreate the fragment
		outState.putInt(ARG_POSITION, mCurrentPosition);

		// save list position
		mListState = list.onSaveInstanceState();
		outState.putParcelable(LIST_STATE, mListState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// restore list position
		if (mListState != null) {
			mListState = savedInstanceState.getParcelable(LIST_STATE);
		}
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onResume() {
		// restore list position
		if (mListState != null)
			list.onRestoreInstanceState(mListState);
		mListState = null;
		super.onResume();
	}

	// longclick the items
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_menu_words, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.context_delete:
				deleteWord(longSelected);
				mode.finish(); // Action picked, so close the CAB
				return true;
			case R.id.context_edit:
				editWord(longSelected);
				mode.finish(); // Action picked, so close the CAB
				return true;
			default:
				return false;
			}
		}

		private void editWord(int longSelected) {
			Intent editIntent = new Intent(getActivity(), EditWordDialogActivity.class);
			editIntent.putExtra(WordsFragment.SELECTED_WORD, longSelected);
			startActivity(editIntent);
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};

	private void deleteWord(int id) {
		if (dbHelper == null) {
			dbHelper = new DbHelper(getActivity());
		}
		dbHelper.deleteWord(id);
		Toast.makeText(getActivity(), "Das Wort wurde gelöscht!", Toast.LENGTH_SHORT).show();
	}

	public String getTitle() {
		return extraTitle.getText().toString();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] columns = { DbHelper.C_ID, DbHelper.NAME, DbHelper.SYNONYM, DbHelper.LESSON,
				DbHelper.ROMANIAN, DbHelper.ANTONYM, DbHelper.ENGLISH, DbHelper.FLEXION,
				DbHelper.RELATED_TERMS };
		String sql = DbHelper.LESSON + "=\"" + selectedLesson + "\"";

		CursorLoader cursorLoader = new CursorLoader(getActivity(), WordsContentProvider.WORDS_URI, columns,
				sql, null, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}