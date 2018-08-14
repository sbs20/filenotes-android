package sbs20.filenotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sbs20.filenotes.adapters.DirectoryArrayAdapter;
import sbs20.filenotes.storage.IDirectoryProvider;

public abstract class FolderPicker extends DialogPreference {

    private DirectoryArrayAdapter directoryAdapter;

    protected Context context;
    protected String currentDirectory;
    protected IDirectoryProvider provider;

    public FolderPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        this.provider = this.createProvider();
        this.currentDirectory = this.provider.getRootDirectoryPath();
        this.context = context;
    }

    public abstract IDirectoryProvider createProvider();

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton(R.string.action_new, this);
    }

    public void createDirectory(final String path) {
        AsyncTask<IDirectoryProvider, Void, List<String>> query = new AsyncTask<IDirectoryProvider, Void, List<String>>() {

            private Exception exception;

            @Override
            protected List<String> doInBackground(IDirectoryProvider... params) {
                try {
                    params[0].createDirectory(path);
                    return params[0].getChildDirectoryPaths(currentDirectory);
                } catch (Exception e) {
                    exception = e;
                    cancel(false);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<String> dirs) {
                super.onPostExecute(dirs);
                directoryAdapter.updateItems(dirs);
            }

            @Override
            protected void onCancelled() {
                ServiceManager.getInstance().toast(exception.getMessage());
            }
        };

        query.execute(provider);
    }


    public void createFolderStart() {
        final EditText createEditText = new EditText(this.context);

        createEditText.setText(R.string.default_new_folder_name);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int result) {
                switch (result) {
                    case DialogInterface.BUTTON_POSITIVE:
                        String name = createEditText.getText().toString();
                        createDirectory(currentDirectory + "/" + name);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Cancel button clicked - don't do anything further
                        break;
                }
            }
        };

        new AlertDialog.Builder(this.context)
                .setMessage(R.string.action_rename)
                .setView(createEditText)
                .setPositiveButton(android.R.string.ok, dialogClickListener)
                .setNegativeButton(android.R.string.cancel, dialogClickListener)
                .show();
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog dialog = (AlertDialog) getDialog();

        // This is very unorthodox. We're hacking the neutral button to create a "new folder" button.
        // I think this needs to be redone as its own activity. Later.
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFolderStart();
            }
        });
    }

    @Override
    protected View onCreateDialogView() {

        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_preference_folder, null);
        final TextView textView = layout.findViewById(R.id.currentDirectory);
        final ListView listView = layout.findViewById(R.id.directoryList);

        directoryAdapter = new DirectoryArrayAdapter(context, provider);
        directoryAdapter.setCurrentDirectory(currentDirectory);

        textView.setText(currentDirectory);
        listView.setAdapter(directoryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentDirectory = (String) view.getTag();
                directoryAdapter.setCurrentDirectory(currentDirectory);
                textView.setText(currentDirectory);
            }
        });

        // Fix dialog size
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
            this.persistString(currentDirectory);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        String defaultValue = a.getString(index);
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String fallback = provider.getRootDirectoryPath();
        currentDirectory = restoreValue ? getPersistedString(fallback) : (String)defaultValue;
    }
}
