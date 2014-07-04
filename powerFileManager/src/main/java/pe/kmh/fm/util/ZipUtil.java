package pe.kmh.fm.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

public class ZipUtil {

    public static void zip(File src, OutputStream os, String charsetName, boolean includeSrc) throws IOException {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os);
        zos.setEncoding(charsetName);
        FileInputStream fis;

        int length;
        ZipArchiveEntry ze;
        byte[] buf = new byte[8 * 1024];
        String name;

        Stack<File> stack = new Stack<File>();
        File root;
        if (src.isDirectory()) {
            if (includeSrc) {
                stack.push(src);
                root = src.getParentFile();
            } else {
                File[] fs = src.listFiles();
                for (int i = 0; i < fs.length; i++) {
                    stack.push(fs[i]);
                }
                root = src;
            }
        } else {
            stack.push(src);
            root = src.getParentFile();
        }

        while (!stack.isEmpty()) {
            File f = stack.pop();
            name = toPath(root, f);
            if (f.isDirectory()) {
                File[] fs = f.listFiles();
                for (int i = 0; i < fs.length; i++) {
                    if (fs[i].isDirectory()) stack.push(fs[i]);
                    else stack.add(0, fs[i]);
                }
            } else {
                ze = new ZipArchiveEntry(name);
                zos.putArchiveEntry(ze);
                fis = new FileInputStream(f);
                while ((length = fis.read(buf, 0, buf.length)) >= 0) {
                    zos.write(buf, 0, length);
                }
                fis.close();
                zos.closeArchiveEntry();
            }
        }
        zos.close();
    }

    public static void unzip(File zippedFile, File destDir, String charsetName, String filetype) throws IOException {
        FileInputStream is = new FileInputStream(zippedFile);
        ArchiveEntry entry;
        String name;
        File target;
        int nWritten = 0;
        BufferedOutputStream bos;
        byte[] buf = new byte[1024 * 8];
        destDir.mkdirs();
        ArchiveInputStream ais = null;
        if (filetype.equals("zip")) ais = new ZipArchiveInputStream(is, charsetName, true);
        if (filetype.equals("tar")) ais = new TarArchiveInputStream(is);
        while ((entry = ais.getNextEntry()) != null) {
            name = entry.getName();
            target = new File(destDir, name);
            if (entry.isDirectory()) {
                target.mkdirs();
            } else {
                new File(target.getParent()).mkdirs();
                target.createNewFile();
                bos = new BufferedOutputStream(new FileOutputStream(target));
                while ((nWritten = ais.read(buf)) >= 0) {
                    bos.write(buf, 0, nWritten);
                }
                bos.close();
            }
        }
        ais.close();
    }

    private static String toPath(File root, File dir) {
        String path = dir.getAbsolutePath();
        path = path.substring(root.getAbsolutePath().length()).replace(File.separatorChar, '/');
        if (path.startsWith("/")) path = path.substring(1);
        if (dir.isDirectory() && !path.endsWith("/")) path += "/";
        return path;
    }
}