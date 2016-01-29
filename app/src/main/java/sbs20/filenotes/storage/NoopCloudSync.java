package sbs20.filenotes.storage;

import sbs20.filenotes.ServiceManager;

public class NoopCloudSync extends CloudSync {

    public NoopCloudSync(ServiceManager application) {
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
