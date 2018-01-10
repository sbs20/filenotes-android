package com.sbs20.filenotes.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sbs20.filenotes.R;
import com.sbs20.androsync.IDirectoryProvider;
import com.sbs20.androsync.Logger;

public class DirectoryArrayAdapter extends GenericBaseAdpater<String> {

    private IDirectoryProvider provider;
    private String currentDirectory;

    public DirectoryArrayAdapter(Context context, IDirectoryProvider provider) {
        super(context);
        this.provider = provider;
    }

    public void setCurrentDirectory(final String directory) {
        this.currentDirectory = directory;

        AsyncTask<IDirectoryProvider, Void, List<String>> query = new AsyncTask<IDirectoryProvider, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(IDirectoryProvider... params) {
                try {
                    IDirectoryProvider provider = params[0];

                    // Check the directory exists - if not fallback
                    if (!provider.directoryExists(DirectoryArrayAdapter.this.currentDirectory)) {
                        Logger.debug(this, DirectoryArrayAdapter.this.currentDirectory + " does not exist");
                        DirectoryArrayAdapter.this.currentDirectory = provider.getRootDirectoryPath();
                    }

                    return params[0].getChildDirectoryPaths(DirectoryArrayAdapter.this.currentDirectory);
                } catch (Exception e) {
                    Logger.error(
                            this,
                            e.getMessage() + ";path=" + DirectoryArrayAdapter.this.currentDirectory);
                    return new ArrayList<>();
                }
            }

            @Override
            protected void onPostExecute(List<String> dirs) {
                super.onPostExecute(dirs);
                updateItems(dirs);
            }
        };

        query.execute(this.provider);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String directory = this.getItem(position);
        View row = convertView;

        if(row == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            row = inflater.inflate(R.layout.listview_folders, parent, false);
        }

        TextView directoryItem = (TextView) row.findViewById(R.id.directoryItem);

        directoryItem.setText(directory);
        row.setTag(directory);

        return row;
    }
}
