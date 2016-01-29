package sbs20.filenotes.storage;

import com.dropbox.core.v2.DbxFiles;

import java.util.Date;

public class RemoteFile {
    private boolean isFolder;
    private String name;
    private String path;
    private String id;
    private String rev;
    private long size;
    private Date lastModified;
    private Object file;

    public RemoteFile (DbxFiles.FileMetadata metadata) {
        this.file = metadata;
        this.id = metadata.id;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public String getRev() {
        return rev;
    }

    public long getSize() {
        return size;
    }

    public Date getLastModified() {
        return lastModified;
    }
}
