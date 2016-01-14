package sbs20.filenotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class NoteArrayAdapter extends GenericBaseAdpater<Note> {

    private List<Note> files = Collections.emptyList();

    public NoteArrayAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = this.getItem(position);
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = LayoutInflater.from (this.context);
            row = inflater.inflate(R.layout.listview_notes, parent, false);
        }

        TextView filename = (TextView) row.findViewById(R.id.fileName);
        filename.setText(note.getName());

        TextView modificationDate = (TextView) row.findViewById(R.id.modificationDate);
        modificationDate.setText(note.getLastModifiedString());

        TextView fileSize = (TextView) row.findViewById(R.id.fileSize);
        fileSize.setText(note.getSizeString());

        row.setTag(note);

        return row;
    }
}
