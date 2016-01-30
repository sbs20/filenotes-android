package sbs20.filenotes;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import sbs20.filenotes.storage.CloudService;
import sbs20.filenotes.storage.DropboxService;
import sbs20.filenotes.storage.NoopCloudService;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.model.Settings;

public class ServiceManager {

    private static ServiceManager instance;

    private Application application;
    private Settings settings;
    private CloudService cloudService;
    private NotesManager notesManager;
    private DateTime dateTime;
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

    public CloudService getCloudService() {
        if (this.cloudService == null) {
            switch (this.getSettings().getCloudServiceName()) {
                case "dropbox":
                    this.cloudService = new DropboxService(this);
                    break;

                default:
                    this.cloudService = new NoopCloudService(this);
                    break;
            }
        }

        return this.cloudService;
    }

    public void resetCloudSync() {
        this.cloudService = null;
    }

    public DateTime getDateTime() {
        if (this.dateTime == null) {
            this.dateTime = new DateTime();
        }

        return this.dateTime;
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
