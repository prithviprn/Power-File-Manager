package pe.kmh.fm;

import java.io.IOException;
import java.util.Scanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class SettingsActivity extends SherlockPreferenceActivity {

	SharedPreferences sharedPrefs;
	OnSharedPreferenceChangeListener ospcl;
	
	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref);

		Crouton.makeText(SettingsActivity.this, R.string.RestartApp, Style.ALERT).show();

		PackageInfo pi = null;
		try {
			pi = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
		}
		catch (NameNotFoundException e) {
		}
		String version = pi.versionName;
		int versionCode = pi.versionCode;
		Preference r = getPreferenceScreen().findPreference("Info");
		r.setSummary("Version " + version + " (Build " + versionCode + ")");

		getSupportActionBar().setTitle(R.string.Setting);

		boolean isPro = getIntent().getBooleanExtra("isPro", false);
		boolean isRoot = getIntent().getBooleanExtra("isRoot", true);

		if (!isRoot) {
			getPreferenceScreen().findPreference("AutoPermissionOptions").setSummary(R.string.AutoPermissionOptionsNotRoot);
			getPreferenceScreen().findPreference("AutoPermissionOptions").setEnabled(false);
		}
		else if (!isPro) {
			getPreferenceScreen().findPreference("AutoPermissionOptions").setSummary(R.string.AutoPermissionOptionsNotPro);
			getPreferenceScreen().findPreference("AutoPermissionOptions").setEnabled(false);
		}

		if (isRoot) {
			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			ospcl = new OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					String sApp = sharedPrefs.getString("SystemApp_APerm", "644");
					String sEtc = sharedPrefs.getString("SystemEtc_APerm", "644");
					String sFonts = sharedPrefs.getString("SystemFonts_APerm", "644");
					String sFrame = sharedPrefs.getString("SystemFramework_APerm", "644");
					String sMedia = sharedPrefs.getString("SystemMedia_APerm", "644");
					String s = sharedPrefs.getString("System_APerm", "644");
					
					PreferenceScreen Opts = ((PreferenceScreen) getPreferenceScreen().findPreference("AutoPermissionOptions"));
					
					Opts.findPreference("SystemApp_APerm").setSummary(sApp);
					Opts.findPreference("SystemEtc_APerm").setSummary(sEtc);
					Opts.findPreference("SystemFonts_APerm").setSummary(sFonts);
					Opts.findPreference("SystemFramework_APerm").setSummary(sFrame);
					Opts.findPreference("SystemMedia_APerm").setSummary(sMedia);
					Opts.findPreference("System_APerm").setSummary(s);
				}
			};
			
			String sApp = sharedPrefs.getString("SystemApp_APerm", "644");
			String sEtc = sharedPrefs.getString("SystemEtc_APerm", "644");
			String sFonts = sharedPrefs.getString("SystemFonts_APerm", "644");
			String sFrame = sharedPrefs.getString("SystemFramework_APerm", "644");
			String sMedia = sharedPrefs.getString("SystemMedia_APerm", "644");
			String s = sharedPrefs.getString("System_APerm", "644");
			
			PreferenceScreen Opts = ((PreferenceScreen) getPreferenceScreen().findPreference("AutoPermissionOptions"));
			
			Opts.findPreference("SystemApp_APerm").setSummary(sApp);
			Opts.findPreference("SystemEtc_APerm").setSummary(sEtc);
			Opts.findPreference("SystemFonts_APerm").setSummary(sFonts);
			Opts.findPreference("SystemFramework_APerm").setSummary(sFrame);
			Opts.findPreference("SystemMedia_APerm").setSummary(sMedia);
			Opts.findPreference("System_APerm").setSummary(s);
		}
	}

	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		String key = preference.getKey().toString();
		AlertDialog.Builder ad = new AlertDialog.Builder(SettingsActivity.this);
		if (key.equals("RootTools")) ad.setMessage(getLicenseText()).show();
		else if (key.equals("Crouton")) ad.setMessage(Crouton.getLicenseText()).show();
		else if (key.equals("ActionBarSherlock")) ad.setMessage(getLicenseText()).show();
		else if (key.equals("CommonCompress")) ad.setMessage(getLicenseText()).show();
		else if (key.equals("AUIL")) ad.setMessage(getLicenseText()).show();
		else if (key.equals("SND")) ad.setMessage(getLicenseText()).show();
		return super.onPreferenceTreeClick(preferenceScreen, preference);
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
		}
		catch (IOException e) {
		}

		return outLines.toString();
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    if(sharedPrefs != null && ospcl != null) sharedPrefs.registerOnSharedPreferenceChangeListener(ospcl);
	}

	@Override
	protected void onPause() {      
	    super.onPause();
	    if(sharedPrefs != null && ospcl != null) sharedPrefs.unregisterOnSharedPreferenceChangeListener(ospcl);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Crouton.cancelAllCroutons();
	}
}