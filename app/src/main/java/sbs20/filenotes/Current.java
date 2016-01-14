package sbs20.filenotes;

public class Current {
	private static Note selectedNote;
	
	public static void setSelectedNote(Note file) {
		Current.selectedNote = file;
	}
	public static Note getSelectedNote() {
		return Current.selectedNote;
	}
}
