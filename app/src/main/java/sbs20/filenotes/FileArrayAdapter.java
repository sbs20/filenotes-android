package sbs20.filenotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FileArrayAdapter extends GenericBaseAdpater<File> {

    private List<File> files = Collections.emptyList();

    public FileArrayAdapter(Context context) {
        super(context);
    }

    private String getSize(File file) {
        long size = file.length();

        if (file.isDirectory())
            return "";

        if (size < 0)
            return "0";
        else if (size == 1)
            return "1 Byte";
        else if (size < 2048)
            return size + " Bytes";
        else if (size < 1024*1024*2)
            return ((int) (size/1024)) + " KB";
        else
            return Math.round(100.0*size/(1024*1024))/100.0 + " MB";
    }

    private String getModificationDate(File file) {
        Date lastModified = new Date(file.lastModified());
        return lastModified.toString();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = this.getItem(position);
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = LayoutInflater.from (this.context);
            row = inflater.inflate(R.layout.listview_notes, parent, false);
        }

        TextView filename = (TextView) row.findViewById(R.id.fileName);
        filename.setText(file.getName());

        TextView modificationDate = (TextView) row.findViewById(R.id.modificationDate);
        modificationDate.setText(this.getModificationDate(file));

        TextView fileSize = (TextView) row.findViewById(R.id.fileSize);
        fileSize.setText(this.getSize(file));

        row.setTag(file);

        return row;
    }
}
