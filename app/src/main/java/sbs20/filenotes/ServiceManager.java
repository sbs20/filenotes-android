package sbs20.filenotes;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import sbs20.filenotes.storage.ICloudService;
import sbs20.filenotes.storage.DropboxService;
import sbs20.filenotes.storage.IStringTransform;
import sbs20.filenotes.storage.NoopCloudService;
import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.model.Settings;

public class ServiceManager {

    private static ServiceManager instance;

    private Application application;
    private Settings settings;
    private ICloudService cloudService;
    private NotesManager notesManager;

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

    public ICloudService getCloudService() {
        if (this.cloudService == null) {
            switch (this.getSettings().getCloudServiceName()) {
                case "dropbox":
                    this.cloudService = new DropboxService();
                    break;

                default:
                    this.cloudService = new NoopCloudService();
                    break;
            }
        }

        return this.cloudService;
    }

    public void resetCloudSync() {
        this.cloudService = null;
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
        this.toast(this.string(resId));
    }

    public Context getContext() {
        return this.application.getBaseContext();
    }

    public String string(int resId) {
        return this.application.getString(resId);
    }

    public String[] array(int resId) {
        return this.application.getResources().getStringArray(resId);
    }

    public IStringTransform fileReadTransform() {
        return new IStringTransform() {
            @Override
            public String transform(String s) {
                return s.replaceAll("\r\n", "\n");
            }
        };
    }

    public IStringTransform fileWriteTransform() {
        return new IStringTransform() {
            @Override
            public String transform(String s) {
                return s.replaceAll("\n", "\r\n");
            }
        };
    }
}
