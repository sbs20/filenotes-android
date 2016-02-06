package sbs20.filenotes.storage;

import android.os.Environment;

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

import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.Settings;

public class FileSystemService implements IDirectoryListProvider {

    private String getFilepath(String filename) {
        return this.getStorageDirectory().getAbsolutePath() + "/" + filename;
    }

    public File getFile(String name) {
        return new File(this.getFilepath(name));
    }

    public FileSystemService() {
    }

    private File getStorageDirectory() {
        String directoryPath = ServiceManager.getInstance().getSettings().getLocalStoragePath();
        return new File(directoryPath);
    }

    public String readFileAsString(File file, int length) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            FileReader reader = new FileReader(file);
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                stringBuffer.append(buffer, 0, read);
                if (length > -1 && stringBuffer.length() > length) {
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            Logger.error(this, e.toString());
        } finally {
        }

        if (length > -1 && stringBuffer.length() > length) {
            return stringBuffer.substring(0, length);
        }

        return stringBuffer.toString();
    }

    public String readFileAsString(File file) {
        return readFileAsString(file, -1);
    }

    public boolean filesEqual(File file1, File file2) {

        // Quickly check size...
        if (file1.length() != file2.length()) {
            return false;
        }

        try {
            FileInputStream reader1 = new FileInputStream(file2);
            FileInputStream reader2 = new FileInputStream(file2);

            int size = 1024;
            byte[] buffer1 = new byte[size];
            byte[] buffer2 = new byte[size];

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

    public List<File> readAllFilesFromStorage() {
        final Settings settings = ServiceManager.getInstance().getSettings();
        List<File> files = new ArrayList<>();

        if (this.getStorageDirectory().exists()) {
            if (this.getStorageDirectory().isDirectory()) {
                File[] array = this.getStorageDirectory().listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.canRead() && file.isFile();
                    }
                });

                for (File file : array) {
                    files.add(file);
                }
            }
        }

        return files;
    }

    public void write(String name, String text) {
        try {
            FileWriter fileWriter = new FileWriter(this.getFilepath(name));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(text);
            bufferedWriter.close();
            fileWriter.close();
            ServiceManager.getInstance().toast("Saved");
        } catch (IOException ex) {
            Logger.error(this, ex.toString());
        }
    }

    public void delete(String name) {
        File file = new File(this.getFilepath(name));
        if (file.exists()) {
            file.delete();
        }
    }

    public void copy(String srcname, String destname) {
        File src = this.getFile(srcname);
        File dst = this.getFile(destname);

        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
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

    public boolean rename(String name, String desiredName) {
        File desiredFile = new File(this.getFilepath(desiredName));
        if (desiredFile.exists()) {
            return false;
        }

        // Do a copy and delete rather than rename. Rename doesn't tickle
        // the file's lastModified and makes replication bad.
        this.copy(name, desiredName);
        this.delete(name);

        File old = this.getFile(name);
        return (!old.exists() && desiredFile.exists());
    }


    public boolean exists(String name) {
        File file = new File(this.getFilepath(name));
        return file.exists();
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

    public String getRootDirectoryPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }
}
