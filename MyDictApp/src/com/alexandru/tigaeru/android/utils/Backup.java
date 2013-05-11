package com.alexandru.tigaeru.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class Backup {

	public static String saveDB(SQLiteDatabase db, String backupFile) {
		String pathToDB = db.getPath();
		db.close();
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

			if (backupFile == null || backupFile.equals("")) {
				// Path to the external backup
				backupFile = Environment.getExternalStorageDirectory().getPath() + "/Download/MyDictApp/db_"
						+ timeStamp;
			} else {
				// backupFile = Environment.getExternalStorageDirectory().getPath() + "/Download/MyDictApp/"
				// + backupFile;
			}

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
		return backupFile;
	}

	public static void restoreDB(SQLiteDatabase db, String backupFile) {
		String pathToDB = db.getPath();
		db.close();

		try {
			InputStream input = new FileInputStream(backupFile);
			OutputStream output = new FileOutputStream(pathToDB);

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
	}
}
