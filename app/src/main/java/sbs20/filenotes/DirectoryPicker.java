package sbs20.filenotes;

import android.content.Context;
import android.util.AttributeSet;

import sbs20.filenotes.storage.FileSystemManager;
import sbs20.filenotes.storage.IDirectoryListProvider;

public class DirectoryPicker extends FolderPicker {

	public DirectoryPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    @Override
    public IDirectoryListProvider CreateProvider() {
        return new FileSystemManager();
    }
}
