package com.sbs20.androsync;

class FilePair {
    FileItem local;
    FileItem remote;

    FilePair() {
    }

    public String key() {
        return this.local != null ? this.local.key() : this.remote.key();
    }

    public boolean areDatesEqual() {
        if (this.local == null) {
            return false;
        } else if (this.remote == null) {
            return false;
        } else if (this.local.getLastModified().equals(this.remote.getLastModified())) {
            return true;
        } else if (this.local.getLastModified().equals(this.remote.getClientModified())) {
            return true;
        } else {
            return false;
        }
    }
}
