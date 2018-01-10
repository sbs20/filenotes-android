package sbs20.filenotes;

import android.content.Context;
import android.util.AttributeSet;

import com.sbs20.androsync.FileSystemService;
import com.sbs20.androsync.IDirectoryProvider;

public class DirectoryPicker extends FolderPicker {

	public DirectoryPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    @Override
    public IDirectoryProvider createProvider() {
        return ServiceManager.getInstance().getLocalFilesystem();
    }
}
