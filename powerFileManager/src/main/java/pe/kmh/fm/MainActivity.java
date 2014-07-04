package pe.kmh.fm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pe.kmh.fm.prop.FileProperty;
import pe.kmh.fm.prop.RootFile;
import pe.kmh.fm.prop.RootFileProperty;
import pe.kmh.fm.prop.SearchedFileProperty;
import pe.kmh.fm.util.FileUtil;
import pe.kmh.fm.util.StorageList;
import pe.kmh.fm.util.ZipUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import com.crashlytics.android.Crashlytics;

public class MainActivity extends SherlockActivity {

	ArrayList<FileProperty> item = null;
	ArrayList<RootFileProperty> rootitem = null;
	ArrayList<String> path = null;
	Drawable[] icon;
	final int MAX_LIST_ITEMS = 2000;
	ArrayList<String> clipboard = new ArrayList<String>();
	int Clipboard_Count = 0;
	ListView list;
	String root;
	TextView myPath;
	int tag_len;
	int nowlevel = -1;
	int list_state_index[] = new int[MAX_LIST_ITEMS + 1];
	int list_state_top[] = new int[MAX_LIST_ITEMS + 1];
	int isSelected[] = new int[MAX_LIST_ITEMS + 1];
	int Selected_Count = 0;
	long backPressedTime = 0;
	String nowPath = "";
	boolean showMultiSelectToast;
	int NumberOfImageFiles = 0;
	String sfilename;
	String appTheme;
	int SortFlag;
	Drawable nowIcon;
	FileAdapter adapter;
	Resources res;
	int Folder = R.drawable.folder;
	int Others;
	int Audio;
	int Compressed;
	int Video;
	int Check;
	int Scroll_Image;
	int Apk = -1;
	int Image = -2;
	int[] internal_icon;
	Command cmd;
	boolean isRoot = false;
	boolean ShowHiddenFiles;
	boolean UseImageLoader;
	boolean AutoRootCheck;
	String StartPathPref;
	String payload;
	boolean isCracked = false;
	boolean Zip_Flag = false;
	File f;
	ApkLoader loader;
	SharedPreferences sharedPrefs;
	SharedPreferences.Editor editor;
	String[] MenuListItems;
	DrawerLayout MenuLayout;
	SherlockActionBarDrawerToggle MenuToggle;
	ListView MenuList;

	static final int FOLDER_FIRST = 1; // Folder First
	static final int ABC_ORDER = 2; // ABCDE---abcde---xyz
	static final int ALPHABET_ORDER = 3; // AbcDeFgH--XyZ

	// Activity RequestCode
	static final int IAB_FINISHED = 2;
	static final int TEXT_EDITOR_REQUEST = 1;
	static final int JOB_SAVED = 1;
	static final int JOB_NOT_SAVED = 0;

	// Icons Numbers
	static final int NEW_FOLDER = -1;
	static final int NEW_FILE = -2;
	static final int CHANGE_STORAGE = -3;
	static final int REFRESH = -4;
	static final int SEARCH = -5;
	static final int REBOOT = -6;
	static final int SETTING = -7;

	StringBuilder sb;
	ActionMode mActionMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		importPreferences();

		boolean isBusyboxAvailable = true;
		if (AutoRootCheck && RootTools.isAccessGiven()) {
			if (!RootTools.isBusyboxAvailable()) {
				isBusyboxAvailable = false;
				AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
				aDialog.setTitle("Busybox");
				aDialog.setMessage(getString(R.string.BusyboxRequired));
				aDialog.setCancelable(false);
				aDialog.setPositiveButton(getString(R.string.GotoPlayStore), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						RootTools.offerBusyBox(MainActivity.this);
					}
				});

