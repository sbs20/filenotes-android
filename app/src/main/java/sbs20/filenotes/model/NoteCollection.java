package sbs20.filenotes.model;

import java.util.ArrayList;
import java.util.Collections;

import sbs20.filenotes.R;

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

    private String createUniqueNewName(String stem) {
        String attempt = String.format(stem, "");
        int i = 0;
        while (this.isExistingName(attempt)) {
            ++i;
            attempt = String.format(stem, i);
        }

        return attempt;
    }

    public Note createNote(String stem) {
        Note note = new Note();
        note.setName(this.createUniqueNewName(stem));
        this.add(note);
        return note;
    }

    public Note getByName(String name) {
        for (Note note : this) {
            if (name.compareTo(note.getName()) == 0) {
                return note;
            }
        }

        return null;
    }
}
