package sbs20.filenotes;

import android.app.Application;
import android.preference.PreferenceManager;
import android.widget.Toast;

import sbs20.filenotes.storage.CloudSync;
import sbs20.filenotes.storage.DropboxSync;
import sbs20.filenotes.storage.NoopCloudSync;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.model.Settings;

public class FilenotesApplication extends Application {

    private Settings settings;
    private CloudSync cloudSync;
    private NotesManager notesManager;
    private DateTimeHelper dateTimeHelper;
    private Logger logger;

    public FilenotesApplication() {
        super();
    }

    public NotesManager getNotesManager() {
        if (this.notesManager == null) {
            this.notesManager = new NotesManager(this);
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
            this.dateTimeHelper = new DateTimeHelper(this);
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
            this.settings = new Settings(PreferenceManager.getDefaultSharedPreferences(this));
        }
        return this.settings;
    }

    public int getActiveThemeId() {
        String theme = this.getSettings().getThemeId();
        switch (theme) {
            case "light":
                return R.style.AppTheme;

            case "dark":
            default:
                return R.style.AppTheme_Dark;
        }
    }

    public void toast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}