				aDialog.show();
			}
			isRoot = true;
		}

		ShowHiddenFiles = sharedPrefs.getBoolean("HiddenOption", isRoot);

		if (!isRoot) item = new ArrayList<FileProperty>();
		else rootitem = new ArrayList<RootFileProperty>();
		path = new ArrayList<String>();

		if (isRoot) setContentView(R.layout.root_mode);
		else setContentView(R.layout.normal_mode);

		res = getResources();

		myPath = (TextView) findViewById(R.id.path);

		root = isRoot ? "/" : Environment.getExternalStorageDirectory().toString();

		StartPathPref = sharedPrefs.getString("StartPath", "Automatic");
		if (StartPathPref.equals("Internal")) root = Environment.getExternalStorageDirectory().toString();
		else if (StartPathPref.equals("External")) root = getExternalSdPath();

		findViewById(R.id.CopyBtn).setVisibility(View.GONE);
		findViewById(R.id.PasteBtn).setVisibility(View.GONE);
		findViewById(R.id.MoveBtn).setVisibility(View.GONE);
		findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
		if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);

		setListeners();

		PackageInfo pi = null;
		try {
			pi = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
		}
		catch (NameNotFoundException e) {
		}

		String buildNo = sharedPrefs.getString("BuildNumber", "");
		String appName = sharedPrefs.getString("AppName", "");

		if (!appName.equals(pi.packageName)) // First Start
		{
			AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
			aDialog.setTitle(getString(R.string.FirstStart));
			aDialog.setMessage(getString(R.string.LeaveReview));
			aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					MenuLayout.openDrawer(MenuList);
				}
			});

			aDialog.setNegativeButton(getString(R.string.GotoPlayStore), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=pe.kmh.fm"));
					startActivity(intent);
					MenuLayout.openDrawer(MenuList);
				}
			});

			aDialog.show();

			editor.putBoolean("HiddenOption", isRoot);
			Toast.makeText(getApplicationContext(), getString(R.string.SlideIt), Toast.LENGTH_LONG).show();
		}

		if (appName.equals(pi.packageName) && !buildNo.equals(pi.versionName)) // Update
		{
			// TODO Update Log Window
			AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
			aDialog.setTitle(getString(R.string.UpdateLogDialog));
			aDialog.setMessage(getString(R.string.UpdateLog));

			aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
				}
			});

			aDialog.show();
		}

		showMultiSelectToast = sharedPrefs.getBoolean("showMultiSelectToast", true);
		editor.putString("BuildNumber", pi.versionName);
		editor.putString("AppName", pi.packageName);
		editor.putBoolean("showMultiSelectToast", false);
		editor.commit();

		ImageLoaderConfiguration iConfig = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		ImageLoader.getInstance().init(iConfig);

		tag_len = getString(R.string.Path).length() + 1;

		sb = new StringBuilder();

		if (isBusyboxAvailable) LoadList(root);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		MenuToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		MenuToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (MenuToggle.onOptionsItemSelected(item)) return true;

		return super.onOptionsItemSelected(item);
	}

	ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.multi_select_ondark, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem mitem) {
			int itemId = mitem.getItemId();
			if (itemId == R.id.SelectAll) {
				int s;
				if ((path.size() - (nowPath.equals(root) ? 0 : 1)) != Selected_Count) s = View.VISIBLE;
				else s = View.GONE;
				SelectAll(s);
				return false;
			}

			else if (itemId == R.id.Rename) {
				int position = -1;
				if (Selected_Count == 0) Crouton.makeText(MainActivity.this, R.string.NoSelected, Style.ALERT).show();
				if (Selected_Count > 1) Crouton.makeText(MainActivity.this, R.string.NowMultiSelected, Style.ALERT).show();
				if (Selected_Count != 1) return false;
				for (int i = 0; i <= MAX_LIST_ITEMS; i++) {
					if (isSelected[i] == View.VISIBLE) {
						position = i;
						break;
					}
				}

				FileRename(path.get(position));
				return false;
			}

			else if (itemId == R.id.Share) {
				if (Selected_Count == 0) {
					Crouton.makeText(MainActivity.this, R.string.NoSelected, Style.ALERT).show();
				}

				Intent intent = new Intent();

				if (Selected_Count > 1) {
					intent.setAction(Intent.ACTION_SEND_MULTIPLE);
					intent.setType("*/*");

					String s;
					String path = nowPath.endsWith("/") ? nowPath : nowPath + "/";
					ArrayList<Uri> Uris = new ArrayList<Uri>();
					for (int i = 0; i < (isRoot ? rootitem.size() : item.size()); i++) {
						if (isSelected[i] == View.VISIBLE) {
							if (isRoot) s = path + rootitem.get(i).getName();
							else s = path + item.get(i).getName();
							Uris.add(Uri.fromFile(new File(s)));
						}
					}

					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, Uris);
					PackageManager packageManager = getPackageManager();
					List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
					boolean isIntentSafe = activities.size() > 0;
					if (isIntentSafe) startActivity(intent);
					else Crouton.makeText(MainActivity.this, R.string.CannotShare, Style.ALERT).show();
					Refresh_Screen();
					return false;
				}
				else {
					int position = 0;
					for (int i = 0; i <= MAX_LIST_ITEMS; i++) {
						if (isSelected[i] == View.VISIBLE) {
							position = i;
							break;
						}
					}

					intent.setAction(Intent.ACTION_SEND);

					String name;
					if (isRoot) name = rootitem.get(position).getName();
					else name = item.get(position).getName();

					String extension = FileUtil.getExtension(name);
					String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
					intent.setType(mimeType);
					String path = nowPath.endsWith("/") ? nowPath : nowPath + "/";
					intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path + name)));
					PackageManager packageManager = getPackageManager();
					List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
					boolean isIntentSafe = activities.size() > 0;
					try {
						if (isIntentSafe) startActivity(intent);
						else Crouton.makeText(MainActivity.this, R.string.CannotShare, Style.ALERT).show();
					}
					catch (Exception e) {
						Crouton.makeText(MainActivity.this, R.string.CannotShare, Style.ALERT).show();
						e.printStackTrace();
					}
					Refresh_Screen();
					return false;
				}
			}

			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			for (int i = 0; i < path.size(); i++) {
				isSelected[i] = View.GONE;
				View v = list.getChildAt(i);
				if (v != null) {
					ImageView check = (ImageView) v.findViewById(R.id.check);
					check.setVisibility(View.GONE);
					check.refreshDrawableState(); // Refresh
				}
			}
			Selected_Count = 0;
			findViewById(R.id.CopyBtn).setVisibility(View.GONE);
			findViewById(R.id.PasteBtn).setVisibility(View.GONE);
			findViewById(R.id.MoveBtn).setVisibility(View.GONE);
			findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
			if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);
		}
	};

	public void importPreferences() {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPrefs.edit();

		boolean crashActivation = sharedPrefs.getBoolean("CrashlyticsActivation", true);
		if (crashActivation) {
			Log.d("PowerFileManager", "Crashlytics Enabled");
			Crashlytics.start(this);
		}

		appTheme = sharedPrefs.getString("AppTheme", "Light");
		if (appTheme.equals("Light")) {
			setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
			Others = R.drawable.others_light;
			Audio = R.drawable.audio_light;
			Compressed = R.drawable.compressed_light;
			Video = R.drawable.video_light;
			Check = R.drawable.check_light;
			Scroll_Image = R.drawable.image_light;
		}
		else if (appTheme.equals("Dark")) {
			setTheme(R.style.Theme_Sherlock);
			Others = R.drawable.others_dark;
			Audio = R.drawable.audio_dark;
			Compressed = R.drawable.compressed_dark;
			Video = R.drawable.video_dark;
			Check = R.drawable.check_dark;
			Scroll_Image = R.drawable.image_dark;
		}
		else {
			appTheme = "Light";
			setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
			editor.putString("AppTheme", "Light");
			editor.commit();
			Others = R.drawable.others_light;
			Audio = R.drawable.audio_light;
			Compressed = R.drawable.compressed_light;
			Video = R.drawable.video_light;
			Check = R.drawable.check_light;
			Scroll_Image = R.drawable.image_light;
		}

		String Sort = sharedPrefs.getString("SortOption", "Alphabet");
		if (Sort.equals("FolderFirst")) SortFlag = FOLDER_FIRST;
		else if (Sort.equals("ABC")) SortFlag = ABC_ORDER;
		else if (Sort.equals("Alphabet")) SortFlag = ALPHABET_ORDER;

		UseImageLoader = sharedPrefs.getBoolean("UseImageLoader", true);

		AutoRootCheck = sharedPrefs.getBoolean("AutoRootCheck", true);
	}

	public void setListeners() {
		MenuListItems = isRoot ? res.getStringArray(R.array.RootModeMenuItems) : res.getStringArray(R.array.NormalModeMenuItems);
		MenuLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		MenuToggle = new SherlockActionBarDrawerToggle(this, MenuLayout, R.drawable.ic_drawer, R.string.OpenMenu, R.string.CloseMenu);
		MenuLayout.setDrawerListener(MenuToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		MenuList = (ListView) findViewById(R.id.left_drawer);
		if (appTheme.equals("Light")) MenuList.setBackgroundColor(Color.WHITE);
		else MenuList.setBackgroundColor(Color.BLACK);

		ArrayList<String> arr = new ArrayList<String>();
		Collections.addAll(arr, MenuListItems);
		MenuList.setAdapter(new DrawerAdapter(arr));
		MenuList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				int itemId = 0;
				if (pos == 0) itemId = NEW_FOLDER;
				else if (pos == 1) itemId = NEW_FILE;
				else if (pos == 2) itemId = CHANGE_STORAGE;
				else if (pos == 3) itemId = REFRESH;
				else if (pos == 4) itemId = SEARCH;
				else if (pos == 5 && !isRoot) itemId = SETTING;
				else if (pos == 5 && isRoot) itemId = REBOOT;
				else if (pos == 6) itemId = SETTING;
				else return;

				MenuLayout.closeDrawer(MenuList);

				if (itemId == NEW_FOLDER) {
					MakeNewFolder();
					return;
				}

				else if (itemId == NEW_FILE) {
					MakeNewFile();
					return;
				}

				else if (itemId == CHANGE_STORAGE) {
					ChangeStorage();
					return;
				}

				else if (itemId == REFRESH) {
					LoadList(nowPath);
					return;
				}

				else if (itemId == SEARCH) {
					InitializeSearch();
					return;
				}

				else if (itemId == REBOOT) {
					AlertDialog.Builder ad1 = new AlertDialog.Builder(MainActivity.this);

					String[] items = { "Reboot", "Recovery" };
					Crouton.makeText(MainActivity.this, R.string.SomeDevicesNotOK, Style.INFO).show();
					ad1.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							AlertDialog.Builder ad2 = new AlertDialog.Builder(MainActivity.this);
							ad2.setMessage(getString(R.string.RebootConfirm));
							ad2.setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									String s = which == 0 ? "reboot" : "reboot recovery";

									cmd = new Command(0, s) {

										@Override
										public void output(int arg0, String arg1) {
										}
									};

									try {
										RootTools.getShell(true).add(cmd).waitForFinish();
									}
									catch (Exception e) {
										Crouton.makeText(MainActivity.this, e.getMessage(), Style.ALERT).show();
									}
								}
							});

							ad2.setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
								}
							});

							ad2.create().show();
						}
					});

					ad1.create().show();

					return;
				}

				else if (itemId == SETTING) {
					Intent sActivity = new Intent(MainActivity.this, SettingsActivity.class);
					sActivity.putExtra("isRoot", isRoot);
					startActivity(sActivity);
					return;
				}
			}

		});

		list = (ListView) findViewById(R.id.MainList);

		list.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("rawtypes")
			@Override
			public void onItemClick(AdapterView parent, View view, final int position, long id) {
				if (Selected_Count > 0) // If now Selecting File/Dirs
				{
					SelectItem(parent, position);
					return;
				}

				if (isRoot) f = new RootFile(path.get(position));
				else f = new File(path.get(position));

				if (isRoot && rootitem.get(position).getIcon().equals("FOLDER")) LoadList(path.get(position));
				else if (!isRoot && f.isDirectory()) LoadList(path.get(position));
				else {
					String name = f.getName();
					int length = name.length() - 1;
					sb.setLength(0);

					while (true) {
						if (name.charAt(length) != 46) sb.append(name.charAt(length--));
						else break;
						if (length <= 0) {
							sb = null;
							break;
						}
					}

					if (sb == null) sb = new StringBuilder().append("");
					StringBuilder temp = new StringBuilder();
					if (sb != null) temp = sb.reverse();
					String extension = temp.toString().toLowerCase();
					String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

					if (extension.equals("zip") || extension.equals("tar")) {
						Context mContext = getApplicationContext();
						LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
						final View layout = inflater.inflate(R.layout.getname, null);
						final EditText et = (EditText) layout.findViewById(R.id.gettingName);
						et.setHint(getString(R.string.ToUnZip));
						Crouton.makeText(MainActivity.this, R.string.ifEmptythen, Style.INFO).show();
						AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
						aDialog.setTitle(getString(R.string.Unzipping));
						aDialog.setView(layout);

						aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								String name = et.getText().toString();
								sb.setLength(0);
								sb.append(nowPath);
								if (!sb.toString().endsWith("/")) sb.append("/");
								if (!name.equals("")) sb.append(name);
								if (!sb.toString().endsWith("/")) sb.append("/");

								final ProgressDialog pdialog = ProgressDialog.show(MainActivity.this, getString(R.string.Unzipping),
										getString(R.string.Wait), true);

								final Handler handler = new Handler() {

									@Override
									public void handleMessage(Message msg) {
										LoadList(nowPath);
									}
								};

								new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											ZipUtil.unzip(f, new File(sb.toString()), "EUC-KR", FileUtil.getExtension(f));
										}
										catch (IOException e) {
											e.printStackTrace();
										}
										handler.sendEmptyMessage(0);
										pdialog.dismiss();
									}
								}).start();
							}
						});

						aDialog.show();
						return;
					}

					if (mimeType == null || extension.equals("xml") || extension.equals("txt") || !runFile(f, mimeType)) {
						AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
						alertDialog.setMessage(getString(R.string.AskOpenWithTextEditor));

						alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.No),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
									}
								});

						alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Yes),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent intent = new Intent(MainActivity.this, TextEditor.class);
										intent.putExtra("filepath", f.getPath());
										if (isRoot) intent.putExtra("Perm", FileUtil.calcPerm(rootitem.get(position).getPerm()));
										intent.putExtra("isRoot", isRoot);
										startActivityForResult(intent, TEXT_EDITOR_REQUEST);
									}
								});

						alertDialog.show();
						return;
					}
				}

				if (Clipboard_Count == 0) {
					findViewById(R.id.CopyBtn).setVisibility(View.GONE);
					findViewById(R.id.PasteBtn).setVisibility(View.GONE);
					findViewById(R.id.MoveBtn).setVisibility(View.GONE);
					findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
					if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);
				}
			}

		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (!root.equals(nowPath) && position == 0) return true;
				if (Selected_Count > 0) // If now Selecting File/Dirs
				{
					SelectItem(parent, position);
					return true;
				}

				mActionMode = startActionMode(mActionModeCallback);
				SelectItem(parent, position);

				if (showMultiSelectToast) {
					Crouton.makeText(MainActivity.this, R.string.NowStartMultiSelectMode, Style.INFO).show();
					showMultiSelectToast = false;
				}

				return true;
			}
		});

		list.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != SCROLL_STATE_IDLE) {
					if (isRoot) ((RootFileAdapter) list.getAdapter()).isScrolling = true;
					else ((FileAdapter) list.getAdapter()).isScrolling = true;
					loader.isScrolling = true;
				}
				else {
					if (isRoot) ((RootFileAdapter) list.getAdapter()).isScrolling = false;
					else ((FileAdapter) list.getAdapter()).isScrolling = false;

					((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
					loader.isScrolling = false;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		list.setScrollingCacheEnabled(true);
	}

	private void LoadList(String dirPath) {
		int path_len = myPath.getText().toString().length();
		if (path_len - tag_len < dirPath.length()) nowlevel++; // Go into
		else if (path_len - tag_len > dirPath.length()) nowlevel--; // Go back
		nowPath = dirPath;
		if (!isRoot) item.clear();
		if (isRoot) rootitem.clear();
		path.clear();

		if (isRoot) f = new RootFile(dirPath);
		else f = new File(dirPath);

		File[] files = null;
		String[] fileperms = null;
		Integer[] filesizes = null;

		if (isRoot) files = ((RootFile) f).listFiles();
		if (isRoot) fileperms = ((RootFile) f).listPerms();
		if (isRoot) filesizes = ((RootFile) f).listSizes();

		else files = (File[]) f.listFiles();

		for (int i = 0; i <= MAX_LIST_ITEMS; i++)
			isSelected[i] = View.GONE;
		Selected_Count = 0;

		if (!dirPath.equals(root)) {
			if (isRoot) rootitem.add(new RootFileProperty("FOLDER", "", getString(R.string.ParentFolder), "", ""));
			else item.add(new FileProperty("FOLDER", "", getString(R.string.ParentFolder), ""));
			path.add(f.getParent());
		}

		if (files == null) {
			Crouton.makeText(MainActivity.this, R.string.NoStorage, Style.ALERT).show();
			return;
		}

		int filelen = files.length;
		for (int i = 0; i < filelen; i++) {
			File file = files[i];

			if ((ShowHiddenFiles || !file.isHidden()) && file.canRead()) {
				boolean isDir = isRoot ? ((RootFile) file).isDirectory() : file.isDirectory();

				path.add(file.getPath());
				String icontype = isDir ? "FOLDER" : FileUtil.getExtension(file);
				String filesize = isDir ? "" : FileUtil.formatFileSize(file.length());
				if (isRoot && i < filesizes.length) filesize = isDir ? "" : FileUtil.formatFileSize(filesizes[i]);
				if (isRoot) {
					if (fileperms == null || fileperms.length <= i) rootitem.add(new RootFileProperty(icontype, file.getName(), DateFormat
							.format("yyyy.MM.dd kk:mm", file.lastModified()).toString(), filesize, ""));
					else rootitem.add(new RootFileProperty(icontype, file.getName(), DateFormat.format("yyyy.MM.dd kk:mm",
							file.lastModified()).toString(), filesize, fileperms[i]));
				}
				else item.add(new FileProperty(icontype, file.getName(), DateFormat.format("yyyy.MM.dd kk:mm", file.lastModified())
						.toString(), filesize));
			}
		}

		final int psize = path.size();

		if (!isRoot && SortFlag != ALPHABET_ORDER) // ABCabc Order
		{
			Comparator<FileProperty> sort = new Comparator<FileProperty>() {

				@Override
				public int compare(FileProperty a, FileProperty b) {
					return a.getName().compareTo(b.getName());
				}
			};

			Collections.sort(item, sort);
			Collections.sort(path);
		}

		if (SortFlag == ALPHABET_ORDER) {
			Comparator<FileProperty> sort_items = new Comparator<FileProperty>() {

				@Override
				public int compare(FileProperty a, FileProperty b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			};

			Comparator<String> sort_strs = new Comparator<String>() {

				@Override
				public int compare(String a, String b) {
					return a.compareToIgnoreCase(b);
				}
			};

			if (isRoot) {
				Collections.sort(rootitem, sort_items);
				Collections.sort(path, sort_strs);
			}
			else {
				Collections.sort(item, sort_items);
				Collections.sort(path, sort_strs);
			}
		}

		if (SortFlag == FOLDER_FIRST) {
			if (isRoot) {
				ArrayList<Integer> Folders = new ArrayList<Integer>();
				ArrayList<Integer> Files = new ArrayList<Integer>();

				for (int i = 0; i < psize; i++) {
					if (rootitem.get(i).getIcon().equals("FOLDER")) Folders.add(i);
					else Files.add(i);
				}

				ArrayList<RootFileProperty> temp_item = new ArrayList<RootFileProperty>(rootitem);

				rootitem.clear();
				for (int i = 0; i < Folders.size(); i++)
					rootitem.add(temp_item.get(Folders.get(i)));
				for (int i = 0; i < Files.size(); i++)
					rootitem.add(temp_item.get(Files.get(i)));

				ArrayList<String> temp_path = new ArrayList<String>(path);

				path.clear();
				for (int i = 0; i < Folders.size(); i++)
					path.add(temp_path.get(Folders.get(i)));
				for (int i = 0; i < Files.size(); i++)
					path.add(temp_path.get(Files.get(i)));
			}
			else // Not Rooted : ABC / FolderFirst Count Sort - O(N) Algorithm
			{
				ArrayList<Integer> Folders = new ArrayList<Integer>();
				ArrayList<Integer> Files = new ArrayList<Integer>();

				for (int i = 0; i < psize; i++) {
					if (item.get(i).getIcon().equals("FOLDER")) Folders.add(i);
					else Files.add(i);
				}

				ArrayList<FileProperty> temp_item = new ArrayList<FileProperty>(item);

				item.clear();
				for (int i = 0; i < Folders.size(); i++)
					item.add(temp_item.get(Folders.get(i)));
				for (int i = 0; i < Files.size(); i++)
					item.add(temp_item.get(Files.get(i)));

				ArrayList<String> temp_path = new ArrayList<String>(path);

				path.clear();
				for (int i = 0; i < Folders.size(); i++)
					path.add(temp_path.get(Folders.get(i)));
				for (int i = 0; i < Files.size(); i++)
					path.add(temp_path.get(Files.get(i)));
			}
		}

		if (!dirPath.equals(root)) {
			if (isRoot) rootitem.set(0, new RootFileProperty("FOLDER", "../", getString(R.string.ParentFolder), "", ""));
			else item.set(0, new FileProperty("FOLDER", "../", getString(R.string.ParentFolder), ""));
			path.set(0, f.getParent());
		}

		icon = new Drawable[psize];
		internal_icon = new int[psize];

		String mimeType, ico;
		for (int i = 0; i < psize; i++) {
			ico = isRoot ? rootitem.get(i).getIcon() : item.get(i).getIcon();
			if (ico.equals("FOLDER")) internal_icon[i] = Folder;
			else {
				mimeType = FileUtil.getMIME(ico);
				if (ico.equals("zip") || ico.equals("7z") || ico.equals("rar") || ico.equals("tar")) internal_icon[i] = Compressed;
				else if (mimeType == null) internal_icon[i] = Others;
				else if (mimeType.startsWith("image")) internal_icon[i] = Image;
				else if (mimeType.startsWith("audio")) internal_icon[i] = Audio;
				else if (ico.equals("avi") || ico.equals("wmv") || ico.equals("mkv") || ico.equals("mp4")) internal_icon[i] = Video;
				else if (!ico.equals("apk")) internal_icon[i] = Others;
				else if (ico.equals("apk")) internal_icon[i] = Apk;
			}
		}

		if (isRoot) {
			RootFileAdapter adapter = new RootFileAdapter(rootitem);
			if (path_len - tag_len <= dirPath.length()) // Go Into
			{
				list_state_index[nowlevel] = list.getFirstVisiblePosition();
				list_state_top[nowlevel] = list.getChildAt(0) == null ? 0 : list.getChildAt(0).getTop();
				list.setAdapter(adapter);
			}
			else if (path_len - tag_len > dirPath.length()) // Go Back
			{
				list.setAdapter(adapter);
				list.setSelectionFromTop(list_state_index[nowlevel + 1], list_state_top[nowlevel + 1]);
			}
		}

		else {
			FileAdapter adapter = new FileAdapter(item);
			if (path_len - tag_len <= dirPath.length()) // Go Into
			{
				list_state_index[nowlevel] = list.getFirstVisiblePosition();
				list_state_top[nowlevel] = list.getChildAt(0) == null ? 0 : list.getChildAt(0).getTop();
				list.setAdapter(adapter);
			}
			else if (path_len - tag_len > dirPath.length()) // Go Back
			{
				list.setAdapter(adapter);
				list.setSelectionFromTop(list_state_index[nowlevel + 1], list_state_top[nowlevel + 1]);
			}
		}

		if (path_len - tag_len == dirPath.length()) list.setSelectionFromTop(list_state_index[nowlevel], list_state_top[nowlevel]);
		myPath.setText(getString(R.string.Path) + " " + dirPath);

		if (mActionMode != null) mActionMode.finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == TEXT_EDITOR_REQUEST) {
			if (resultCode == JOB_SAVED) LoadList(nowPath);
		}
	}

	@Override
	public void onBackPressed() {
		if (MenuLayout.isDrawerOpen(MenuList)) MenuLayout.closeDrawer(MenuList);
		else if (nowlevel > 0 && Selected_Count == 0) LoadList(path.get(0));
		else if (nowlevel >= 0 && (Selected_Count > 0 || Clipboard_Count > 0)) {
			for (int i = 0; i <= MAX_LIST_ITEMS; i++)
				isSelected[i] = View.GONE;
			Selected_Count = 0;
			Clipboard_Count = 0;
			if (showMultiSelectToast) Crouton.makeText(MainActivity.this, R.string.EndofMultiSelectMode, Style.INFO).show();

			Refresh_Screen();
		}
		else {
			long tempTime = System.currentTimeMillis();
			long intervalTime = tempTime - backPressedTime;

			if (0 <= intervalTime && 2000 >= intervalTime) super.onBackPressed();
			else {
				backPressedTime = tempTime;
				Crouton.makeText(MainActivity.this, R.string.PressAgainToExit, Style.INFO).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (!MenuLayout.isDrawerOpen(MenuList)) MenuLayout.openDrawer(MenuList);
			else MenuLayout.closeDrawer(MenuList);
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onCopyBtnPress(View v) {
		int n = 0, i = 0;
		clipboard.clear();
		while (true) {
			if (isSelected[i] == View.VISIBLE) {
				clipboard.add(path.get(i));
				if (n < Selected_Count) n++;
				else break;
			}
			if (i < MAX_LIST_ITEMS) i++;
			else break;
		}

		Clipboard_Count = Selected_Count;

		Refresh_Screen();
		findViewById(R.id.PasteBtn).setVisibility(View.VISIBLE);
		findViewById(R.id.MoveBtn).setVisibility(View.VISIBLE);
	}

	public void onPasteBtnPress(View v) {
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setTitle(R.string.Copying);
		dialog.setMessage(getString(R.string.Wait));
		dialog.setMax(Clipboard_Count);
		dialog.setCancelable(false);
		dialog.show();

		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				int mes = msg.what;
				if (mes == -1) {
					Set_Auto_Perm();
					LoadList(nowPath);
				}
				else if (mes == -2) {
					Toast.makeText(getApplicationContext(), getString(R.string.CannotCopy), Toast.LENGTH_SHORT).show();
				}

				else {
					dialog.setProgress(mes);
					dialog.setMessage(clipboard.get(mes - 1));
				}
			}
		};

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < Clipboard_Count; i++) {
						if (clipboard.get(i).equals(nowPath)) {
							handler.sendEmptyMessage(-2);
							continue;
						}
						if (isRoot) FileUtil.RootFileCopy(new RootFile(clipboard.get(i)), new RootFile(nowPath));
						else FileUtil.NormalFileCopy(new File(clipboard.get(i)), new File(nowPath));
						handler.sendEmptyMessage(i + 1);
					}
				}
				catch (IOException e) {
					Crouton.makeText(MainActivity.this, e.getMessage(), Style.ALERT).show();
				}
				handler.sendEmptyMessage(-1);
				dialog.dismiss();
			}
		}).start();

		findViewById(R.id.CopyBtn).setVisibility(View.GONE);
		findViewById(R.id.PasteBtn).setVisibility(View.GONE);
		findViewById(R.id.MoveBtn).setVisibility(View.GONE);
		findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
		if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);
	}

	public void onDeleteBtnPress(View v) {
		AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
		aDialog.setMessage(getString(R.string.DeleteConfirm));
		aDialog.setCancelable(false);
		aDialog.setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface di, int which) {
				final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, getString(R.string.Deleting),
						getString(R.string.Wait), true);

				final Handler handler = new Handler() {

					@Override
					public void handleMessage(Message msg) {
						LoadList(nowPath);
					}
				};

				new Thread(new Runnable() {

					@Override
					public void run() {
						int i = 0;
						while (true) {
							Log.d("PFM", path.get(i));
							if (isSelected[i] == View.VISIBLE) {
								if (isRoot) new RootFile(path.get(i)).delete();
								else FileUtil.DeleteFile(path.get(i));
							}
							if (i < path.size() - 1) i++;
							else break;
						}

						handler.sendEmptyMessage(0);
						dialog.dismiss();
					}
				}).start();

				findViewById(R.id.CopyBtn).setVisibility(View.GONE);
				findViewById(R.id.PasteBtn).setVisibility(View.GONE);
				findViewById(R.id.MoveBtn).setVisibility(View.GONE);
				findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
				if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);
			}
		});

		aDialog.setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
			}
		});

		AlertDialog ad = aDialog.create();
		ad.show();
	}

	public void onMoveBtnPress(View v) {
		final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setTitle(R.string.Moving);
		dialog.setMessage(getString(R.string.Wait));
		dialog.setMax(Clipboard_Count);
		dialog.setCancelable(false);
		dialog.show();

		final Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				int mes = msg.what;
				if (mes == -1) {
					Set_Auto_Perm();
					LoadList(nowPath);
				}
				else if (mes == -2) {
					Toast.makeText(getApplicationContext(), getString(R.string.CannotMove), Toast.LENGTH_SHORT).show();
				}

				else {
					dialog.setProgress(mes);
					dialog.setMessage(clipboard.get(mes - 1));
				}
			}
		};

		new Thread(new Runnable() {

			@Override
			public void run() {
				RootTools.remount(nowPath, "rw");
				for (int i = 0; i < Clipboard_Count; i++) {

					if (clipboard.get(i).equals(nowPath)) {
						handler.sendEmptyMessage(-2);
						continue;
					}

					final int prog = i + 1;

					if (!isRoot || (new File(clipboard.get(i)).canRead() && (new File(nowPath).canWrite()))) {
						try {
							boolean result = FileUtil.NormalFileCopy(new File(clipboard.get(i)), new File(nowPath));
							if (result == false) handler.sendEmptyMessage(-2);
							else FileUtil.DeleteFile(clipboard.get(i));
							handler.sendEmptyMessage(prog);
						}
						catch (IOException e) {
							Crouton.makeText(MainActivity.this, e.getMessage(), Style.ALERT).show();
						}
					}

					else {
						final String w = "busybox mv \"" + clipboard.get(i) + "\" \"" + nowPath + "\"";
						cmd = new Command(0, w) {

							@Override
							public void output(int id, String line) {
								handler.sendEmptyMessage(prog);
							}
						};

						try {
							RootTools.getShell(true).add(cmd).waitForFinish();
						}

						catch (Exception e) {
							Crouton.makeText(MainActivity.this, e.getMessage(), Style.ALERT).show();
						}
					}

				}

				handler.sendEmptyMessage(-1);
				dialog.dismiss();
			}
		}).start();

		findViewById(R.id.CopyBtn).setVisibility(View.GONE);
		findViewById(R.id.PasteBtn).setVisibility(View.GONE);
		findViewById(R.id.MoveBtn).setVisibility(View.GONE);
		findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
		if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);
	}

	public void onPermBtnPress(View v) {
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.permset, null);
		final EditText NumPerm = (EditText) layout.findViewById(R.id.NumericPerm);
		AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
		aDialog.setTitle(getString(R.string.Perm));
		aDialog.setView(layout);

		aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String newPerm = NumPerm.getText().toString();
				RootTools.remount(nowPath, "rw");
				int i = 0;
				while (true) {
					String aPath = new String();
					if (isSelected[i] == View.VISIBLE) aPath = path.get(i);
					{
						final String w = "busybox chmod " + newPerm + " \"" + aPath + "\"";
						cmd = new Command(0, w) {

							@Override
							public void output(int id, String line) {
							}
						};

						try {
							RootTools.getShell(true).add(cmd).waitForFinish();
						}
						catch (Exception e) {
							Crouton.makeText(MainActivity.this, e.getMessage(), Style.ALERT).show();
						}
					}

					if (i < path.size()) i++;
					else break;
				}

				LoadList(nowPath);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
				findViewById(R.id.CopyBtn).setVisibility(View.GONE);
				findViewById(R.id.PasteBtn).setVisibility(View.GONE);
				findViewById(R.id.MoveBtn).setVisibility(View.GONE);
				findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
				findViewById(R.id.PermBtn).setVisibility(View.GONE);
			}
		});

		AlertDialog ad = aDialog.create();
		ad.show();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public void Set_Auto_Perm() {
		if (nowPath.startsWith("/system/app") || nowPath.startsWith("/system/etc") || nowPath.startsWith("/system/fonts")
				|| nowPath.startsWith("/system/framework") || nowPath.startsWith("/system/media") || nowPath.equals("/system")) {
			String p = null;
			if (nowPath.startsWith("/system/app")) p = sharedPrefs.getString("SystemApp_APerm", "644");
			if (nowPath.startsWith("/system/etc")) p = sharedPrefs.getString("SystemEtc_APerm", "644");
			if (nowPath.startsWith("/system/fonts")) p = sharedPrefs.getString("SystemFonts_APerm", "644");
			if (nowPath.startsWith("/system/framework")) p = sharedPrefs.getString("SystemFramework_APerm", "644");
			if (nowPath.startsWith("/system/media")) p = sharedPrefs.getString("SystemMedia_APerm", "644");
			if (nowPath.equals("/system")) p = sharedPrefs.getString("System_APerm", "644");
			for (int i = 0; i < Clipboard_Count; i++) {
				if (new File(nowPath + "/" + new File(clipboard.get(i)).getName()).isFile()) {
					final String w = "busybox chmod " + p + " \"" + nowPath + "/" + new File(clipboard.get(i)).getName() + "\"";
					cmd = new Command(0, w) {

						@Override
						public void output(int id, String line) {
						}
					};

					try {
						RootTools.getShell(true).add(cmd).waitForFinish();
					}
					catch (Exception e) {
						Crouton.makeText(MainActivity.this, e.getMessage(), Style.ALERT).show();
					}
				}

				else {
					final String w = "busybox chmod 755" + " \"" + nowPath + "/" + new File(clipboard.get(i)).getName() + "\"";
					cmd = new Command(0, w) {

						@Override
						public void output(int id, String line) {
						}
					};

					try {
						RootTools.getShell(true).add(cmd).waitForFinish();
					}
					catch (Exception e) {
						Crouton.makeText(MainActivity.this, e.getMessage(), Style.ALERT).show();
					}
				}
			}

			Crouton.makeText(MainActivity.this, R.string.AutoPermissionSetFinished, Style.INFO).show();
		}

	}

	public void Refresh_Screen() {
		for (int i = 0; i <= MAX_LIST_ITEMS; i++)
			isSelected[i] = View.GONE;
		Selected_Count = 0;

		if (isRoot) {
			RootFileAdapter adapter = new RootFileAdapter(rootitem);
			list_state_index[nowlevel] = list.getFirstVisiblePosition();
			list_state_top[nowlevel] = list.getChildAt(0) == null ? 0 : list.getChildAt(0).getTop();
			list.setAdapter(adapter);
			list.setSelectionFromTop(list_state_index[nowlevel], list_state_top[nowlevel]);
		}
		else {
			FileAdapter adapter = new FileAdapter(item);
			list_state_index[nowlevel] = list.getFirstVisiblePosition();
			list_state_top[nowlevel] = list.getChildAt(0) == null ? 0 : list.getChildAt(0).getTop();
			list.setAdapter(adapter);
			list.setSelectionFromTop(list_state_index[nowlevel], list_state_top[nowlevel]);
		}

		findViewById(R.id.CopyBtn).setVisibility(View.GONE);
		findViewById(R.id.PasteBtn).setVisibility(View.GONE);
		findViewById(R.id.MoveBtn).setVisibility(View.GONE);
		findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
		if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);

		if (mActionMode != null && Selected_Count == 0) mActionMode.finish();
	}

	public void SelectItem(AdapterView<?> parent, int position) {
		if (!nowPath.equals(root) && position == 0) return;
		Button CopyBtn = (Button) findViewById(R.id.CopyBtn);
		Button DeleteBtn = (Button) findViewById(R.id.DeleteBtn);
		Button PermBtn = isRoot ? (Button) findViewById(R.id.PermBtn) : null;

		if (isSelected[position] == View.VISIBLE) // Already Selected
		{
			// When Selected_Count == 1, After this process it will be 0, then
			// set copy/delete button invisible
			if (Selected_Count == 1) {
				CopyBtn.setVisibility(View.GONE);
				DeleteBtn.setVisibility(View.GONE);
				if (isRoot) PermBtn.setVisibility(View.GONE);
				mActionMode.finish();
				Selected_Count = 1;
			}

			isSelected[position] = View.GONE; // Deselect
			Selected_Count--;
			int firstPos = parent.getFirstVisiblePosition();
			int wantedPos = position - firstPos;
			View v = parent.getChildAt(wantedPos);
			ImageView check = (ImageView) v.findViewById(R.id.check);
			check.setVisibility(View.GONE); // Uncheck it
			check.refreshDrawableState(); // Refresh Button
			if (Selected_Count > 0) mActionMode.setTitle(String.valueOf(Selected_Count) + " " + getString(R.string.N_Selected));
			if (Selected_Count == 1) mActionMode.getMenu().findItem(R.id.Rename).setVisible(true);
			else if (Selected_Count > 1) mActionMode.getMenu().findItem(R.id.Rename).setVisible(false);
			return;
		}

		CopyBtn.setVisibility(View.VISIBLE); // Set Visible
		DeleteBtn.setVisibility(View.VISIBLE); // Set Visible
		if (isRoot) PermBtn.setVisibility(View.VISIBLE); // Set Visible

		findViewById(R.id.PasteBtn).setVisibility(View.GONE);
		findViewById(R.id.MoveBtn).setVisibility(View.GONE);

		isSelected[position] = View.VISIBLE; // Set Visible
		Selected_Count++;
		int firstPos = parent.getFirstVisiblePosition();
		int wantedPos = position - firstPos;
		View v = parent.getChildAt(wantedPos);
		ImageView check = (ImageView) v.findViewById(R.id.check);
		check.setVisibility(View.VISIBLE);
		check.refreshDrawableState(); // Refresh

		mActionMode.setTitle(String.valueOf(Selected_Count) + " " + getString(R.string.N_Selected));

		if (Selected_Count == 1) mActionMode.getMenu().findItem(R.id.Rename).setVisible(true);
		else mActionMode.getMenu().findItem(R.id.Rename).setVisible(false);
		return;
	}

	public void SelectAll(int p) {
		for (int i = nowPath.equals(root) ? 0 : 1; i < path.size(); i++) {
			isSelected[i] = p;
			View v = list.getChildAt(i);
			if (v != null) {
				ImageView check = (ImageView) v.findViewById(R.id.check);
				check.setVisibility(p);
				check.refreshDrawableState(); // Refresh
			}
		}

		Selected_Count = path.size() - (nowPath.equals(root) ? 0 : 1);
		if (p == View.GONE) {
			Selected_Count = 0;
			mActionMode.finish();
			findViewById(R.id.CopyBtn).setVisibility(View.GONE);
			findViewById(R.id.PasteBtn).setVisibility(View.GONE);
			findViewById(R.id.MoveBtn).setVisibility(View.GONE);
			findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
			if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);
		}
		mActionMode.setTitle(String.valueOf(Selected_Count) + " " + getString(R.string.N_Selected));
		if (Selected_Count > 1) mActionMode.getMenu().findItem(R.id.Rename).setVisible(false);
	}

	public boolean runFile(File file, String MimeType) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, MimeType);
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		boolean isIntentSafe = activities.size() > 0;
		try {
			if (isIntentSafe) startActivity(intent);
			else return false;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public void FileRename(final String filepath) {
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.getname, null);
		EditText name = (EditText) layout.findViewById(R.id.gettingName);
		AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
		aDialog.setTitle(getString(R.string.Renaming));
		aDialog.setView(layout);
		name.setText(new File(filepath).getName());
		name.setSelection(name.getText().length());
		aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				EditText NewName = (EditText) layout.findViewById(R.id.gettingName);
				String newNameStr = NewName.getText().toString();

				File now = isRoot ? new RootFile(filepath) : new File(filepath);
				now.renameTo(new File(nowPath + "/" + newNameStr));
				LoadList(nowPath);
				findViewById(R.id.CopyBtn).setVisibility(View.GONE);
				findViewById(R.id.PasteBtn).setVisibility(View.GONE);
				findViewById(R.id.MoveBtn).setVisibility(View.GONE);
				findViewById(R.id.DeleteBtn).setVisibility(View.GONE);
				if (isRoot) findViewById(R.id.PermBtn).setVisibility(View.GONE);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});

		aDialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});

		AlertDialog ad = aDialog.create();
		ad.show();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public void MakeNewFolder() {
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.getname, null);
		final EditText FolderName = (EditText) layout.findViewById(R.id.gettingName);
		FolderName.setHint(R.string.newFolder_Hint);
		AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
		aDialog.setTitle(getString(R.string.newFolder));
		aDialog.setView(layout);

		aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String newFolderName = FolderName.getText().toString();

				File file = isRoot ? new RootFile(nowPath + "/" + newFolderName) : new File(nowPath + "/" + newFolderName);
				file.mkdir();
				LoadList(nowPath);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});

		aDialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});

		AlertDialog ad = aDialog.create();
		ad.show();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public void MakeNewFile() {
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.getname, null);
		final EditText FileName = (EditText) layout.findViewById(R.id.gettingName);
		FileName.setHint(R.string.newFile_Hint);
		AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
		aDialog.setTitle(getString(R.string.newFile));
		aDialog.setView(layout);

		aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String newFileName = FileName.getText().toString();

				File file = isRoot ? new RootFile(nowPath + "/" + newFileName) : new File(nowPath + "/" + newFileName);
				try {
					file.createNewFile();
				}
				catch (IOException e) {
				}

				LoadList(nowPath);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});

		aDialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});

		AlertDialog ad = aDialog.create();
		ad.show();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public void ChangeStorage() {
		AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
		aDialog.setTitle(getString(R.string.ChangeStorage));

		final String extPath = getExternalSdPath();

		if (extPath == null && !isRoot) {
			Crouton.makeText(MainActivity.this, R.string.NoExternalStorage, Style.CONFIRM).show();
			return;
		}
		if (!(Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)) Crouton.makeText(MainActivity.this, R.string.ShownDataCanIncorrect,
				Style.INFO).show();
		else Crouton.makeText(MainActivity.this, R.string.CannotGetStorageDataOnFroyo, Style.ALERT).show();

		String[] items = isRoot && extPath != null ? new String[3] : new String[2];

		int NowSelected = 0;

		if (isRoot) {
			items[0] = "/" + "\n[Root]";
			items[1] = Environment.getExternalStorageDirectory().getAbsolutePath() + "\n[" + getString(R.string.InternalStorage) + "]";
			if (extPath != null) items[2] = extPath + "\n[" + getString(R.string.ExternalStorage) + "]";

			if (root.equals("/")) NowSelected = 0;
			if (root.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) NowSelected = 1;
			if (extPath != null && root.equals(extPath)) NowSelected = 2;
		}
		else {
			items[0] = Environment.getExternalStorageDirectory().getAbsolutePath() + "\n[" + getString(R.string.InternalStorage) + "]";
			items[1] = extPath + "\n[" + getString(R.string.ExternalStorage) + "]";

			if (root.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) NowSelected = 0;
			if (extPath != null && root.equals(extPath)) NowSelected = 1;
		}

		aDialog.setSingleChoiceItems(items, NowSelected, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				item = new ArrayList<FileProperty>();
				rootitem = new ArrayList<RootFileProperty>();
				path = new ArrayList<String>();
				nowlevel = -1;
				list_state_index = new int[MAX_LIST_ITEMS + 1];
				list_state_top = new int[MAX_LIST_ITEMS + 1];
				isSelected = new int[MAX_LIST_ITEMS + 1];
				Selected_Count = 0;
				backPressedTime = 0;
				nowPath = "";

				if (which == 0) root = isRoot ? "/" : Environment.getExternalStorageDirectory().getAbsolutePath();
				if (which == 1) root = isRoot ? Environment.getExternalStorageDirectory().getAbsolutePath() : extPath;
				if (which == 2) root = extPath;
				myPath.setText("/");
				LoadList(root);

				dialog.dismiss();
			}
		});

		AlertDialog ad = aDialog.create();
		ad.show();
	}

	public void InitializeSearch() {
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.getname, null);
		final EditText SearchingFileName = (EditText) layout.findViewById(R.id.gettingName);
		SearchingFileName.setHint(R.string.Search_Hint);

		AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
		aDialog.setTitle(getString(R.string.Search));
		aDialog.setView(layout);

		aDialog.setPositiveButton(getString(R.string.Finish), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				sfilename = SearchingFileName.getText().toString();
				if (sfilename.equals("")) return;
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

				final ProgressDialog pdialog = ProgressDialog.show(MainActivity.this, getString(R.string.Searching),
						getString(R.string.Wait), true);
				new Thread(new Runnable() {

					@Override
					public void run() {

						ArrayList<SearchedFileProperty> arr = null;
						arr = Search(nowPath, sfilename, new ArrayList<SearchedFileProperty>());

						Intent searchActivity = new Intent(MainActivity.this, SearchActivity.class);
						searchActivity.putParcelableArrayListExtra("filelist", arr);
						startActivity(searchActivity);
						pdialog.dismiss();
					}
				}).start();
			}
		});

		aDialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
		});

		AlertDialog ad = aDialog.create();
		ad.show();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public ArrayList<SearchedFileProperty> Search(String Path, String Name, ArrayList<SearchedFileProperty> arr) {
		File file = isRoot ? new RootFile(Path) : new File(Path);
		if (file.equals(null)) return null;
		File list[] = file.listFiles();
		if (list == null) return null;
		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory() && (ShowHiddenFiles || !file.isHidden())) Search(list[i].getAbsolutePath(), Name, arr);
			else {
				if (list[i].getName().contains(Name))
					arr.add(new SearchedFileProperty(FileUtil.getExtension(list[i]), list[i].getName(), DateFormat.format(
							"yyyy.MM.dd kk:mm", list[i].lastModified()).toString(), "", list[i].getAbsolutePath()));
			}
		}

		return arr;
	}

	public Drawable getApkIcon(String filePath) {
		PackageInfo packageInfo = getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
		if (packageInfo == null) return res.getDrawable(R.drawable.android);
		ApplicationInfo appInfo = packageInfo.applicationInfo;
		appInfo.sourceDir = filePath;
		appInfo.publicSourceDir = filePath;
		Drawable icon = appInfo.loadIcon(getPackageManager());
		if (icon == null) icon = res.getDrawable(R.drawable.android);
		return icon;
	}

	public String getExternalSdPath() {
		String extPath;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			File[] t_Path_arr = ContextCompat.getExternalFilesDirs(getApplicationContext(), "");

			if (t_Path_arr.length > 1 && t_Path_arr[1] != null) {
				String t_Path = t_Path_arr[1].getAbsolutePath();
				int point = t_Path.indexOf("Android");
				extPath = ((File[]) (ContextCompat.getExternalFilesDirs(getApplicationContext(), "")))[1].getAbsolutePath().substring(0,
						point - 1);
			}
			else extPath = null;
		}
		else extPath = StorageList.getMicroSDCardDirectory();

		return extPath;
	}

	public class FileAdapter extends BaseAdapter {

		private ArrayList<FileProperty> object;
		boolean isScrolling = false;
		String filename, filedate, filesize, txtPerm;
		String file, mimeType;
		String s;
		Drawable dr;
		ViewHolder holder;

		public FileAdapter(ArrayList<FileProperty> object) {
			super();
			this.object = object;
			loader = new ApkLoader(getApplicationContext());
			s = nowPath.endsWith("/") ? nowPath : nowPath + "/";
		}

		@Override
		public int getCount() {
			return object.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
				convertView = inflater.inflate(R.layout.row, parent, false);
				holder = new ViewHolder();

				// Find View
				holder.fileicon = (ImageView) convertView.findViewById(R.id.icon);
				holder.filename = (TextView) convertView.findViewById(R.id.filename);
				holder.filedate = (TextView) convertView.findViewById(R.id.filedate);
				holder.filesize = (TextView) convertView.findViewById(R.id.filesize);
				holder.check = (ImageView) convertView.findViewById(R.id.check);
				holder.check.setBackgroundResource(Check);
				convertView.setTag(holder);
			}
			else holder = (ViewHolder) convertView.getTag();

			filename = object.get(position).getName();
			filedate = object.get(position).getDate();
			filesize = object.get(position).getSize();

			// Set Resources
			String dir = s + filename;

			if (internal_icon[position] > 0) holder.fileicon.setImageResource(internal_icon[position]);
			else if (icon[position] != null) holder.fileicon.setImageDrawable(icon[position]);
			else {
				if (internal_icon[position] == Apk) {
					if (isScrolling) holder.fileicon.setImageResource(R.drawable.android);
					else loader.DisplayImage(object.get(position).getName(), dir, holder.fileicon, position);
				}
				else if (internal_icon[position] == Image) {
					if (isScrolling) holder.fileicon.setImageResource(Scroll_Image);
					else {
						if (UseImageLoader) {
							holder.fileicon.setImageResource(Scroll_Image);
							ImageLoader.getInstance().displayImage("file:/" + dir, holder.fileicon, new SimpleImageLoadingListener() {

								@SuppressWarnings("deprecation")
								@Override
								public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
									if ((Integer) view.getTag() < path.size())
										icon[(Integer) view.getTag()] = new BitmapDrawable(loadedImage);
								}
							});

							holder.fileicon.setTag(position);
						}
						else holder.fileicon.setImageResource(Scroll_Image);
					}
				}
			}

			holder.filename.setText(filename);
			holder.filedate.setText(filedate);
			holder.filesize.setText(filesize);
			holder.check.setVisibility(isSelected[position]);
			return convertView;
		}

		class ViewHolder {

			ImageView fileicon;
			TextView filename;
			TextView filedate;
			TextView filesize;
			ImageView check;
		}
	}

	public class RootFileAdapter extends BaseAdapter {

		private ArrayList<RootFileProperty> object;
		boolean isScrolling = false;
		String filename, filedate, fileperm, filesize;
		String file, mimeType;
		String s;
		ViewHolder holder;
		Drawable dr;

		public RootFileAdapter(ArrayList<RootFileProperty> object) {
			super();
			this.object = object;
			loader = new ApkLoader(getApplicationContext());
			s = nowPath.endsWith("/") ? nowPath : nowPath + "/";
		}

		@Override
		public int getCount() {
			return object.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
				row = inflater.inflate(R.layout.rootrow, parent, false);
				holder = new ViewHolder();

				// Find View
				holder.fileicon = (ImageView) row.findViewById(R.id.icon);
				holder.filename = (TextView) row.findViewById(R.id.filename);
				holder.filedate = (TextView) row.findViewById(R.id.filedate);
				holder.fileperm = (TextView) row.findViewById(R.id.fileperm);
				holder.filesize = (TextView) row.findViewById(R.id.filesize);
				holder.check = (ImageView) row.findViewById(R.id.check);
				holder.check.setBackgroundResource(Check);
				row.setTag(holder);
			}
			else holder = (ViewHolder) row.getTag();

			filename = object.get(position).getName();
			filedate = object.get(position).getDate();
			fileperm = object.get(position).getIntPerm();
			filesize = object.get(position).getSize();

			// Set Resources
			String dir = s + filename;

			if (internal_icon[position] > 0) holder.fileicon.setImageResource(internal_icon[position]);
			else if (icon[position] != null) holder.fileicon.setImageDrawable(icon[position]);
			else {
				if (internal_icon[position] == Apk) {
					if (isScrolling) holder.fileicon.setImageResource(R.drawable.android);
					else loader.DisplayImage(object.get(position).getName(), dir, holder.fileicon, position);
				}
				else if (internal_icon[position] == Image) {
					if (isScrolling) holder.fileicon.setImageResource(Scroll_Image);
					else {
						if (UseImageLoader) {
							holder.fileicon.setImageResource(Scroll_Image);
							ImageLoader.getInstance().displayImage("file:/" + dir, holder.fileicon, new SimpleImageLoadingListener() {

								@SuppressWarnings("deprecation")
								@Override
								public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
									if ((Integer) view.getTag() < path.size())
										icon[(Integer) view.getTag()] = new BitmapDrawable(loadedImage);
								}
							});

							holder.fileicon.setTag(position);
						}
						else holder.fileicon.setImageResource(Scroll_Image);
					}
				}
			}

			holder.filename.setText(filename);
			holder.filedate.setText(filedate);
			holder.fileperm.setText(fileperm);
			holder.filesize.setText(filesize);
			holder.check.setVisibility(isSelected[position]);
			return row;
		}

		class ViewHolder {

			ImageView fileicon;
			TextView filename;
			TextView filedate;
			TextView fileperm;
			TextView filesize;
			ImageView check;
		}
	}

	public class DrawerAdapter extends BaseAdapter {

		private ArrayList<String> object;
		ViewHolder holder;

		public DrawerAdapter(ArrayList<String> object) {
			super();
			this.object = object;
		}

		@Override
		public int getCount() {
			return object.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
				row = inflater.inflate(R.layout.menurow, parent, false);
				holder = new ViewHolder();

				// Find View
				holder.icon = (ImageView) row.findViewById(R.id.icon);
				holder.name = (TextView) row.findViewById(R.id.name);
				row.setTag(holder);
			}
			else holder = (ViewHolder) row.getTag();

			holder.name.setText(object.get(pos));

			Drawable dr = null;
			if (pos == 0) dr = res.getDrawable(Folder);
			else if (pos == 1) dr = res.getDrawable(Others);
			if (appTheme.equals("Dark")) {
				if (pos == 2) dr = res.getDrawable(R.drawable.storage_icon_ondark);
				else if (pos == 3) dr = res.getDrawable(R.drawable.refresh_icon_ondark);
				else if (pos == 4) dr = res.getDrawable(R.drawable.search_icon_ondark);
				else if (pos == 5 && !isRoot) dr = res.getDrawable(R.drawable.settings_icon_ondark);
				else if (pos == 5 && isRoot) dr = res.getDrawable(R.drawable.reboot_icon_ondark);
				else if (pos == 6) dr = res.getDrawable(R.drawable.settings_icon_ondark);
			}
			else {
				if (pos == 2) dr = res.getDrawable(R.drawable.storage_icon_onlight);
				else if (pos == 3) dr = res.getDrawable(R.drawable.refresh_icon_onlight);
				else if (pos == 4) dr = res.getDrawable(R.drawable.search_icon_onlight);
				else if (pos == 5 && !isRoot) dr = res.getDrawable(R.drawable.settings_icon_onlight);
				else if (pos == 5 && isRoot) dr = res.getDrawable(R.drawable.reboot_icon_onlight);
				else if (pos == 6) dr = res.getDrawable(R.drawable.settings_icon_onlight);
			}
			holder.icon.setImageDrawable(dr);
			return row;
		}

		class ViewHolder {

			ImageView icon;
			TextView name;
		}
	}

	public class ApkLoader {

		public Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
		ExecutorService executorService;
		boolean isScrolling = false;

		public ApkLoader(Context context) {
			executorService = Executors.newFixedThreadPool(5);
		}

		final int stub_id = R.drawable.android;

		public void DisplayImage(String url, String b, ImageView imageView, int pos) {
			imageViews.put(imageView, url);
			queuePhoto(url, b, imageView, pos);
			imageView.setImageResource(stub_id);
		}

		public void queuePhoto(String url, String b, ImageView imageView, int pos) {
			PhotoToLoad p = new PhotoToLoad(url, b, imageView, pos);
			executorService.submit(new PhotosLoader(p));
		}

		// Task for the queue
		public class PhotoToLoad {

			public String url;
			public ImageView imageView;
			public String b;
			public int pos;

			public PhotoToLoad(String u, String bmp, ImageView i, int p) {
				url = u;
				b = bmp;
				imageView = i;
				pos = p;
			}
		}

		class PhotosLoader implements Runnable {

			PhotoToLoad photoToLoad;

			PhotosLoader(PhotoToLoad photoToLoad) {
				this.photoToLoad = photoToLoad;
			}

			@Override
			public void run() {
				if (isScrolling) return;
				if (imageViewReused(photoToLoad)) return;
				icon[photoToLoad.pos] = getApkIcon(photoToLoad.b);
				if (icon[photoToLoad.pos].getClass() == NinePatchDrawable.class
						|| icon[photoToLoad.pos].getClass() == StateListDrawable.class)
					icon[photoToLoad.pos] = res.getDrawable(R.drawable.android);

				Bitmap bmp = ((BitmapDrawable) icon[photoToLoad.pos]).getBitmap();
				if (imageViewReused(photoToLoad)) return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				Activity a = (Activity) photoToLoad.imageView.getContext();
				a.runOnUiThread(bd);
			}
		}

		boolean imageViewReused(PhotoToLoad photoToLoad) {
			String tag = imageViews.get(photoToLoad.imageView);
			if (tag == null || !tag.equals(photoToLoad.url)) return true;
			return false;
		}

		// Used to display bitmap in the UI thread
		class BitmapDisplayer implements Runnable {

			Bitmap bitmap;
			PhotoToLoad photoToLoad;

			public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
				bitmap = b;
				photoToLoad = p;
			}

			public void run() {
				if (imageViewReused(photoToLoad)) return;
				if (bitmap != null) photoToLoad.imageView.setImageBitmap(bitmap);
				else photoToLoad.imageView.setImageResource(stub_id);
			}
		}
	}
}