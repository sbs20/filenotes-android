package sbs20.filenotes.storage;

import java.io.IOException;
import java.util.List;

import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Settings;

public abstract class CloudService {

    protected ServiceManager serviceManager;
    protected Settings settings;

    public CloudService(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        this.settings = serviceManager.getSettings();
    }

    public abstract void login();
    public abstract void logout();
    public abstract List<File> files() throws IOException;
    public abstract void upload(File file) throws Exception;
    public abstract void download(File file) throws Exception ;
    public abstract void download(File file, String localName) throws Exception ;
    public abstract void delete(File file) throws Exception ;
}
