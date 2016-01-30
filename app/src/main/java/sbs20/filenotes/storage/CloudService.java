package sbs20.filenotes.storage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Settings;
import sbs20.filenotes.model.Logger;

public abstract class CloudService {

    protected ServiceManager serviceManager;
    protected Settings settings;

    public CloudService(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        this.settings = serviceManager.getSettings();
    }

    protected Logger getLogger() {
        return this.serviceManager.getLogger();
    }

    public abstract void login();
    public abstract void logout();
    public abstract boolean isAuthenticated();
    public abstract List<File> files() throws IOException;
    public abstract void upload(File file);
    public abstract void download(File file);
    public abstract void download(File file, String localName);
    public abstract void delete(File file);
}
