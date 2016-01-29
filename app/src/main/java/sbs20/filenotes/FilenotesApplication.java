package sbs20.filenotes;

import android.app.Application;

public class FilenotesApplication extends Application {

    public FilenotesApplication() {
        super();
        ServiceManager.register(this);
    }
}
