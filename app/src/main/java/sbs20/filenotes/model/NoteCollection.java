package sbs20.filenotes.model;

import java.util.ArrayList;
import java.util.Collections;

public class NoteCollection extends ArrayList<Note> {

    public void sort() {
        Collections.sort(this);
    }

    public boolean isExistingName(String name) {
        for (int index = 0; index < this.size(); index++) {
            if (this.get(index).getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public Note getByName(String name) {
        for (Note note : this) {
            if (name.equals(note.getName())) {
                return note;
            }
        }

        return null;
    }
}
