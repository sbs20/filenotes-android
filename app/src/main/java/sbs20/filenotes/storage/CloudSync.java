package sbs20.filenotes.storage;

import java.util.LinkedList;
import java.util.Queue;

import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Settings;
import sbs20.filenotes.model.Logger;

public abstract class CloudSync {

    protected ServiceManager serviceManager;
    protected Settings settings;
    protected Queue<String> messages;

    public CloudSync(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        this.settings = serviceManager.getSettings();
        this.messages = new LinkedList<String>();
    }

    public Queue<String> getMessages() {
        return this.messages;
    }

    protected Logger getLogger() {
        return this.serviceManager.getLogger();
    }

    public abstract void login();
    public abstract void logout();
    public abstract boolean isAuthenticated();
    public abstract CloudSync trySync();
}
