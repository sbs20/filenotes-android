package sbs20.filenotes;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.StorageManager;

public class FilenotesApplication extends Application {

    private StorageManager storageManager;
    private DateTimeHelper dateTimeHelper;
    private Logger logger;

    public FilenotesApplication() {
        super();
    }

    public StorageManager getStorageManager() {
        if (this.storageManager == null) {
            this.storageManager = new StorageManager(this);
        }

        return this.storageManager;
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

    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public int getActiveThemeId() {
        String theme = this.getPreferences().getString(SettingsPreferenceActivity.KEY_THEME, "light");
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
