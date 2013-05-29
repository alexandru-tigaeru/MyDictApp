package com.alexandru.tigaeru.android.mydictapp.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.alexandru.tigaeru.android.mydictapp.R;
import com.alexandru.tigaeru.android.mydictapp.main.MainActivity;

/**
 * 
 * @author Alexandru_Tigaeru
 *
 */
public class NewLessonNameDialogActivity extends Activity {
	private String lessonName;
	public static final String NEWNAME = "newName";
	private String curTitle;
	private boolean cancel = false;

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.newlessonnamedialog_layout);

		Bundle extras = getIntent().getExtras();
		curTitle = extras.getString(MainActivity.TITLE);
		TextView title = (TextView) findViewById(R.id.newName_Text);
		title.setText(curTitle);

	    this.setFinishOnTouchOutside(false);

		super.onCreate(arg0);
	}

	public void close(View v) {
		this.cancel = true;
		this.finish();
	}

	public void saveNewName(View v) {
		this.lessonName = ((EditText) findViewById(R.id.newName_Text)).getText().toString();
		this.finish();
	}

	public void finish() {
		Intent data = new Intent();
		data.putExtra(NEWNAME, lessonName);
		if (cancel) {
			setResult(RESULT_CANCELED, data);
		} else {
			setResult(RESULT_OK, data);
		}
		super.finish();
	}
}
