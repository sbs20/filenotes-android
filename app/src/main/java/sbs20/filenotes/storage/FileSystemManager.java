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

public class FileSystemManager implements IDirectoryListProvider {

    private String getFilepath(String filename) {
        return this.getStorageDirectory().getAbsolutePath() + "/" + filename;
    }

    public File getFile(String name) {
        return new File(this.getFilepath(name));
    }

    public FileSystemManager() {
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

    public File[] readAllFilesFromStorage() {
        final Settings settings = ServiceManager.getInstance().getSettings();
        if (this.getStorageDirectory().exists()) {
            if (this.getStorageDirectory().isDirectory()) {
                return this.getStorageDirectory().listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.canRead() && file.isFile()) {
                            Locale locale = Locale.getDefault();
                            String filename = file.getName().toLowerCase(locale);

                            if (filename.startsWith(".") && settings.excludeHiddenFile()) {
                                return false;
                            }

                            if (!filename.endsWith(".txt") && settings.excludeNonTextFile()) {
                                return false;
                            }

                            return true;
                        }

                        return false;
                    }
                });
            }
        }

        return new File[0];
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
