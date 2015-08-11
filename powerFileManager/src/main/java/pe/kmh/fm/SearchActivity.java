package pe.kmh.fm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pe.kmh.fm.prop.SearchedFileProperty;

public class SearchActivity extends ActionBarActivity {

	ArrayList<SearchedFileProperty> filelist;
	ArrayList<Integer> icon = new ArrayList<Integer>();

	Resources res;
	int Folder = R.drawable.folder;
	int Others = R.drawable.others;
	int Audio = R.drawable.audio;
	int Compressed = R.drawable.compressed;
	int Video = R.drawable.video;
	int PPT = R.drawable.ppt;
	int Word = R.drawable.word;
	int Excel = R.drawable.excel;
	int HTML = R.drawable.html;
	int PDF = R.drawable.pdf;
	int TXT = R.drawable.txt;
	int Scroll_Image = R.drawable.image;
	int Apk = -1;
	int Image = -2;

	@SuppressLint("InlinedApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search);

		getSupportActionBar().setTitle(R.string.Search);

		Intent intent = this.getIntent();
		filelist = intent.getParcelableArrayListExtra("filelist");

		res = getResources();

		final int psize = filelist != null ? filelist.size() : 0;
		Thread getIcons = new Thread() {

			public void run() {
				String file, mimeType, ico;
				for (int i = 0; i < psize; i++) {
					ico = filelist.get(i).getIcon();
					if (ico.equals("FOLDER")) icon.set(i, Folder);
					else {
						file = ico;
						mimeType = getMIME(file);
						if (ico.equals("zip") || ico.equals("7z") || ico.equals("rar") || ico.equals("tar"))
							icon.add(Compressed);
						else if (mimeType == null) icon.add(Others);
						else if (mimeType.startsWith("image")) icon.add(Image);
						else if (mimeType.startsWith("audio")) icon.add(Audio);
						else if (mimeType.startsWith("video")) icon.add(Video);
						else if (ico.equals("doc") || ico.equals("docx")) icon.add(Word);
						else if (ico.equals("ppt") || ico.equals("pptx")) icon.add(PPT);
						else if (ico.equals("xls") || ico.equals("xlsx")) icon.add(Excel);
						else if (ico.equals("pdf")) icon.add(PDF);
						else if (ico.equals("txt")) icon.add(TXT);
						else if (ico.equals("html")) icon.add(HTML);
						else if (ico.equals("apk")) icon.add(Apk);
						else if (!ico.equals("apk")) icon.add(Others);
					}
				}
			}
		};

		runOnUiThread(getIcons);

		FileAdapter adapter = new FileAdapter(filelist);
		final ListView list = (ListView) findViewById(R.id.SearchList);

		list.setOnScrollListener(new OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState != SCROLL_STATE_IDLE)
					((FileAdapter) list.getAdapter()).isScrolling = true;
				else {
					((FileAdapter) list.getAdapter()).isScrolling = false;
					((FileAdapter) list.getAdapter()).notifyDataSetChanged();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		ImageLoaderConfiguration iConfig = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		ImageLoader.getInstance().init(iConfig);

		list.setScrollingCacheEnabled(true);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@SuppressWarnings("rawtypes")
			@Override
			public void onItemClick(AdapterView parent, View view, final int position, long id) {
				final File file = new File(filelist.get(position).getPath());

				String name = file.getName();
				int length = name.length() - 1;
				StringBuffer ext = new StringBuffer();
				while (true) {
					if (name.charAt(length) != 46) ext.append(name.charAt(length--));
					else break;
					if (length <= 0) break;
				}

				StringBuffer temp = new StringBuffer();
				if (ext != null) temp = ext.reverse();
				String extension = temp.toString();
				String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
				if (mimeType == null || extension.toLowerCase().equals("xml") || extension.toLowerCase().equals("txt")
						|| runFile(file, mimeType) == false) {
					AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();
					alertDialog.setMessage(getString(R.string.AskOpenWithTextEditor));

					alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.No), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});

					alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Yes), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(SearchActivity.this, TextEditor.class);
							intent.putExtra("filepath", file.getPath());
							startActivity(intent);
						}
					});

					alertDialog.show();
					return;
				}
			}
		});
		list.setAdapter(adapter);
	}

	public boolean runFile(File file, String MimeType) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, MimeType);
		PackageManager packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		boolean isIntentSafe = activities.size() > 0;
		if (isIntentSafe) startActivity(intent);
		else return false;
		return true;
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

	public String getMIME(String ext) {
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
	}

	public class FileAdapter extends BaseAdapter {

		boolean isScrolling;
		String fileicon, filename, filepath, filedate, filesize;
		ApkLoader loader = new ApkLoader();
		private ArrayList<SearchedFileProperty> object;

		public FileAdapter(ArrayList<SearchedFileProperty> object) {
			super();
			this.object = object;
		}

		@Override
		public int getCount() {
			return object == null ? 0 : object.size();
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
				LayoutInflater inflater = LayoutInflater.from(SearchActivity.this);
				convertView = inflater.inflate(R.layout.searchrow, parent, false);
				holder = new ViewHolder();

				// Find View
				holder.fileicon = (ImageView) convertView.findViewById(R.id.icon);
				holder.filename = (TextView) convertView.findViewById(R.id.filename);
				holder.filepath = (TextView) convertView.findViewById(R.id.filepath);
				holder.filedate = (TextView) convertView.findViewById(R.id.filedate);
				holder.filesize = (TextView) convertView.findViewById(R.id.filesize);
				convertView.setTag(holder);
			} else holder = (ViewHolder) convertView.getTag();

			fileicon = object.get(position).getIcon();
			filename = object.get(position).getName();
			filepath = object.get(position).getPath();
			filedate = object.get(position).getDate();
			filesize = object.get(position).getSize();

			// Set Resources
			if (icon.get(position) > 0) holder.fileicon.setImageResource(icon.get(position));
			else {
				if (icon.get(position) == Apk) {
					if (isScrolling) holder.fileicon.setImageResource(R.drawable.android);
					else
						loader.DisplayImage(object.get(position).getName(), object.get(position).getPath(), holder.fileicon);
				} else if (icon.get(position) == Image) {
					if (isScrolling) holder.fileicon.setImageResource(Scroll_Image);
					else
						ImageLoader.getInstance().displayImage("file:/" + object.get(position).getPath(), holder.fileicon);
				}
			}

			holder.filename.setText(filename);
			holder.filepath.setText(filepath);
			holder.filedate.setText(filedate);
			holder.filesize.setText(filesize);
			return convertView;
		}

		class ViewHolder {

			ImageView fileicon;
			TextView filename;
			TextView filepath;
			TextView filedate;
			TextView filesize;
		}
	}

	public class ApkLoader {

		final int stub_id = R.drawable.android;
		public Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
		ExecutorService executorService;

		public ApkLoader() {
			executorService = Executors.newFixedThreadPool(5);
		}

		public void DisplayImage(String url, String b, ImageView imageView) {
			imageViews.put(imageView, url);
			queuePhoto(url, b, imageView);
			imageView.setImageResource(stub_id);
		}

		public void queuePhoto(String url, String b, ImageView imageView) {
			PhotoToLoad p = new PhotoToLoad(url, b, imageView);
			executorService.submit(new PhotosLoader(p));
		}

		boolean imageViewReused(PhotoToLoad photoToLoad) {
			String tag = imageViews.get(photoToLoad.imageView);
			if (tag == null || !tag.equals(photoToLoad.url)) return true;
			return false;
		}

		// Task for the queue
		public class PhotoToLoad {

			public String url;
			public String b;
			public ImageView imageView;

			public PhotoToLoad(String u, String bmp, ImageView i) {
				url = u;
				b = bmp;
				imageView = i;
			}
		}

		class PhotosLoader implements Runnable {

			PhotoToLoad photoToLoad;

			PhotosLoader(PhotoToLoad photoToLoad) {
				this.photoToLoad = photoToLoad;
			}

			@Override
			public void run() {
				if (imageViewReused(photoToLoad)) return;
				Bitmap bmp = ((BitmapDrawable) getApkIcon(photoToLoad.b)).getBitmap();
				if (imageViewReused(photoToLoad)) return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				Activity a = (Activity) photoToLoad.imageView.getContext();
				a.runOnUiThread(bd);
			}
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
