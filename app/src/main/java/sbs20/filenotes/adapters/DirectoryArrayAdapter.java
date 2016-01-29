package sbs20.filenotes.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import sbs20.filenotes.R;
import sbs20.filenotes.storage.IDirectoryListProvider;

public class DirectoryArrayAdapter extends GenericBaseAdpater<String> {

    private IDirectoryListProvider provider;
    private String currentDirectory;

    public DirectoryArrayAdapter(Context context, IDirectoryListProvider provider) {
        super(context);
        this.provider = provider;
    }

    public void setCurrentDirectory(final String directory) {
        final DirectoryArrayAdapter adapter = this;
        this.currentDirectory = directory;

        AsyncTask<IDirectoryListProvider, Void, List<String>> query = new AsyncTask<IDirectoryListProvider, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(IDirectoryListProvider... params) {
                return params[0].getChildDirectoryPaths(directory);
            }

            @Override
            protected void onPostExecute(List<String> dirs) {
                super.onPostExecute(dirs);
                adapter.updateItems(dirs);
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
            row = inflater.inflate(R.layout.listview_directories, parent, false);
        }

        TextView directoryItem = (TextView) row.findViewById(R.id.directoryItem);

        directoryItem.setText(directory);
        row.setTag(directory);

        return row;
    }
}
