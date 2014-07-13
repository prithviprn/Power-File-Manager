package pe.kmh.fm;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import pe.kmh.fm.prop.RootFile;
import pe.kmh.fm.util.FileUtil;

public class TextEditor extends SherlockActivity {

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
        if (appTheme.equals("Light")) setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        else if (appTheme.equals("Dark")) setTheme(R.style.Theme_Sherlock);
        else {
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
        RootFile to = new RootFile(filepath);
        if (isRoot) {
            RootTools.remount(new RootFile(filepath).getParent(), "rw");
            to = new RootFile(Environment.getExternalStorageDirectory().toString() + "/temp/" + new File(filepath).getName());
        }

        try {
            FileWriter fw = new FileWriter(to);
            fw.write(e.getText().toString());
            fw.close();

            if (isRoot)
            {
                RootTools.remount(new RootFile(filepath).getParent(), "rw");
                RootFile from = new RootFile(filepath);
                from.delete();
                RootFile fromDir = new RootFile(from.getParent());
                FileUtil.RootFileCopy(to, fromDir);
                String w = "busybox chmod " + perm + " " + filepath;

                try {
                    Command cmd = new Command(0, w) {

                        @Override
                        public void output(int id, String line) {
                        }
                    };

                    RootTools.getShell(true).add(cmd).waitForFinish();
                } catch (Exception e) {
                }
            }
            new RootFile(Environment.getExternalStorageDirectory().toString() + "/temp").delete();
        } catch (IOException e) {
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
            RootFile to = new RootFile(new RootFile(filepath).getParent());
            if (isRoot) {
                new File(Environment.getExternalStorageDirectory().toString() + "/temp/").mkdirs();
                to = new RootFile(Environment.getExternalStorageDirectory().toString() + "/temp");
                RootTools.remount(new RootFile(filepath).getParent(), "rw");
                FileUtil.RootFileCopy(new RootFile(filepath), to);
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