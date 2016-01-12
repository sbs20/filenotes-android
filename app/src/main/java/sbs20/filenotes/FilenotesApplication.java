package sbs20.filenotes;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class FilenotesApplication extends Application {

    public SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    public int getActiveThemeId() {
        String theme = this.getPreferences().getString(SettingsActivity.KEY_THEME, "light");
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
