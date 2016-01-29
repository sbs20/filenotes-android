package sbs20.filenotes.storage;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import sbs20.filenotes.FilenotesApplication;
import sbs20.filenotes.SettingsPreferenceActivity;

public class FileSystemManager implements IDirectoryListProvider {

    private FilenotesApplication application;

    private String getFilepath(String filename) {
        return this.getStorageDirectory().getAbsolutePath() + "/" + filename;
    }

    private File getFile(String name) {
        return new File(this.getFilepath(name));
    }

    public FileSystemManager(FilenotesApplication application) {
        this.application = application;
    }

    private File getStorageDirectory() {
        String directoryPath = this.application.getSettings().getStorageDirectory();
        return new File(directoryPath);
    }

    public String readFileAsString(File file) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            FileReader reader = new FileReader(file);
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                stringBuffer.append(buffer, 0, read);
            }
            reader.close();
        } catch (IOException e) {
            this.application
                    .getLogger()
                    .error(this, e.toString());

        } finally {

        }

        return stringBuffer.toString();
    }

    public File[] readAllFilesFromStorage() {
        if (this.getStorageDirectory().exists()) {
            if (this.getStorageDirectory().isDirectory()) {
                return this.getStorageDirectory().listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.canRead() && file.isFile()) {
                            // TODO move these to preferences
                            boolean showAllFiles = true;
                            boolean showHiddenFiles = false;

                            Locale locale = Locale.getDefault();
                            String filename = file.getName().toLowerCase(locale);

                            if (filename.startsWith(".") && !showHiddenFiles) {
                                return false;
                            }

                            if (!filename.endsWith(".txt") && !showAllFiles) {
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
            this.application.toast("Saved");
        } catch (IOException ex) {
            this.application.getLogger().error(this, ex.toString());
        }
    }

    public void delete(String name) {
        File file = new File(this.getFilepath(name));
        if (file.exists()) {
            file.delete();
        }
    }

    public boolean rename(String name, String desiredName) {
        File desiredFile = new File(this.getFilepath(desiredName));
        if (desiredFile.exists()) {
            return false;
        }

        File file = this.getFile(name);
        return file.renameTo(desiredFile);
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
