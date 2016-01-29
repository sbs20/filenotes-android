package sbs20.filenotes;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import sbs20.filenotes.adapters.DirectoryArrayAdapter;
import sbs20.filenotes.storage.IDirectoryListProvider;

public abstract class FolderPickerDialog extends DialogPreference {

    protected Context context;
    protected String currentDirectory;
    protected IDirectoryListProvider provider;

    public FolderPickerDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        this.context = context;
    }

    @Override
    protected View onCreateDialogView() {

        final FolderPickerDialog dialog = this;
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this.context).inflate(R.layout.dialog_preference_directory, null);
        final TextView textView = (TextView) layout.findViewById(R.id.currentDirectory);
        final ListView listView = (ListView) layout.findViewById(R.id.directoryList);

        final DirectoryArrayAdapter directoryAdapter = new DirectoryArrayAdapter(this.context, this.provider);
        directoryAdapter.setCurrentDirectory(dialog.currentDirectory);

        textView.setText(dialog.currentDirectory);
        listView.setAdapter(directoryAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.currentDirectory = (String) view.getTag();
                directoryAdapter.setCurrentDirectory(dialog.currentDirectory);
                textView.setText(dialog.currentDirectory);
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
            this.persistString(this.currentDirectory);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String defaultValue = a.getString(index);
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String fallback = this.provider.getRootDirectoryPath();
        this.currentDirectory = restoreValue ? this.getPersistedString(fallback) : (String)defaultValue;
    }
}
