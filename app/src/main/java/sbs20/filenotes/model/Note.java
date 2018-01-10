package sbs20.filenotes.model;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Note implements Comparable<Note> {

    private String path;
    private String name;
    private String text;
    private String textSummary;
    private long size;
    private Date lastModified;

    private String originalText;

    public Note() {
        this.path = "";
        this.name = "";
        this.text = "";
        this.textSummary = "";
        this.size = 0;
        this.originalText = "";
        this.lastModified = new Date();
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
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

    public String getPath() { return path; }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTextSummary() {
        int summaryLength = 27;
        StringBuffer buffer = new StringBuffer();
        String value = this.text.length() > 0 ? this.text : this.textSummary;
        for (int index = 0; index < value.length() && index < summaryLength && value.charAt(index) != '\n'; index++) {
            buffer.append(value.charAt(index));
        }

        if (buffer.length() == summaryLength) {
            buffer.append("...");
        }

        return buffer.toString();
    }

    public void setTextSummary(String value) {
        this.textSummary = value;
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

    public int compareTo(Note another) {
        return Comparators.Name.compare(this, another);
    }

    public boolean isHidden() {
        return this.name.startsWith(".");
    }

    public boolean isText() {
        return this.name.toLowerCase().endsWith(".txt");
    }

    @Override
    public int hashCode() {
        try {
            Charset utf8 = Charset.forName("UTF-8");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(utf8.encode(this.getName()));
            return new BigInteger(md5.digest()).hashCode();
        } catch (NoSuchAlgorithmException e) {
            return super.hashCode();
        }
    }

    public static class Comparators {

        public static Comparator<Note> Name = new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                Locale locale = Locale.getDefault();
                String l = n1.getName().toLowerCase(locale);
                String r = n2.getName().toLowerCase(locale);
                return l.compareTo(r);
            }
        };

        public static Comparator<Note> DateModifiedDescending = new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                return -1 * n1.lastModified.compareTo(n2.lastModified);
            }
        };
    }
}
