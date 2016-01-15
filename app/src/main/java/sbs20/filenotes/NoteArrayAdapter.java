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
    private Context context;

    public NoteArrayAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = this.getItem(position);
        View row = convertView;

        if(row == null) {
            LayoutInflater inflater = LayoutInflater.from (this.context);
            row = inflater.inflate(R.layout.listview_notes, parent, false);
        }

        TextView name = (TextView) row.findViewById(R.id.listnote_name);
        name.setText(note.getName());

        TextView text = (TextView) row.findViewById(R.id.listnote_text);
        text.setText(note.getTextSummary());

        TextView lastModified = (TextView) row.findViewById(R.id.listnode_lastModified);
        MainActivity activity = (MainActivity)this.context;
        DateTimeHelper dateTimeHelper = activity.getFilenotesApplication().getDateTimeHelper();

        String date = dateTimeHelper.formatDate(note.getLastModified());
        date += "\n" + dateTimeHelper.formatTime(note.getLastModified());
        lastModified.setText(date);

        TextView size = (TextView) row.findViewById(R.id.listnote_size);
        size.setText(note.getSizeString());

        row.setTag(note);

        return row;
    }
}