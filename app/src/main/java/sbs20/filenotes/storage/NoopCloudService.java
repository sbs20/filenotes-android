package sbs20.filenotes.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import sbs20.filenotes.ServiceManager;

public class NoopCloudService extends CloudService {

    public NoopCloudService(ServiceManager application) {
        super(application);
    }

    @Override
    public void login() {
    }

    @Override
    public void logout() {
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public List<File> files() throws IOException {
        this.getLogger().verbose(this, "files()");
        throw new IOException("NoopService has no remote files");
    }

    @Override
    public void upload(File file) {
    }

    @Override
    public void download(File file) {
    }

    @Override
    public void download(File file, String localName) {
    }

    @Override
    public void delete(File file) {
    }
}
