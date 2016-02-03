package sbs20.filenotes.model;

import java.io.File;
import java.util.Date;

import sbs20.filenotes.DateTime;
import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.R;
import sbs20.filenotes.storage.FileSystemManager;

public class NotesManager {
    private FileSystemManager storage;
	private Note selectedNote;
	private NoteCollection notes;
    private boolean isReplicationRequired;

    public NotesManager() {
        this.storage = new FileSystemManager();
        this.notes = new NoteCollection();
        this.isReplicationRequired = false;
    }

	private void setSelectedNote(Note note) {
		this.selectedNote = note;
	}
	public Note getSelectedNote() {
		return this.selectedNote;
	}

	public NoteCollection getNotes() {
		return this.notes;
	}

    private void mergeFileIntoNote(File file, Note note) {
        note.setTextSummary(this.storage.readFileAsString(file, 128));
        note.setSize(file.length());
        note.setLastModified(new Date(file.lastModified()));
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

        Logger.verbose(this, "readAllFromStorage.Start");
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

        Logger.verbose(this, "readAllFromStorage.Finish");
    }

    private void registerUpdate() {
        this.isReplicationRequired = true;
    }

    public boolean isReplicationRequired() {
        Settings settings = ServiceManager.getInstance().getSettings();
        Date lastSync = settings.getLastSync();
        boolean isOverThreshold = DateTime.now().getTime() - lastSync.getTime() > settings.replicationThresholdInMilliseconds();
        return this.isReplicationRequired || isOverThreshold;
    }

    public void setReplicationRequired(boolean value) {
        this.isReplicationRequired = value;
    }

    public void writeToStorage(Note note) {
        this.storage.write(note.getName(), note.getText());
        note.reset();
        this.registerUpdate();
    }

    public void editNote(Note note) {
        File file = this.storage.getFile(note.getName());
        if (file.exists()) {
            note.setText(this.storage.readFileAsString(file));
            note.reset();
        }

        this.setSelectedNote(note);
    }

    public void deleteNote(Note note) {
        this.storage.delete(note.getName());
        this.notes.remove(note);
        this.registerUpdate();
    }

    public boolean renameNote(Note note, String desiredName) {
        boolean succeeded = this.storage.rename(note.getName(), desiredName);
        if (succeeded) {
            note.setName(desiredName);
            this.registerUpdate();
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
        String stem = ServiceManager.getInstance().getContext().getString(R.string.new_note_file_stem);
        Note note = new Note();
        note.setName(this.createUniqueNewName(stem));
        this.notes.add(note);
        return note;
    }
}
