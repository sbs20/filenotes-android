package com.sbs20.androsync;

import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FileSystemService implements IDirectoryProvider {

    private static final int BUFFER_SIZE = 1024;
//    private SyncContext syncContext;
//    private File storageDirectory;

    public FileSystemService() {

//        this.syncContext = syncContext;
//        this.storageDirectory = new File(this.syncContext.getLocalStoragePath());
    }

    public File getFile(String path) {
        return new File(path);
    }

    public boolean filesEqual(File file1, File file2) {

        // Quickly check size...
        if (file1.length() != file2.length()) {
            return false;
        }

        try {
            FileInputStream reader1 = new FileInputStream(file2);
            FileInputStream reader2 = new FileInputStream(file2);

            byte[] buffer1 = new byte[BUFFER_SIZE];
            byte[] buffer2 = new byte[BUFFER_SIZE];

            int read1 = reader1.read(buffer1);
            int read2 = reader2.read(buffer2);

            while (read1 != -1) {

                if (read1 != read2) {
                    // I can't understand how this would happen given the files are the same size
                    // but let's be really pessimistic
                    return false;
                }

                for (int index = 0; index < read1; index++) {
                    if (buffer1[index] != buffer2[index]) {
                        return false;
                    }
                }

                read1 = reader1.read(buffer1);
                read2 = reader2.read(buffer2);
            }

            reader1.close();
            reader2.close();
        } catch (IOException e) {
            Logger.error(this, e.toString());
        } finally {
        }

        // If we made it all the way here...
        return true;
    }

    public List<File> readAllFilesFromStorage(String path) {
        List<File> files = new ArrayList<>();

        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            File[] array = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.canRead() && file.isFile();
                }
            });

            for (File file : array) {
                files.add(file);
            }
        }

        return files;
    }

    public void write(String path, byte[] data) throws IOException {
//        try {
            FileOutputStream fileWriter = new FileOutputStream(path);
            BufferedOutputStream bufferedWriter = new BufferedOutputStream(fileWriter);
            bufferedWriter.write(data);
            bufferedWriter.close();
            fileWriter.close();
//            ServiceManager.getInstance().toast("Saved");
//        } catch (IOException ex) {
//            Logger.error(this, ex.toString());
//        }
    }

    public void delete(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public void copy(String sourcePath, String destinationPath) {
        File src = this.getFile(sourcePath);
        File dst = this.getFile(destinationPath);

        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
        } catch (IOException ex) {
            Logger.error(this, ex.toString());
        }
    }

    public boolean rename(String path, String desiredPath) {
        File desiredFile = new File(desiredPath);
        if (desiredFile.exists()) {
            return false;
        }

        // Do a copy and delete rather than rename. Rename doesn't tickle
        // the file's lastModified and makes replication bad.
        this.copy(path, desiredPath);
        this.delete(path);

        File old = this.getFile(path);
        return (!old.exists() && desiredFile.exists());
    }

    public boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }

    @Override
    public boolean directoryExists(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    @Override
    public List<String> getChildDirectoryPaths(String path) {
        File directory = new File(path);

        File[] dirs = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
            return pathname.isDirectory();
            }
        });

        List<String> list = new ArrayList<>();

        // Add the parent first
        if (directory.getParentFile() != null) {
            list.add(directory.getParentFile().getAbsolutePath());
        }

        // Now add the children (sorted)
        if (dirs != null) {
            Arrays.sort(dirs, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    Locale locale = Locale.getDefault();
                    String l = lhs.getAbsolutePath().toLowerCase(locale);
                    String r = rhs.getAbsolutePath().toLowerCase(locale);
                    return l.compareTo(r);
                }
            });

            for (File f : dirs) {
                list.add(f.getAbsolutePath());
            }
        }

        return list;
    }

    @Override
    public String getRootDirectoryPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public void createDirectory(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static String fileToString(File file, int length) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            FileReader reader = new FileReader(file);
            char[] buffer = new char[BUFFER_SIZE];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                stringBuffer.append(buffer, 0, read);
                if (length > -1 && stringBuffer.length() > length) {
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            Logger.error(FileSystemService.class, e.toString());
        } finally {
        }

        if (length > -1 && stringBuffer.length() > length) {
            return stringBuffer.substring(0, length);
        }

        return stringBuffer.toString();
    }

    public static String fileToString(File file) {
        return fileToString(file, -1);
    }
}
