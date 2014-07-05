package pe.kmh.fm.prop;

import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pe.kmh.fm.util.FileUtil;

@SuppressWarnings("serial")
public class RootFile extends File {

    private static StringBuilder sb = new StringBuilder(200);
    private ArrayList<String> outLines;
    private ArrayList<String> perms = new ArrayList<String>();
    private ArrayList<Integer> size = new ArrayList<Integer>();
    private String mPath;
    private Long mSize = null;
    private Command cmd;
    private Command command;

    public RootFile(String path) {
        super(path);

        try {
            mPath = path.substring(0, path.lastIndexOf("/"));
            if (!mPath.endsWith("/")) mPath += "/";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RootFile(File f) {
        super(f.getAbsolutePath());

        try {
            String path = f.getAbsolutePath();
            mPath = path.substring(0, path.lastIndexOf("/"));
            if (!mPath.endsWith("/")) mPath += "/";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canRead() {
        // Always TRUE
        return true;
    }

    @Override
    public boolean canWrite() {
        // Always TRUE
        return true;
    }

    @Override
    public boolean delete() {
        File f = new File(this.getPath());
        if (f.canWrite()) {
            try {
                FileUtil.DeleteFile(this.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        boolean result = true;
        RootTools.remount(this.getPath(), "rw");
        String w = "busybox rm -r '" + this.getPath() + "'";
        outLines = new ArrayList<String>();
        try {
            cmd = new Command(0, w) {

                @Override
                public void output(int id, String line) {
                    if (line.contains("\n")) {
                        for (String s : line.split("\n"))
                            output(id, s);
                    } else outLines.add(line);
                }
            };

            RootTools.getShell(true).add(cmd).waitForFinish();
            for (String line : outLines) {
                if (line.contains("failed") || line.contains("can't remove")) result = false;
            }
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean exists() {
        return RootTools.exists(this.getPath());
    }

    @Override
    public boolean isDirectory() {
        boolean result = super.isDirectory();
        if (result == true) return result;
        if (this.getPath().startsWith("/data/media/")) result = true;
        return result;
    }

    @Override
    public long length() {
        if (mSize != null) return mSize;
        else return 0;
    }

    @Override
    public RootFile[] listFiles() {
        final ArrayList<RootFile> files = new ArrayList<RootFile>();

        if (!this.isDirectory()) return null;
        String actualPath = this.getPath().endsWith("/") ? this.getPath() : this.getPath() + "/";

        File[] flist;
        RootFile[] retlist = null;
        if (new File(actualPath).canRead()) { // Not Root Area
            flist = (new File(actualPath)).listFiles();
            if (flist == null) return null;
            ArrayList<File> arr_flist = new ArrayList<File>();
            if (flist != null) {
                for (File f : flist) {
                    arr_flist.add(f);
                }
            }
            Comparator<File> sort = new Comparator<File>() {

                @Override
                public int compare(File a, File b) {
                    return a.getName().compareTo(b.getName());
                }
            };

            Collections.sort(arr_flist, sort);

            retlist = new RootFile[flist.length];
            for (int i = 0; i < flist.length; i++) {
                if (arr_flist.get(i) != null) {
                    retlist[i] = new RootFile(arr_flist.get(i));
                    if (arr_flist.get(i).isDirectory()) size.add(-1);
                    else size.add(Integer.valueOf((int) (arr_flist.get(i).length())));
                }
            }
        }

        final String w = "busybox ls -la '" + actualPath + "'";

        outLines = new ArrayList<String>();
        try {
            cmd = new Command(0, w) {

                @Override
                public void output(int id, String line) {
                    if (line.contains("\n")) {
                        for (String s : line.split("\n"))
                            output(id, s);
                    } else outLines.add(line);
                }
            };

            RootTools.getShell(true).add(cmd).waitForFinish();

            for (String line : outLines) {
                if (line.startsWith("total")) continue;
                String nline = "";
                Matcher matcher = Pattern.compile("\\S+").matcher(line);
                String[] s = new String[30];

                int i = 0;
                while (matcher.find()) {
                    if (i == 8 && !s[0].startsWith("c") && !s[0].startsWith("b")) {
                        s[i++] = line.substring(matcher.start());
                        if (line.contains("->")) s[8] = s[8].split(" ->")[0];
                    } else if (i == 9 && (s[0].startsWith("c") || s[0].startsWith("b"))) {
                        s[i++] = line.substring(matcher.start());
                        if (line.contains("->")) s[9] = s[9].split(" ->")[0];
                    } else s[i++] = matcher.group();
                }

                if (s[8] == null || s[8].equals(".") || s[8].equals("..") || s[8].equals(""))
                    continue;
                if (new File(actualPath).canRead()) {
                    perms.add(s[0].substring(1));
                    if (!s[0].startsWith("-")) size.set(perms.size() - 1, -1);
                    continue;
                }

                nline = s[8];
                if (s[0].startsWith("c") || s[0].startsWith("b")) nline = s[9];

                sb.setLength(0);
                files.add(new RootFile(sb.append(actualPath).append(nline).toString()));
                perms.add(s[0].substring(1));
                if (s[0].startsWith("-")) {
                    size.add(Integer.parseInt(s[4]));
                } else size.add(-1);
            }
        } catch (Exception e) {
            Log.e("PFM", e.getMessage());
        }

        if (new File(actualPath).canRead()) return retlist;

        return files.toArray(new RootFile[0]);
    }

    public String[] listPerms() {
        return perms.toArray(new String[0]);
    }

    public Integer[] listSizes() {
        return size.toArray(new Integer[0]);
    }

    @Override
    public boolean mkdir() {
        boolean result = true;
        RootTools.remount(this.getPath(), "rw");
        String w = "busybox mkdir '" + this.getPath() + "' && chmod 777 '" + this.getPath() + "'";
        outLines = new ArrayList<String>();
        try {
            cmd = new Command(0, w) {

                @Override
                public void output(int id, String line) {
                    if (line.contains("\n")) {
                        for (String s : line.split("\n"))
                            output(id, s);
                    } else outLines.add(line);
                }
            };

            RootTools.getShell(true).add(cmd).waitForFinish();
            for (String line : outLines) {
                if (line.contains("failed") || line.contains("can't create")) result = false;
            }

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean createNewFile() {
        File f = new File(this.getPath());
        if ((new File(this.getParent())).canWrite()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
            }
            return true;
        }

        boolean result = true;

        RootTools.remount(this.getPath(), "rw");
        String w = "busybox touch \"" + this.getPath() + "\" && chmod 777 \"" + this.getPath() + "\"";
        outLines = new ArrayList<String>();
        try {
            cmd = new Command(0, w) {

                @Override
                public void output(int id, String line) {
                    if (line.contains("\n")) {
                        for (String s : line.split("\n"))
                            output(id, s);
                    } else outLines.add(line);
                }
            };

            RootTools.getShell(true).add(cmd).waitForFinish();
            for (String line : outLines) {
                if (line.contains("failed") || line.contains("can't create")) result = false;
            }

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean renameTo(File dest) {
        File f = new File(this.getPath());
        if ((new File(this.getParent())).canWrite()) {
            try {
                f.renameTo(dest);
            } catch (Exception e) {
            }
            return true;
        }

        boolean result = true;
        RootTools.remount(this.getPath(), "rw");
        String w = "busybox mv '" + this.getPath() + "' '" + dest.getPath() + "'";
        outLines = new ArrayList<String>();
        try {
            cmd = new Command(0, w) {

                @Override
                public void output(int id, String line) {
                    if (line.indexOf("\n") > -1) {
                        for (String s : line.split("\n"))
                            output(id, s);
                    } else outLines.add(line);
                }
            };

            RootTools.getShell(true).add(cmd).waitForFinish();
            for (String line : outLines) {
                if (line.contains("failed") || line.contains("can't rename")) result = false;
            }

        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return result;
    }
}
