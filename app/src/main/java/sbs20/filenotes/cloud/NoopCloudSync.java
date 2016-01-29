package sbs20.filenotes.cloud;

import sbs20.filenotes.FilenotesApplication;

public class NoopCloudSync extends CloudSync {

    public NoopCloudSync(FilenotesApplication application) {
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
    public NoopCloudSync trySync() {
        this.getLogger().verbose(this, "trySync()");
        return this;
    }
}
