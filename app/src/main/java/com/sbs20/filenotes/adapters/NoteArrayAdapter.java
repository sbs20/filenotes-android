package com.sbs20.filenotes.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sbs20.androsync.DateTime;
import com.sbs20.filenotes.model.Note;

import sbs20.filenotes.R;

public class NoteArrayAdapter extends GenericBaseAdpater<Note> {

    private static final String[] COLORS = {
            "#f44336",
            "#e91e63",
            "#9c27b0",
            "#673ab7",
            "#3f51b5",
            "#2196f3",
            "#03a9f4",
            "#00bcd4",
            "#009688",
            "#4caf50",
            "#8bc34a",
            "#cddc39",
            "#ffeb3b",
            "#ffc107",
            "#ff9800",
            "#ff5722",
            "#795548",
            "#9e9e9e",
            "#607d8b"
    };

    private int getColor(Note note) {
        double max = Integer.MAX_VALUE;
        double universe = max - (double) Integer.MIN_VALUE;
        double randomish = (max - note.hashCode()) / universe;
        int index = (int) (randomish * COLORS.length);
        return Color.parseColor(COLORS[index]);
    }

    public NoteArrayAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Note note = this.getItem(position);
        View row = convertView;

        if(row == null) {
            LayoutInflater inflater = LayoutInflater.from (this.context);
            row = inflater.inflate(R.layout.listview_notes, parent, false);
        }

        View flash = (View) row.findViewById(R.id.listnote_flash);
        flash.setBackgroundColor(this.getColor(note));

        TextView name = (TextView) row.findViewById(R.id.listnote_name);
        name.setText(note.getName());

        TextView text = (TextView) row.findViewById(R.id.listnote_text);
        text.setText(note.getTextSummary());

        TextView lastModified = (TextView) row.findViewById(R.id.listnode_lastModified);

        DateTime formatter = new DateTime(this.context);
        String date = formatter.formatDate(note.getLastModified());
        date += "\n" + formatter.formatTime(note.getLastModified());
        lastModified.setText(date);

        TextView size = (TextView) row.findViewById(R.id.listnote_size);
        size.setText(note.getSizeString());

        row.setTag(note);

        return row;
    }
}
