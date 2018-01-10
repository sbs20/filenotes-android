package com.sbs20.androsync;

public class Action {

    public enum Type {
        None,
        Download,
        Upload,
        DeleteLocal,
        DeleteRemote,
        ResolveConflict
    }

    Type type;
    FilePair filePair;

    public Action(Type type, FilePair filePair) {
        this.type = type;
        this.filePair = filePair;
    }

    public Type getType() {
        return this.type;
    }

    public String key() {
        return this.filePair.key();
    }
}
