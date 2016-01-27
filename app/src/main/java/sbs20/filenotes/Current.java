package sbs20.filenotes;

public class Current {

	private static Note selectedNote;
	private static NoteCollection notes;

	public static void setSelectedNote(Note file) {
		Current.selectedNote = file;
	}
	public static Note getSelectedNote() {
		return Current.selectedNote;
	}

	public static NoteCollection getNotes() {
		if (Current.notes == null) {
			Current.notes = new NoteCollection();
		}

		return Current.notes;
	}
}
