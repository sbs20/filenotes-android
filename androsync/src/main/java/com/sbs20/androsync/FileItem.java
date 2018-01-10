package com.sbs20.androsync;

import com.dropbox.core.v2.files.FileMetadata;

import java.util.Date;

public class FileItem {
    private boolean isFolder;
    private String name;
    private String path;
    private String id;
    private String rev;
    private long size;
    private Date lastModified;
    private Date clientModified;
    private Object file;
    private boolean isLocal;

    public FileItem(FileMetadata dbxfile) {
        this.isFolder = false;
        this.name = dbxfile.getName();
        this.path = dbxfile.getPathLower();
        this.id = dbxfile.getId();
        this.rev = dbxfile.getRev();
        this.size = dbxfile.getSize();
        this.lastModified = dbxfile.getServerModified();
        this.clientModified = dbxfile.getClientModified();
        this.file = dbxfile;
        this.isLocal = false;
    }

    public FileItem(java.io.File file) {
        this.isFolder = file.isDirectory();
        this.name = file.getName();
        this.path = file.getPath();
        this.id = "";
        this.rev = "";
        this.size = file.length();
        this.lastModified = new Date(file.lastModified());
        this.clientModified = new Date(file.lastModified());
        this.file = file;
        this.isLocal = true;
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

    public Date getClientModified() {
        return clientModified;
    }

    public Object getFile() {return this.file;}

    public String key() {
        return name;
    }

    public boolean isLocal() { return this.isLocal; }
}
