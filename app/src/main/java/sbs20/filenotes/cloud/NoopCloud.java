package sbs20.filenotes.cloud;

import sbs20.filenotes.FilenotesApplication;

public class NoopCloud extends CloudStorage {

    public NoopCloud(FilenotesApplication application) {
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
    public NoopCloud trySync() {
        this.getLogger().verbose(this, "trySync()");
        return this;
    }
}
