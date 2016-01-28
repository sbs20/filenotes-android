package sbs20.filenotes.cloud;

import java.util.LinkedList;
import java.util.Queue;

import sbs20.filenotes.FilenotesApplication;
import sbs20.filenotes.model.Logger;

public abstract class CloudStorage {

    protected FilenotesApplication application;
    protected Queue<String> messages;

    public CloudStorage(FilenotesApplication application) {
        this.application = application;
        this.messages = new LinkedList<String>();
    }

    public Queue<String> getMessages() {
        return this.messages;
    }

    protected Logger getLogger() {
        return this.application.getLogger();
    }

    public CloudStorage sync() {
        this.getLogger().verbose(this, "sync()");
        return this;
    }
}
