package pe.kmh.fm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import pe.kmh.fm.prop.RootFile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

public class TextEditor extends SherlockActivity {

	String filepath;
	EditText e;
	int perm;
	boolean isRoot;

	static final int JOB_SAVED = 1;
	static final int JOB_NOT_SAVED = 0;

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Settings Import
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPrefs.edit();

		String appTheme = sharedPrefs.getString("AppTheme", "Light");
		if (appTheme.equals("Light")) setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		else if (appTheme.equals("Dark")) setTheme(R.style.Theme_Sherlock);
		else {
			appTheme = "Light";
			setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
			editor.putString("AppTheme", "Light");
			editor.commit();
		}

		setContentView(R.layout.texteditor);
		Intent intent = this.getIntent();
		filepath = intent.getStringExtra("filepath");
		perm = intent.getIntExtra("Perm", 777);
		isRoot = intent.getBooleanExtra("isRoot", false);
		e = (EditText) findViewById(R.id.TextEdit);
		getSupportActionBar().setTitle(R.string.TextEditor);

		new FileLoadTask().execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onFileSaveBtnPress(View v) {
		if (isRoot) {
			RootTools.remount(new RootFile(filepath).getParent(), "rw");
			Editable data = e.getText();
			String p = data.toString();

			File f = new File(Environment.getExternalStorageDirectory().toString() + "/temp/TMP.tmp");
			new File(Environment.getExternalStorageDirectory().toString() + "/temp/").mkdir();
			try {
				f.createNewFile();
			}
			catch (IOException e1) {
			}

			try {
				FileWriter fw = new FileWriter(f);
				fw.write(p);
				fw.close();

				RootTools.copyFile(f.getAbsolutePath(), filepath, false, true);
				String w = "busybox chmod " + perm + " " + filepath;

				try {
					Command cmd = new Command(0, w) {

						@Override
						public void output(int id, String line) {
						}
					};

					RootTools.getShell(true).add(cmd).waitForFinish();
				}
				catch (Exception e) {
				}
			}
			catch (IOException e1) {
			}
		}
		else {
			try {
				FileWriter fw = new FileWriter(new File(filepath));
				fw.write(e.getText().toString());
				fw.close();
			}
			catch (IOException e) {
			}

		}

		setResult(JOB_SAVED);
		finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		setResult(JOB_NOT_SAVED);
		finish();
	}

	private class FileLoadTask extends AsyncTask<Void, Void, Void> {

		StringBuilder outLines = new StringBuilder();
		ProgressDialog pdialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pdialog = ProgressDialog.show(TextEditor.this, getString(R.string.Opening), getString(R.string.Wait), true);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (isRoot) {
				try {
					String w = "busybox cat \"" + filepath + "\"";
					Command cmd = new Command(0, w) {

						@Override
						public void output(int id, String line) {
							if (line.indexOf("\n") > -1) {
								for (String s : line.split("\n"))
									output(id, s);
							}
							else if (!outLines.toString().equals("")) outLines.append("\n" + line);
							else outLines.append(line);
						}
					};

					RootTools.getShell(true).add(cmd).waitForFinish();
				}
				catch (Exception ex) {
					Log.e("Exception", ex.getMessage());
				}
			}
			else {
				try {
					@SuppressWarnings("resource")
					Scanner s = new Scanner(new FileInputStream(filepath)).useDelimiter("\n");
					if (!s.hasNext()) return null;
					while (true) {
						outLines.append(s.next());
						if (s.hasNext()) outLines.append("\n");
						else break;
					}
					
					s.close();
				}
				catch (FileNotFoundException e) {
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			e.setText(outLines.toString());
			pdialog.dismiss();
		}
	}
}