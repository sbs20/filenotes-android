package sbs20.filenotes.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import sbs20.filenotes.Current;
import sbs20.filenotes.FilenotesApplication;
import sbs20.filenotes.SettingsPreferenceActivity;

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

    private File[] readAllFilesFromStorage() {
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

    private void mergeFileIntoNote(File file, Note note) {
        note.setText(this.readFileAsString(file));
        note.setLastModified(new Date(file.lastModified()));
        note.reset();
    }

    private boolean fileArrayContainsName(File[] files, String name) {
        for (File file : files) {
            if (name.compareTo(file.getName()) == 0) {
                return true;
            }
        }

        return false;
    }

    public void readAllFromStorage() {

        NoteCollection notes = Current.getNotes();

        File[] files = this.readAllFilesFromStorage();

        // Ensure all files are in notes and up to date
        for (File file : files) {
            Note note = notes.getByName(file.getName());
            if (note == null) {
                note = new Note();
                note.setName(file.getName());
                notes.add(note);
            }

            this.mergeFileIntoNote(file, note);
        }

        // Now ensure that any notes NOT in a file is removed
        for (int index = 0; index < notes.size(); index++) {
            if (!fileArrayContainsName(files, notes.get(index).getName())) {
                notes.remove(index);
                index--;
            }
        }

        notes.sort();
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
            this.application.getLogger().error(this, ex.toString());
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

    public boolean isStored(Note note) {
        File file = new File(this.getFilepath(note));
        return file.exists();
    }
}
