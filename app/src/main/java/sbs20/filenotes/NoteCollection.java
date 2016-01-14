package sbs20.filenotes;

import java.util.ArrayList;
import java.util.Collections;

public class NoteCollection extends ArrayList<Note> {

    public void sort() {
        Collections.sort(this);
    }

    public boolean isExistingName(String name) {
        for (int index = 0; index < this.size(); index++) {
            if (this.get(index).getName().compareToIgnoreCase(name) == 0) {
                return true;
            }
        }

        return false;
    }

    private String createUniqueNewName() {
        String stem = "_New%s.txt";
        String attempt = String.format(stem, "");
        int i = 0;
        while (this.isExistingName(attempt)) {
            ++i;
            attempt = String.format(stem, i);
        }

        return attempt;
    }

    public Note createNote() {
        Note note = new Note();
        note.setName(this.createUniqueNewName());
        this.add(note);
        return note;
    }
}
