package sbs20.filenotes;

import java.io.File;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DirectoryPickerDialog extends DialogPreference {

	private Context context;
	private File currentDirectory;
		
	public DirectoryPickerDialog(Context context, AttributeSet attrs) {
		super(context, attrs);

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        this.context = context;
        this.currentDirectory = this.getRootDirectory();
	}
		
	private File getRootDirectory() {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		return new File(path);
	}

	@Override
	protected View onCreateDialogView() {

        final DirectoryPickerDialog dialog = this;
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.dialog_preference_directory, null);
        final TextView textView = (TextView) layout.findViewById(R.id.currentDirectory);
        final ListView listView = (ListView) layout.findViewById(R.id.directoryList);
        final DirectoryArrayAdapter directoryAdapter = new DirectoryArrayAdapter(this.context, currentDirectory);

        textView.setText(dialog.currentDirectory.getAbsolutePath());
        listView.setAdapter(directoryAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.currentDirectory = (File) view.getTag();
                directoryAdapter.setCurrentDirectory(dialog.currentDirectory);
                textView.setText(dialog.currentDirectory.getAbsolutePath());
            }
        });

        // Fix dialog size
        WindowManager windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        final int height = (int)(displayMetrics.heightPixels * 0.9);
        layout.setMinimumHeight(height);

        return layout;
    }
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
	    // When the user selects "OK", persist the new value
	    if (positiveResult) {
	    	this.persistString(this.currentDirectory.getAbsolutePath());
	    }
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		String defaultValue = a.getString(index);
		return defaultValue;
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String fallback = this.getRootDirectory().getAbsolutePath();
		String path = restoreValue ? this.getPersistedString(fallback) : (String)defaultValue;
		this.currentDirectory = new File(path);
	}
}
