package com.alexandru.tigaeru.android.mydictapp.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.alexandru.tigaeru.android.mydictapp.R;

public class AboutDialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle arg0) {
		setContentView(R.layout.aboutdialog_layout);
		
	    this.setFinishOnTouchOutside(false);
		super.onCreate(arg0);
	}
	
	public void close(View v){
		this.finish();
	}
}
