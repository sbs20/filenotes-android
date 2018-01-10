package com.sbs20.filenotes.model;

import android.content.SharedPreferences;
import android.graphics.Typeface;

import com.sbs20.androsync.ISettings;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;

import com.sbs20.androsync.DateTime;
import com.sbs20.androsync.Logger;
import sbs20.filenotes.R;
import com.sbs20.filenotes.ServiceManager;

public class Settings implements ISettings {

    private SharedPreferences sharedPreferences;

    public static final String APPEARANCE_THEME = "pref_appearance_theme";
    public static final String APPEARANCE_FONTFACE = "pref_appearance_font";
    public static final String APPEARANCE_FONTSIZE = "pref_appearance_font_size";
    public static final String APPEARANCE_WORDWRAP = "pref_appearance_wordwrap";
    public static final String APPEARANCE_NOTE_SORT = "pref_appearance_note_sort";

    public static final String STORAGE_DIRECTORY = "pref_storage_directory";
    public static final String STORAGE_USE_INTERNAL = "pref_storage_useinternal";

    public static final String BEHAVIOUR_SHOW_HIDDEN = "pref_behaviour_show_hidden";
    public static final String BEHAVIOUR_SHOW_NONTEXT = "pref_behaviour_show_nontext";
    public static final String BEHAVIOUR_AUTOSAVE = "pref_behaviour_autosave";

    public static final String CLOUD_SERVICE = "pref_cloud";
    public static final String CLOUD_SERVICE_LOGIN = "pref_cloud_login";
    public static final String CLOUD_SERVICE_LOGOUT = "pref_cloud_logout";
    public static final String CLOUD_STORAGE_PATH = "pref_cloud_storage_directory";

    public static final String REPLICATION_ONCHANGE = "pref_replication_onchange";
    public static final String REPLICATION_INTERVAL_MINUTES = "pref_replication_interval";
    public static final String REPLICATION_SKIP_ERROR = "pref_replication_skip_error";
    public static final String REPLICATION_LAST_SYNC = "last_sync";
    public static final String REPLICATION_NEXT_SYNC = "next_sync";

    public static final String DROPBOX_ACCESS_TOKEN = "pref_dbx_access_token";

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
        Logger.verbose(this, "set(" + key + ", " + value + ")");
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    public void remove(String key) {
        Logger.verbose(this, "remove(" + key + ")");
        this.sharedPreferences.edit().remove(key).commit();
    }

    public int getThemeId() {
        String theme = this.get(APPEARANCE_THEME, "light");
        switch (theme) {
            case "light":
                return R.style.AppTheme;

            case "amoled":
                return R.style.AppTheme_Amoled;

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
        try {
            if (this.internalStorage()) {
                return ServiceManager.getInstance().getContext().getFilesDir().getCanonicalPath();
            } else {
                return this.get(STORAGE_DIRECTORY, "");
            }
        } catch (IOException ex) {
            return "";
        }
    }

    public Typeface getFontFace() {
        String fontFace = this.get(APPEARANCE_FONTFACE, "monospace");

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
        String s = this.get(APPEARANCE_FONTSIZE, "16");
        return Integer.parseInt(s);
    }

    public Comparator<Note> getNoteSortComparator() {
        String s = this.get(APPEARANCE_NOTE_SORT, "name");
        if (s.equals("modified_desc")) {
            return Note.Comparators.DateModifiedDescending;
        } else {
            return Note.Comparators.Name;
        }
    }

    public String getAuthToken() {
        return this.get(DROPBOX_ACCESS_TOKEN, null);
    }

    public void setAuthToken(String value) {
        this.set(DROPBOX_ACCESS_TOKEN, value);
    }

    public void clearAuthToken() {
        this.remove(DROPBOX_ACCESS_TOKEN);
    }

    public String getRemoteStoragePath() {
        return this.get(CLOUD_STORAGE_PATH);
    }

    public Date getLastSync() {
        String s = this.get(REPLICATION_LAST_SYNC);
        return DateTime.from8601String(s);
    }

    public void setLastSync(Date date) {
        String s = DateTime.to8601String(date);
        this.set(REPLICATION_LAST_SYNC, s);
    }

    public Date getNextSync() {
        String s = this.get(REPLICATION_NEXT_SYNC);
        return DateTime.from8601String(s);
    }

    public void setNextSync(Date date) {
        String s = DateTime.to8601String(date);
        this.set(REPLICATION_NEXT_SYNC, s);
    }

    public void clearLastSync() {
        this.remove(REPLICATION_LAST_SYNC);
    }

    public void clearNextSync() {
        this.remove(REPLICATION_NEXT_SYNC);
    }

    public boolean showHiddenFile() {
        return this.sharedPreferences.getBoolean(BEHAVIOUR_SHOW_HIDDEN, true);
    }

    public boolean showNonTextFile() {
        return this.sharedPreferences.getBoolean(BEHAVIOUR_SHOW_NONTEXT, false);
    }

    public long replicationIntervalInMilliseconds() {
        String s = this.get(REPLICATION_INTERVAL_MINUTES, "15");
        int interval = Integer.parseInt(s);
        return interval * 60 * 1000;
    }

    public boolean replicationSkipError() {
        return this.sharedPreferences.getBoolean(REPLICATION_SKIP_ERROR, true);
    }

    public boolean isReplicationOnChange() {
        return this.sharedPreferences.getBoolean(REPLICATION_ONCHANGE, true);
    }

    public boolean wordWrap() {
        return this.sharedPreferences.getBoolean(APPEARANCE_WORDWRAP, false);
    }

    public boolean autosave() {
        return this.sharedPreferences.getBoolean(BEHAVIOUR_AUTOSAVE, false);
    }

    public boolean internalStorage() {
        return this.sharedPreferences.getBoolean(STORAGE_USE_INTERNAL, true);
    }
}
