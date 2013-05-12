package com.alexandru.tigaeru.android.mydictapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class MySimpleCursorAdapter extends SimpleCursorAdapter {

	public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to, 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// get reference to the row
		View view = super.getView(position, convertView, parent);
		// check for odd or even to set alternate colors to the row background
		if (position % 2 == 0) {
			view.setBackgroundColor(Color.rgb(25, 25, 25));
		} else {
			// view.setBackgroundColor(Color.rgb(255, 255, 255));
		}

		// Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.accelerate_decelerate_interpolator);
		// convertView.startAnimation(animation);

		return view;
	}

}
