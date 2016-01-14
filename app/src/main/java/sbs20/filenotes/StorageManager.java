package sbs20.filenotes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class StorageManager {

    private FilenotesApplication application;

    private String getFilepath(String filename) {
        return this.getStorageDirectory().getAbsolutePath() + "/" + filename;
    }

    private String getFilepath(Note note) {
        return this.getFilepath(note.getName());
    }

    private File getFile(Note note) {
        return new File(this.getFilepath(note));
    }

    public StorageManager(FilenotesApplication application) {
        this.application = application;
    }

    private File getStorageDirectory() {
        String directoryPath = this.application
                .getPreferences()
                .getString(PreferenceSettingsActivity.KEY_STORAGE_DIRECTORY, "");

        return new File(directoryPath);
    }

    public static String readFileAsString(File file) {
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

        } finally {

        }

        return stringBuffer.toString();
    }

    public NoteCollection readAllFromStorage() {

        NoteCollection notes = new NoteCollection();

        if (this.getStorageDirectory().exists()) {
            if (this.getStorageDirectory().isDirectory()) {

                File[] files = this.getStorageDirectory().listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.canRead() && file.isFile()) {
                            Locale locale = Locale.getDefault();
                            String filename = file.getName().toLowerCase(locale);
                            if (filename.endsWith(".txt")) {
                                return true;
                            }
                        }

                        return false;
                    }
                });

                for (int index = 0; index < files.length; index++) {
                    notes.add(Note.FromFile(files[index]));
                }

                notes.sort();
            }
        }

        return notes;
    }

    public void writeToStorage(Note note) {
        try {
            FileWriter fileWriter = new FileWriter(this.getFilepath(note));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(note.getText());
            bufferedWriter.close();
            fileWriter.close();
            this.application.toast("Saved");
            note.reset();
        } catch (IOException ex) {
            this.application.toast(ex.toString());
        }
    }

    public void deleteNote(Note note) {
        File file = new File(this.getFilepath(note));
        if (file.exists()) {
            file.delete();
        }
    }

    public boolean renameNote(Note note, String desiredName) {
        File desiredFile = new File(this.getFilepath(desiredName));
        if (desiredFile.exists()) {
            return false;
        }

        File file = this.getFile(note);
        boolean succeeded = file.renameTo(desiredFile);
        if (succeeded) {
            note.setName(desiredName);
        }

        return succeeded;
    }
}
