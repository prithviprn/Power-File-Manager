package pe.kmh.fm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import pe.kmh.fm.util.StorageList;

public class SettingsActivity extends PreferenceActivity {

	SharedPreferences sharedPrefs;
	OnSharedPreferenceChangeListener ospcl;
	boolean isChanged = false;

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref);

		Crouton.makeText(SettingsActivity.this, R.string.RestartApp, Style.ALERT).show();

		PackageInfo pi = null;
		try {
			pi = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
		} catch (NameNotFoundException e) {
		}
		String version = pi.versionName;
		int versionCode = pi.versionCode;
		Preference r = getPreferenceScreen().findPreference("Info");
		r.setSummary("Version " + version + " (Build " + versionCode + ")");

		boolean isRoot = getIntent().getBooleanExtra("isRoot", true);

		if (!isRoot) {
			getPreferenceScreen().findPreference("AutoPermissionOptions").setSummary(R.string.AutoPermissionOptionsNotRoot);
			getPreferenceScreen().findPreference("AutoPermissionOptions").setEnabled(false);
		}

		ListPreference startPath = (ListPreference) getPreferenceScreen().findPreference("StartPath");

		String extPath;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			File[] t_Path_arr = ContextCompat.getExternalFilesDirs(getApplicationContext(), "");

			if (t_Path_arr.length > 1 && t_Path_arr[1] != null) {
				String t_Path = t_Path_arr[1].getAbsolutePath();
				int point = t_Path.indexOf("Android");
				extPath = (t_Path.substring(0, point - 1));
			} else extPath = null;
		} else extPath = StorageList.getMicroSDCardDirectory();

		String items[] = extPath != null ? new String[3] : new String[2];
		items[0] = getString(R.string.AutomaticSet);
		items[1] = Environment.getExternalStorageDirectory().getAbsolutePath() + "\n[" + getString(R.string.InternalStorage) + "]";
		if (extPath != null) items[2] = extPath + "\n[" + getString(R.string.ExternalStorage) + "]";

		startPath.setEntries(items);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		ospcl = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				Refresh_Screen();
				isChanged = true;
			}
		};
		Refresh_Screen();
	}

	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		String key = preference.getKey().toString();
		AlertDialog.Builder ad = new AlertDialog.Builder(SettingsActivity.this);
		if (key.equals("RootTools")) ad.setMessage(getLicenseText()).show();
		else if (key.equals("Crouton")) ad.setMessage(Crouton.getLicenseText()).show();
		else if (key.equals("CommonCompress")) ad.setMessage(getLicenseText()).show();
		else if (key.equals("AUIL")) ad.setMessage(getLicenseText()).show();
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@SuppressWarnings("deprecation")
	public void Refresh_Screen() {

		PreferenceScreen ps = getPreferenceScreen();

		String sort = sharedPrefs.getString("SortOption", getString(R.string.Alphabet));
		if (sort.equals("Alphabet")) sort = getString(R.string.Alphabet);
		else if (sort.equals("FolderFirst")) sort = getString(R.string.FolderFirst);
		else if (sort.equals("ABC")) sort = getString(R.string.ABC);
		String startPath = sharedPrefs.getString("StartPath", getString(R.string.AutomaticSet));
		if (startPath.equals("Automatic")) startPath = getString(R.string.AutomaticSet);
		else if (startPath.equals("Internal")) startPath = getString(R.string.InternalStorage);
		else if (startPath.equals("External")) startPath = getString(R.string.ExternalStorage);

		ps.findPreference("SortOption").setSummary(sort);
		ps.findPreference("StartPath").setSummary(startPath);

		PreferenceScreen Opts = ((PreferenceScreen) getPreferenceScreen().findPreference("AutoPermissionOptions"));

		String sApp = sharedPrefs.getString("SystemApp_APerm", "644");
		String sEtc = sharedPrefs.getString("SystemEtc_APerm", "644");
		String sFonts = sharedPrefs.getString("SystemFonts_APerm", "644");
		String sFrame = sharedPrefs.getString("SystemFramework_APerm", "644");
		String sMedia = sharedPrefs.getString("SystemMedia_APerm", "644");
		String sys = sharedPrefs.getString("System_APerm", "644");

		Opts.findPreference("SystemApp_APerm").setSummary(sApp);
		Opts.findPreference("SystemEtc_APerm").setSummary(sEtc);
		Opts.findPreference("SystemFonts_APerm").setSummary(sFonts);
		Opts.findPreference("SystemFramework_APerm").setSummary(sFrame);
		Opts.findPreference("SystemMedia_APerm").setSummary(sMedia);
		Opts.findPreference("System_APerm").setSummary(sys);

	}

	public String getLicenseText() {
		StringBuilder outLines = new StringBuilder();
		try {
			@SuppressWarnings("resource")
			Scanner s = new Scanner(this.getAssets().open("Apache.txt")).useDelimiter("\n");
			while (true) {
				outLines.append(s.next());
				if (s.hasNext()) outLines.append("\n");
				else break;
			}

			s.close();
		} catch (IOException e) {
		}

		return outLines.toString();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sharedPrefs != null && ospcl != null)
			sharedPrefs.registerOnSharedPreferenceChangeListener(ospcl);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sharedPrefs != null && ospcl != null)
			sharedPrefs.unregisterOnSharedPreferenceChangeListener(ospcl);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isChanged) {
			Context mContext = getApplicationContext();
			Intent mStartActivity = new Intent(mContext, MainActivity.class);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
			System.exit(0);
			Crouton.cancelAllCroutons();
		}
	}
}