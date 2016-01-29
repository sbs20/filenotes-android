package sbs20.filenotes;

import android.content.Context;
import android.util.AttributeSet;

import sbs20.filenotes.storage.FileSystemManager;

public class DirectoryPickerDialog extends FolderPickerDialog {

	public DirectoryPickerDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
        this.provider = new FileSystemManager(null);
        this.currentDirectory = this.provider.getRootDirectoryPath();
	}
}
