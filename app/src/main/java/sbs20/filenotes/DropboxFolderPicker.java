package sbs20.filenotes;

import android.content.Context;
import android.util.AttributeSet;

import sbs20.filenotes.storage.DropboxSync;
import sbs20.filenotes.storage.IDirectoryListProvider;

public class DropboxFolderPicker extends FolderPicker {
    public DropboxFolderPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public IDirectoryListProvider CreateProvider() {
        return new DropboxSync(ServiceManager.getInstance());
    }
}
