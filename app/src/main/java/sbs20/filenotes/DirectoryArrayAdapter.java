package sbs20.filenotes;

import android.content.Context;
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

public class DirectoryArrayAdapter extends GenericBaseAdpater<File> {

    private File currentDirectory;

    private List<File> getCurrentDirectoryListing() {
        File[] dirs = this.currentDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        List<File> list = new ArrayList<>();

        // Add the parent first
        if (this.currentDirectory.getParentFile() != null) {
            list.add(this.currentDirectory.getParentFile());
        }

        // Now add the children (sorted)
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

    public DirectoryArrayAdapter(Context context, File directory) {
        super(context);
        this.setCurrentDirectory(directory);
    }

    public void setCurrentDirectory(File directory) {
        this.currentDirectory = directory;
        List<File> dirs = this.getCurrentDirectoryListing();
        this.updateItems(dirs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File directory = this.getItem(position);
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            row = inflater.inflate(R.layout.listview_directories, parent, false);
        }

        TextView directoryItem = (TextView) row.findViewById(R.id.directoryItem);

        String text = directory.getName();
        if (this.currentDirectory.getParentFile() != null) {
            if (directory.compareTo(this.currentDirectory.getParentFile()) == 0) {
                text = "..";
            }
        }

        directoryItem.setText(text);
        row.setTag(directory);

        return row;
    }
}
