package sbs20.filenotes.model;

import android.content.SharedPreferences;
import android.graphics.Typeface;

import sbs20.filenotes.R;

public class Settings {

    private SharedPreferences sharedPreferences;

    public static final String LOCAL_STORAGE_PATH = "pref_storagedir";
    public static final String FONTFACE = "pref_font";
    public static final String FONTSIZE = "pref_font_size";
    public static final String THEME = "pref_theme";
    public static final String DROPBOX_ACCESS_TOKEN = "pref_dbx_access_token";
    public static final String CLOUD_SYNC_SERVICE = "pref_cloud";
    public static final String REMOTE_STORAGE_PATH = "pref_cloudstoragedir";

    public Settings(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public String get(String key, String dflt) {
        return this.sharedPreferences.getString(key, dflt);
    }

    public String get(String key) {
        return this.get(key, null);
    }

    public void set(String key, String value) {
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    public void remove(String key) {
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

    public String getCloudSyncName() {
        return this.get(CLOUD_SYNC_SERVICE, "none");
    }

    public void clearCloudSyncName() {
        this.remove(CLOUD_SYNC_SERVICE);
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
}
