package sbs20.filenotes;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import sbs20.filenotes.storage.CloudSync;
import sbs20.filenotes.storage.DropboxSync;
import sbs20.filenotes.storage.NoopCloudSync;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.model.Settings;

public class ServiceManager {

    private static ServiceManager instance;

    private Application application;
    private Settings settings;
    private CloudSync cloudSync;
    private NotesManager notesManager;
    private DateTimeHelper dateTimeHelper;
    private Logger logger;

    private ServiceManager() {}

    public static ServiceManager getInstance() {
        if (instance == null) {
            throw new NullPointerException("ServiceManager.register(serviceManager) not yet called");
        }
        return instance;
    }

    public static void register(Application application) {
        instance = new ServiceManager();
        instance.application = application;
    }

    public NotesManager getNotesManager() {
        if (this.notesManager == null) {
            this.notesManager = new NotesManager();
        }

        return this.notesManager;
    }

    public CloudSync getCloudSync() {
        if (this.cloudSync == null) {
            switch (this.getSettings().getCloudSyncName()) {
                case "dropbox":
                    this.cloudSync = new DropboxSync(this);
                    break;

                default:
                    this.cloudSync = new NoopCloudSync(this);
                    break;
            }
        }

        return this.cloudSync;
    }

    public void resetCloudSync() {
        this.cloudSync = null;
    }

    public DateTimeHelper getDateTimeHelper() {
        if (this.dateTimeHelper == null) {
            this.dateTimeHelper = new DateTimeHelper();
        }

        return this.dateTimeHelper;
    }

    public Logger getLogger() {
        if (this.logger == null) {
            this.logger = new Logger(this);
        }
        return this.logger;
    }

    public Settings getSettings() {
        if (this.settings == null) {
            this.settings = new Settings(PreferenceManager.getDefaultSharedPreferences(this.application));
        }
        return this.settings;
    }

    public void toast(String s) {
        Toast.makeText(application.getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    public void toast(int resId) {
        this.toast(this.application.getString(resId));
    }

    public Context getContext() {
        return this.application.getBaseContext();
    }
}
