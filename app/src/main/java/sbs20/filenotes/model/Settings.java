package sbs20.filenotes.model;

import android.content.SharedPreferences;
import android.graphics.Typeface;

import java.util.Date;

import sbs20.filenotes.DateTime;
import sbs20.filenotes.R;
import sbs20.filenotes.ServiceManager;

public class Settings {

    private SharedPreferences sharedPreferences;

    public static final String THEME = "pref_theme";
    public static final String FONTFACE = "pref_font";
    public static final String FONTSIZE = "pref_font_size";

    public static final String LOCAL_STORAGE_PATH = "pref_storagedir";
    public static final String STORAGE_SHOW_HIDDEN = "pref_show_hidden";
    public static final String STORAGE_SHOW_NONTEXT = "pref_show_nontext";

    public static final String CLOUD_SERVICE = "pref_cloud";
    public static final String REMOTE_STORAGE_PATH = "pref_cloudstoragedir";
    public static final String LAST_SYNC = "last_sync";

    public static final String DROPBOX_ACCESS_TOKEN = "pref_dbx_access_token";

    public Settings(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String get(String key, String dflt) {
        Logger.verbose(this, "get(" + key + ")");
        return this.sharedPreferences.getString(key, dflt);
    }

    public String get(String key) {
        return this.get(key, null);
    }

    public void set(String key, String value) {
        Logger.verbose(this, "set(" + key + ", " + value + ")");
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    public void remove(String key) {
        Logger.verbose(this, "remove(" + key + ")");
        this.sharedPreferences.edit().remove(key).commit();
    }

    public int getThemeId() {
        String theme = this.get(THEME, "light");
        switch (theme) {
            case "light":
                return R.style.AppTheme;

            case "dark":
            default:
                return R.style.AppTheme_Dark;
        }
    }

    public String getCloudServiceName() {
        return this.get(CLOUD_SERVICE, "none");
    }

    public void clearCloudServiceName() {
        this.remove(CLOUD_SERVICE);
    }

    public String getLocalStoragePath() {
        return this.get(LOCAL_STORAGE_PATH, "");
    }

    public Typeface getFontFace() {
        String fontFace = this.get(FONTFACE, "monospace");

        if (fontFace.equals("monospace")) {
            return Typeface.MONOSPACE;
        } else if (fontFace.equals("sansserif")) {
            return Typeface.SANS_SERIF;
        } else if (fontFace.equals("serif")) {
            return Typeface.SERIF;
        }

        return Typeface.MONOSPACE;
    }

    public int getFontSize() {
        String s = this.get(FONTSIZE, "16");
        return Integer.parseInt(s);
    }

    public String getDropboxAccessToken() {
        return this.get(DROPBOX_ACCESS_TOKEN, null);
    }

    public void setDropboxAccessToken(String value) {
        this.set(DROPBOX_ACCESS_TOKEN, value);
    }

    public void clearDropboxAccessToken() {
        this.remove(DROPBOX_ACCESS_TOKEN);
    }

    public String getRemoteStoragePath() {
        return this.get(REMOTE_STORAGE_PATH);
    }

    public Date getLastSync() {
        String s = this.get(LAST_SYNC);
        return DateTime.from8601String(s);
    }

    public void setLastSync(Date date) {
        String s = DateTime.to8601String(date);
        this.set(LAST_SYNC, s);
    }

    public void clearLastSync() {
        this.remove(LAST_SYNC);
    }

    public boolean showHiddenFile() {
        return this.sharedPreferences.getBoolean(STORAGE_SHOW_HIDDEN, true);
    }

    public boolean showNonTextFile() {
        return this.sharedPreferences.getBoolean(STORAGE_SHOW_NONTEXT, false);
    }

    public long replicationThresholdInMilliseconds() {
        return 5 * 60 * 1000;
    }
}
