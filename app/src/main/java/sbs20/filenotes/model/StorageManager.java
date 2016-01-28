package sbs20.filenotes.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import sbs20.filenotes.FilenotesApplication;
import sbs20.filenotes.SettingsPreferenceActivity;

public class StorageManager {

    private FilenotesApplication application;

    private String getFilepath(String filename) {
        return this.getStorageDirectory().getAbsolutePath() + "/" + filename;
    }

    private File getFile(String name) {
        return new File(this.getFilepath(name));
    }

    public StorageManager(FilenotesApplication application) {
        this.application = application;
    }

    private File getStorageDirectory() {
        String directoryPath = this.application
                .getPreferences()
                .getString(SettingsPreferenceActivity.KEY_STORAGE_DIRECTORY, "");

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
}
