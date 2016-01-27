package sbs20.filenotes;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class Note implements Comparable<Note> {

    private String name;
    private String text;
    private Date lastModified;

    private String originalText;

    public Note() {
        this.name = "";
        this.text = "";
        this.originalText = "";
        this.lastModified = new Date();
    }

    public long getSize() {
        return this.getText().length();
    }

    public String getSizeString() {
        long kb = 1 << 10;
        long mb = kb << 10;
        long size = this.getSize();
        if (size < 0) {
            return "0";
        } else if (size == 1) {
            return "1 Byte";
        } else if (size < 2 * kb) {
            return size + " Bytes";
        } else if (size < 2 * mb) {
            return ((int) (size / kb)) + " KB";
        } else {
            return Math.round(100.0 * size / (mb)) / 100.0 + " MB";
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) {
            text = "";
        }

        this.text = text;
    }

    public String getTextSummary() {
        int summaryLength = 27;
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < this.text.length() && index < summaryLength && this.text.charAt(index) != '\n'; index++) {
            buffer.append(this.text.charAt(index));
        }

        if (buffer.length() == summaryLength) {
            buffer.append("...");
        }

        return buffer.toString();
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDirty() {
        return this.text.compareTo(this.originalText) != 0;
    }

    public void reset() {
        this.originalText = this.text;
    }

    public String getLastModifiedString() {
        return this.getLastModified().toString();
    }

    public int compareTo(Note another) {
        Locale locale = Locale.getDefault();
        String l = this.getName().toLowerCase(locale);
        String r = another.getName().toLowerCase(locale);
        return l.compareTo(r);
    }
}
