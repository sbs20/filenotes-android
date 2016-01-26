package sbs20.filenotes;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
	
	private List<File> getChildDirectories(File dir) {
		File[] dirs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		List<File> list = new ArrayList<File>();

		if (dir.getParentFile() != null) {
			list.add(dir.getParentFile());
		}
		
		if (dirs != null) {
			Arrays.sort(dirs, new Comparator<File>() {
				@Override
				public int compare(File lhs, File rhs) {
					Locale locale = Locale.getDefault();
					String l = lhs.getAbsolutePath().toLowerCase(locale);
					String r = rhs.getAbsolutePath().toLowerCase(locale);
					return l.compareTo(r);
				}
			});

			for (File f : dirs) {
				list.add(f);
			}
		}
		
		return list;
	}
	
	private ArrayAdapter<File> createArrayAdapter() {
		final DirectoryPickerDialog thisDialog = this;
		List<File> dirs = this.getChildDirectories(this.currentDirectory);
		return new ArrayAdapter<File>(this.context, R.layout.listview_directories, dirs) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				File directory = this.getItem(position);
				View row = convertView;

				if(row == null)
				{
					LayoutInflater inflater = thisDialog.getDialog().getLayoutInflater();
					row = inflater.inflate(R.layout.listview_directories, parent, false);
				}

				TextView directoryTextView = (TextView) row.findViewById(R.id.directoryItem);
				
				String text = directory.getName();
				if (thisDialog.currentDirectory.getParentFile() != null) {
					if (directory.compareTo(thisDialog.currentDirectory.getParentFile()) == 0) {
						text = "..";
					}
				}

				directoryTextView.setText(text);

				row.setTag(directory);

				return row;
			}
		};
	}
		
	@Override
	protected View onCreateDialogView() {

        final DirectoryPickerDialog dialog = this;
        LinearLayout layout = (LinearLayout)LayoutInflater.from(this.context).inflate(R.layout.dialog_preference_directory, null);
        final TextView textView = (TextView) layout.findViewById(R.id.currentDirectory);
        final ListView listView = (ListView) layout.findViewById(R.id.directoryList);

        textView.setText(dialog.currentDirectory.getAbsolutePath());
        listView.setAdapter(this.createArrayAdapter());

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = (File) view.getTag();
                dialog.currentDirectory = file;
                listView.setAdapter(dialog.createArrayAdapter());
                textView.setText(dialog.currentDirectory.getAbsolutePath());
            }
        });

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
