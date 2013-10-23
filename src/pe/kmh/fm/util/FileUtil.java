package pe.kmh.fm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pe.kmh.fm.prop.RootFile;

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
}