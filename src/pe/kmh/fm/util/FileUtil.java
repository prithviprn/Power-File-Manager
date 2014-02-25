package pe.kmh.fm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pe.kmh.fm.prop.RootFile;

import android.webkit.MimeTypeMap;

import com.stericson.RootTools.RootTools;

public class FileUtil {

	public static boolean NormalFileCopy(final File from, final File to) throws IOException {
		if (from.getParent().equals(to.getAbsolutePath())) return false;
		if (from.isDirectory()) {
			File TargetFolder = new File(to, from.getName());
			if (!TargetFolder.exists()) TargetFolder.mkdirs();
			String[] children = from.list();
			for (int i = 0; i < children.length; i++)
				NormalFileCopy(new File(from, children[i]), new File(TargetFolder.toString()));
		}
		else {
			String s = from.getName();
			File Target = makeFile(to, s);
			if (Target == null) return false;
			InputStream in;
			try {
				in = new FileInputStream(from);
				OutputStream out = new FileOutputStream(Target);

				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
					out.write(buf, 0, len);
				in.close();
				out.close();
			}
			catch (Exception e) {
			}
		}

		return true;
	}

	public static void RootFileCopy(final RootFile from, final RootFile to) {
		if (from.canRead() && to.canWrite()) {
			try {
				NormalFileCopy((File) from, (File) to);
			}
			catch (Exception e) {
			}
		}
		
		RootTools.remount(to.getAbsolutePath(), "rw");
		final String from_path = "\"" + from.getAbsolutePath() + "\"";
		final String to_path = "\"" + to.getAbsolutePath() + "\"";
		if (from.isFile()) RootTools.copyFile(from_path, to_path, false, true);
		else if (from.isDirectory()) {
			RootFile files[] = from.listFiles();
			String dir = "\"" + to.getAbsolutePath()
					+ from.getAbsolutePath().substring(from.getAbsolutePath().lastIndexOf("/"), from.getAbsolutePath().length()) + "\"";
			int len = files.length;

			new RootFile(dir.substring(1, dir.length() - 1)).mkdirs();

			for (int i = 0; i < len; i++) {
				if ((new RootFile(from.getAbsolutePath() + "/" + files[i].getName())).isDirectory()) RootFileCopy(
						new RootFile(from.getAbsolutePath() + "/" + files[i].getName()), new RootFile(dir.substring(1, dir.length() - 1)));

				else RootTools.copyFile("\"" + from.getAbsolutePath() + "/" + files[i].getName() + "\"", dir, false, true);
			}
		}
	}

	public static File makeFile(File dir, String file_path) {
		File file = null;
		if (dir.isDirectory()) {
			file = new File(dir.toString(), file_path);
			if (file != null && !file.exists() && !file.isDirectory()) {
				try {
					file.createNewFile();
				}
				catch (IOException e) {
				}
			}
		}
		return file;
	}

	public static void DeleteFile(String dir) {
		File fileOrDirectory = new File(dir);
		if (fileOrDirectory.isDirectory()) for (File child : fileOrDirectory.listFiles())
			DeleteFile(child.toString());

		fileOrDirectory.delete();
	}

	public static String formatFileSize(long size) {
		if (size == -1) return new String("[Link]");
		if (size < 1024) return new Long(size).toString() + " bytes";
		else if (size < 1024 * 1024) {
			String t = String.format("%.2f KB", (double) size / 1024);
			return t;
		}
		else if (size < 1024 * 1024 * 1024) {
			String t = String.format("%.2f MB", (double) size / 1024 / 1024);
			return t;
		}
		else {
			String t = String.format("%.2f GB", (double) size / 1024 / 1024 / 1024);
			return t;
		}
	}

	public static String getExtension(File file) {
		String name = file.getName();
		return getExtension(name);
	}

	public static String getExtension(String name) {
		int length = name.length() - 1;
		if (length < 0) return "";
		StringBuilder sb = new StringBuilder();

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
		return extension;
	}

	public static String getMIME(String ext) {
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
	}

	public static int calcPerm(String perm) {
		if (perm.length() < 9) return 0;
		int ret = 0;
		if (perm.charAt(0) == 'r') ret += 400;
		if (perm.charAt(1) == 'w') ret += 200;
		if (perm.charAt(2) == 'x') ret += 100;
		if (perm.charAt(3) == 'r') ret += 40;
		if (perm.charAt(4) == 'w') ret += 20;
		if (perm.charAt(5) == 'x') ret += 10;
		if (perm.charAt(6) == 'r') ret += 4;
		if (perm.charAt(7) == 'w') ret += 2;
		if (perm.charAt(8) == 'x') ret += 1;

		return ret;
	}
}