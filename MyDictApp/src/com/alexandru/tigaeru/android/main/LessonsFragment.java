package com.alexandru.tigaeru.android.main;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.alexandru.tigaeru.android.mydictapp.R;

public class LessonsFragment extends ListFragment {
	private OnLessonSelectedListener mCallback;
	private ActionMode mActionMode;
	// store the lessons
	private List<Lesson> lessonsList = new ArrayList<Lesson>();
	// Adapter to mange the entries in the ListFragment
	private ArrayAdapter<Lesson> adapter;
	private int itemSelected;
	private int lessonSelected;
	private int nrOfLessons;
	private boolean isTablet = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layout = android.R.layout.simple_list_item_activated_1;
		// grrrr
		isTablet = ((MainActivity) getActivity()).isTablet();

		// Create an array adapter for the list view
		adapter = new ArrayAdapter<Lesson>(getActivity(), layout, lessonsList);

		setListAdapter(adapter);
		if (!isTablet) {
			loadLessons();
		}
	}

	public void loadLessons() {
		// ?!
		Lesson.resetNrOfLessons();

		// create the lessons
		for (int i = 0; i < nrOfLessons; i++) {
			addNewLesson();
		}
	}

	public void addNewLesson() {
		lessonsList.add(new Lesson());
		// notify the change to the adapter
		adapter.notifyDataSetChanged();
	}

	public boolean removeLastLesson() {
		if (Lesson.decrementNrOfLessons()) {
			lessonsList.remove(lessonsList.size() - 1);

			// notify the change to the adapter
			adapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();

		// When in two-pane layout, set the listview to highlight the selected list item
		// (We do this during onStart because at the point the listview is available.)
		if (getFragmentManager().findFragmentById(R.id.words_fragment) != null) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		} else {
		}

		ListView myListView = getListView();

		myListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (mActionMode != null) {
					return false;
				}

				// Start the CAB using the ActionMode.Callback defined above
				itemSelected = position + 1;
				mActionMode = getActivity().startActionMode(mActionModeCallback);
				view.setSelected(true);
				return true;
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented the callback interface.
		try {
			mCallback = (OnLessonSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnHeadlineSelectedListener");
		}

		// Bundle extras = getArguments();
		// int nrOfLessons = extras.getInt(MainActivity.NR_OF_LESSONS);
		// loadLessons(nrOfLessons);

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mCallback != null) {
			// Notify the parent activity of selected item
			mCallback.onLessonSelected(position);
			lessonSelected = position;
			// Set the item as checked to be highlighted when in two-pane layout
			getListView().setItemChecked(position, true);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallback = null;
	}

	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_menu_lessons, menu);
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
				Toast.makeText(getActivity(), "Lektion " + itemSelected + " wurde gelöscht!",
						Toast.LENGTH_SHORT).show();
				mode.finish(); // Action picked, so close the CAB
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
		}
	};

	public int getSelectedLesson() {
		return lessonSelected + 1;
	}

	public void setNrOfLessons(int nr) {
		nrOfLessons = nr;
	}

	// The container Activity must implement this interface so the fragment can deliver messages
	public interface OnLessonSelectedListener {
		/** Called by HeadlinesFragment when a list item is selected */
		public void onLessonSelected(int position);
	}

}