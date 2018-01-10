package sbs20.filenotes;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.sbs20.androsync.FileSystemService;
import com.sbs20.androsync.ICloudService;
import com.sbs20.androsync.DropboxService;
import com.sbs20.androsync.NoopCloudService;
import com.sbs20.androsync.Replicator;
import com.sbs20.androsync.SyncContext;
import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.model.Settings;

public class ServiceManager {

    private static ServiceManager instance;

    private Application application;
    private Settings settings;
    private NotesManager notesManager;
    private FileSystemService localFilesystem;
    private ICloudService cloudService;
    private Replicator replicator;
    private SyncContext syncContext;
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

    public SyncContext getSyncContext() {
        if (this.syncContext == null) {
            this.syncContext = new SyncContext(
                    this.getLocalFilesystem(),
                    this.getCloudService(),
                    this.getSettings().getLocalStoragePath(),
                    this.getSettings().getRemoteStoragePath(),
                    string(R.string.replication_conflict_extension));
        }

        return this.syncContext;
    }

    public FileSystemService getLocalFilesystem() {
        if (this.localFilesystem == null) {
            this.localFilesystem = new FileSystemService();
        }

        return this.localFilesystem;
    }

    public DropboxService getDropboxService() {
        return new DropboxService(
                this.string(R.string.dropbox_app_key),
                this.string(R.string.dropbox_client_identifier),
                this.string(R.string.dropbox_locale),
                this.getContext(),
                this.settings);
    }

    public ICloudService getCloudService() {
        if (this.cloudService == null) {
            switch (this.getSettings().getCloudServiceName()) {
                case "dropbox":
                    this.cloudService = this.getDropboxService();
                    break;

                default:
                    this.cloudService = new NoopCloudService();
                    break;
            }
        }

        return this.cloudService;
    }

    public Replicator getReplicator() {
        if (this.replicator == null) {
            this.replicator = new Replicator(
                    this.getSettings(),
                    this.getSyncContext());
        }

        return this.replicator;
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

}
