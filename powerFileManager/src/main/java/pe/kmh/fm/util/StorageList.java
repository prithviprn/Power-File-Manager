package pe.kmh.fm.util;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StorageList extends Activity {

    public static String getMicroSDCardDirectory() {
        List<String> mMounts = readMountsFile();
        List<String> mVold = readVoldFile();

        for (int i = 0; i < mMounts.size(); i++) {
            String mount = mMounts.get(i);

            // vold.fstab Check : below Android 4.3
            if (!mVold.contains(mount) && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mMounts.remove(i--);
                continue;
            }

            File root = new File(mount);
            if (!root.exists() || !root.isDirectory()) {
                mMounts.remove(i--);
                continue;
            }

            if (!isAvailableFileSystem(mount)) {
                mMounts.remove(i--);
                continue;
            }

            if (!checkMicroSDCard(mount)) {
                mMounts.remove(i--);
            }
        }

        if (mMounts.size() == 1) {
            return mMounts.get(0);
        } else if (Environment.getExternalStorageDirectory().getAbsolutePath().equals("/storage/sdcard0"))
            return "/storage/sdcard1";
        return null;
    }

    private static List<String> readMountsFile() {
        /**
         * Scan the /proc/mounts file and look for lines like this:
         * /dev/block/vold/179:1 /mnt/sdcard vfat
         * rw,dirsync,nosuid,nodev,noexec,
         * relatime,uid=1000,gid=1015,fmask=0602,dmask
         * =0602,allow_utime=0020,codepage
         * =cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
         *
         * When one is found, split it into its elements and then pull out the
         * path to the that mount point and add it to the arraylist
         */
        List<String> mMounts = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File("/proc/mounts"));

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split("[ \t]+");
                    String element = lineElements[1];

                    mMounts.add(element);
                }
            }

            scanner.close();
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
        return mMounts;
    }

    private static List<String> readVoldFile() {
        /**
         * Scan the /system/etc/vold.fstab file and look for lines like this:
         * dev_mount sdcard /mnt/sdcard 1
         * /devices/platform/s3c-sdhci.0/mmc_host/mmc0
         *
         * When one is found, split it into its elements and then pull out the
         * path to the that mount point and add it to the arraylist
         */

        List<String> mVold = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith("dev_mount")) {
                    String[] lineElements = line.split("[ \t]+");
                    String element = lineElements[2];

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    mVold.add(element);
                }
            }

            scanner.close();
        } catch (Exception e) {
            // Auto-generated catch block
            e.printStackTrace();
        }

        return mVold;
    }

    @SuppressWarnings("deprecation")
    private static boolean checkMicroSDCard(String fileSystemName) {
        StatFs statFs = new StatFs(fileSystemName);

        long totalSize = (long) statFs.getBlockSize() * statFs.getBlockCount();

        if (totalSize < 1073741824) {
            return false;
        }

        return true;
    }

    private static boolean isAvailableFileSystem(String fileSystemName) {
        final String[] unAvailableFileSystemList = {"/dev", "/mnt/asec", "/mnt/obb", "/system", "/data", "/cache", "/efs", "/firmware"};
        for (String name : unAvailableFileSystemList) {
            if (fileSystemName.contains(name) == true) {
                return false;
            }
        }

        if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(fileSystemName) == true)
            return false;

        return true;
    }
}