package sbs20.filenotes.model;

import java.io.File;
import java.util.Date;

import sbs20.filenotes.FilenotesApplication;
import sbs20.filenotes.R;
import sbs20.filenotes.storage.FileSystemManager;

public class NotesManager {

    private FilenotesApplication application;
    private FileSystemManager storage;
	private Note selectedNote;
	private NoteCollection notes;

    public NotesManager(FilenotesApplication application) {
        this.application = application;
        this.storage = new FileSystemManager(application);
        this.notes = new NoteCollection();
    }

	public void setSelectedNote(Note file) {
		this.selectedNote = file;
	}
	public Note getSelectedNote() {
		return this.selectedNote;
	}

	public NoteCollection getNotes() {
		return this.notes;
	}

    private void mergeFileIntoNote(File file, Note note) {
        note.setText(this.storage.readFileAsString(file));
        note.setLastModified(new Date(file.lastModified()));
        note.reset();
    }

    private static boolean fileArrayContainsName(File[] files, String name) {
        for (File file : files) {
            if (name.equals(file.getName())) {
                return true;
            }
        }

        return false;
    }

    public void readAllFromStorage() {
        this.application
                .getLogger()
                .verbose(this, "readAllFromStorage.Start");

        File[] files = this.storage.readAllFilesFromStorage();

        // Ensure all files are in notes and up to date
        for (File file : files) {
            Note note = this.notes.getByName(file.getName());
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

        this.application
                .getLogger()
                .verbose(this, "readAllFromStorage.Finish");
    }

    public void writeToStorage(Note note) {
        this.storage.write(note.getName(), note.getText());
        note.reset();
    }

    public void deleteNote(Note note) {
        this.storage.delete(note.getName());
        this.notes.remove(note);
    }

    public boolean renameNote(Note note, String desiredName) {
        boolean succeeded = this.storage.rename(note.getName(), desiredName);
        if (succeeded) {
            note.setName(desiredName);
        }

        return succeeded;
    }

    public boolean isStored(Note note) {
        return this.storage.exists(note.getName());
    }

    private String createUniqueNewName(String stem) {
        String attempt = String.format(stem, "");
        int i = 0;
        while (this.notes.isExistingName(attempt)) {
            ++i;
            attempt = String.format(stem, i);
        }

        return attempt;
    }

    public Note createNote() {
        String stem = this.application.getString(R.string.new_note_file_stem);
        Note note = new Note();
        note.setName(this.createUniqueNewName(stem));
        this.notes.add(note);
        return note;
    }
}
