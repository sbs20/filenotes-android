package sbs20.filenotes.model;

import sbs20.filenotes.FilenotesApplication;

public class NotesManager {

    private FilenotesApplication application;
	private Note selectedNote;
	private NoteCollection notes;

    public NotesManager(FilenotesApplication application) {
        this.application = application;
    }

	public void setSelectedNote(Note file) {
		this.selectedNote = file;
	}
	public Note getSelectedNote() {
		return this.selectedNote;
	}

	public NoteCollection getNotes() {
		if (this.notes == null) {
            this.notes = new NoteCollection();
		}

		return this.notes;
	}
}
