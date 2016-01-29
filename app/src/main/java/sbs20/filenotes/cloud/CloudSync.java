package sbs20.filenotes.cloud;

import java.util.LinkedList;
import java.util.Queue;

import sbs20.filenotes.FilenotesApplication;
import sbs20.filenotes.model.Settings;
import sbs20.filenotes.model.Logger;

public abstract class CloudSync {

    protected FilenotesApplication application;
    protected Settings settings;
    protected Queue<String> messages;

    public CloudSync(FilenotesApplication application) {
        this.application = application;
        this.settings = application.getSettings();
        this.messages = new LinkedList<String>();
    }

    public Queue<String> getMessages() {
        return this.messages;
    }

    protected Logger getLogger() {
        return this.application.getLogger();
    }

    public abstract void login();
    public abstract void logout();
    public abstract boolean isAuthenticated();
    public abstract CloudSync trySync();
}
