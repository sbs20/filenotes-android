package sbs20.filenotes.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.R;
import com.sbs20.androsync.FileSystemService;
import com.sbs20.androsync.Logger;
import com.sbs20.androsync.SyncContext;

public class NotesManager {

    public interface IStringTransform {
        String transform(String s);
    }

    private SyncContext syncContext;
    private FileSystemService storage;
	private Note selectedNote;
	private NoteCollection notes;
    private boolean isChanged;

    public NotesManager() {
        this.syncContext = ServiceManager.getInstance().getSyncContext();
        this.storage = ServiceManager.getInstance().getLocalFilesystem();
        this.notes = new NoteCollection();
        this.isChanged = false;
    }

	private void setSelectedNote(Note note) {
		this.selectedNote = note;
	}
	public Note getSelectedNote() {
		return this.selectedNote;
	}

    public NoteCollection search(String query) {
        Logger.debug(this, "search()");
        NoteCollection results = new NoteCollection();

        for (Note note : this.notes) {
            if (note.getName()
                    .toLowerCase()
                    .contains(query.toLowerCase()) ||
                this.storedContent(note)
                    .toLowerCase()
                    .contains(query.toLowerCase())) {
                results.add(note);
            }
        }

        return results;
    }

	public NoteCollection getNotes() {
		return this.notes;
	}

    private void mergeFileIntoNote(File file, Note note) {
        note.setTextSummary(this.fileSummaryAsString(file));
        note.setSize(file.length());
        note.setLastModified(new Date(file.lastModified()));
    }

    private static boolean fileListContainsName(List<File> files, String name) {
        for (File file : files) {
            if (name.equals(file.getName())) {
                return true;
            }
        }

        return false;
    }

    public IStringTransform fileReadTransform() {
        return new IStringTransform() {
            @Override
            public String transform(String s) {
                return s.replaceAll("\r\n", "\n");
            }
        };
    }

    public IStringTransform fileWriteTransform() {
        return new IStringTransform() {
            @Override
            public String transform(String s) {
                return s.replaceAll("\n", "\r\n");
            }
        };
    }

    public String fileAsString(File file) {
        return this.fileReadTransform().transform(FileSystemService.fileToString(file));
    }

    public String fileSummaryAsString(File file) {
        return this.fileReadTransform().transform(FileSystemService.fileToString(file, 128));
    }

    public void readAllFromStorage() {
        Logger.verbose(this, "readAllFromStorage.Start");
        String path = ServiceManager.getInstance().getSettings().getLocalStoragePath();
        List<File> files = this.storage.readAllFilesFromStorage(path);

        // Ensure all files are in notes and up to date
        for (File file : files) {
            Note note = this.notes.getByName(file.getName());
            if (note == null) {
                note = new Note();
                note.setName(file.getName());
                note.setPath(file.getAbsolutePath());
                notes.add(note);
            }

            this.mergeFileIntoNote(file, note);
        }

        // Now ensure that any notes NOT in a file is removed
        for (int index = 0; index < notes.size(); index++) {
            if (!fileListContainsName(files, notes.get(index).getName())) {
                notes.remove(index);
                index--;
            }
        }

        // Now filter for preferences
        Settings settings = ServiceManager.getInstance().getSettings();
        for (int index = 0; index < notes.size(); index++) {
            if (notes.get(index).isHidden() && !settings.showHiddenFile()) {
                notes.remove(index);
                index--;
            } else if (!notes.get(index).isText() && !settings.showNonTextFile()) {
                notes.remove(index);
                index--;
            }
        }

        notes.sortBy(settings.getNoteSortComparator());

        Logger.verbose(this, "readAllFromStorage.Finish");
    }

    private void registerChange() {
        this.isChanged = true;
    }

    public void clearChange() {
        this.isChanged = false;
    }

    public boolean isChanged() {
        return this.isChanged;
    }

    public void writeToStorage(Note note) {
        byte[] data = this.fileWriteTransform().transform(note.getText())
                .getBytes(Charset.defaultCharset());

        try {
            this.storage.write(note.getPath(), data);
            ServiceManager.getInstance().toast("Saved");
            note.reset();
            this.registerChange();
        } catch (IOException ex) {
            Logger.error(this, ex.toString());
        }
    }

    public String storedContent(Note note) {
        File file = this.storage.getFile(note.getPath());
        if (file.exists()) {
            return this.fileAsString(file);
        }

        return null;
    }

    public void editNote(Note note) {
        String content = this.storedContent(note);

        if (content != null) {
            note.setText(content);
            note.reset();
        }

        this.setSelectedNote(note);
    }

    public void deleteNote(Note note) {
        this.storage.delete(note.getPath());
        this.notes.remove(note);
        this.registerChange();
    }

    private static String containerPath(String filePath) {
        int pathStemEnd = filePath.lastIndexOf("/");
        if (pathStemEnd == -1) {
            pathStemEnd = 0;
        }

        String stem = filePath.substring(0, pathStemEnd);
        return  stem;
    }

    public boolean renameNote(Note note, String desiredName) {
        String desiredPath = containerPath(note.getPath()) + "/" +
                desiredName;

        boolean succeeded = this.storage.rename(note.getPath(), desiredPath);
        if (succeeded) {
            note.setName(desiredName);
            this.registerChange();
        }

        return succeeded;
    }

    public boolean isStored(Note note) {
        return this.storage.exists(note.getPath());
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
