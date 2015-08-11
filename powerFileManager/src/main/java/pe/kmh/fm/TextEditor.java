package pe.kmh.fm;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import pe.kmh.fm.prop.RootFile;
import pe.kmh.fm.util.FileUtil;

public class TextEditor extends ActionBarActivity {

	static final int JOB_SAVED = 1;
	static final int JOB_NOT_SAVED = 0;
	String filepath;
	EditText e;
	int perm;
	boolean isRoot;

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Settings Import
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPrefs.edit();

		String appTheme = sharedPrefs.getString("AppTheme", "Light");
		if (appTheme.equals("Light")) setTheme(R.style.Theme_AppCompat_Light);
		else if (appTheme.equals("Dark")) setTheme(R.style.Theme_AppCompat);
		else {
			setTheme(R.style.Theme_AppCompat_Light);
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
		try {
			RootFile to;
			FileOutputStream fos;
			if (isRoot) {
				RootTools.remount(new RootFile(getFilesDir().getAbsolutePath()).getParent(), "rw");
				RootTools.remount(new RootFile(filepath).getParent(), "rw");
				to = new RootFile(getFilesDir().getAbsolutePath() + "/" + new File(filepath).getName());
				fos = getApplicationContext().openFileOutput(to.getName(), Context.MODE_PRIVATE);
			} else {
				to = new RootFile(filepath);
				fos = new FileOutputStream(to);
			}

			fos.write(e.getText().toString().getBytes());
			fos.close();

			if (isRoot) {
				RootTools.remount(new RootFile(filepath).getParent(), "rw");
				RootFile from = new RootFile(filepath);
				from.delete();
				RootFile fromDir = new RootFile(from.getParent());
				FileUtil.RootFileCopy(to, fromDir);
				String w = "busybox chmod " + perm + " " + filepath;

				try {
					Command cmd = new Command(0, w) {

						@Override
						public void commandOutput(int id, String line) {

						}

						@Override
						public void commandTerminated(int i, String s) {

						}

						@Override
						public void commandCompleted(int i, int i2) {

						}
					};

					RootTools.getShell(true).add(cmd);
					FileUtil.waitForFinish(cmd);
				} catch (Exception e) {
				}
			}
		} catch (IOException e) {
			Log.e("PFM", e.getMessage());
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
			File to = new File(new File(filepath).getParent());
			if (isRoot) {
				to = getFilesDir();
				RootTools.remount(new RootFile(filepath).getParent(), "rw");
				FileUtil.RootFileCopy(new RootFile(filepath), new RootFile(to));
			}

			try {
				@SuppressWarnings("resource")
				Scanner s = new Scanner(new FileInputStream(to.getAbsolutePath() + "/" + new File(filepath).getName())).useDelimiter("\n");
				if (!s.hasNext()) return null;
				while (true) {
					outLines.append(s.next());
					if (s.hasNext()) outLines.append("\n");
					else break;
				}

				s.close();

				new RootFile(to.getAbsolutePath() + "/" + new File(filepath).getName()).delete();
			} catch (FileNotFoundException e) {
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