package sbs20.filenotes;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class SettingsPreferenceActivity extends AppCompatPreferenceActivity {

    public static final String KEY_STORAGE_DIRECTORY = "pref_storagedir";
    public static final String KEY_FONTFACE = "pref_font";
    public static final String KEY_FONTSIZE = "pref_font_size";
    public static final String KEY_THEME = "pref_theme";
    public static final String KEY_DROPBOX_ACCESS_TOKEN = "pref_dbx_access_token";

    protected FilenotesApplication getFilenotesApplication() {
        return (FilenotesApplication)this.getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(this.getFilenotesApplication().getActiveThemeId());
        super.onCreate(savedInstanceState);
        setupActionBar();

        this.getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsPreferenceFragment())
                .commit();

        // DEBUG to remove bad settings
        // getPreferences().edit().remove(KEY_THEME).commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SettingsPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private FilenotesApplication getFilenotesApplication() {
            return (FilenotesApplication)this.getActivity().getApplication();
        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }

        // This fires on initial click rather than selection....
        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()) {
                case "pref_cloud":
                    this.getFilenotesApplication().getLogger().verbose(this, "onPreferenceTreeClick():pref_cloud");
                    break;
            }

            return false;
        }

        @Override
        public void onResume() {
            super.onResume();
            this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            this.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_cloud")) {
                this.getFilenotesApplication().getLogger().verbose(this, "onSharedPreferenceChanged:pref_cloud");
                String value = sharedPreferences.getString(key, null);
                this.getFilenotesApplication().resetCloudStorage();
                this.getFilenotesApplication().getCloudStorage().login();
            }
        }
    }
}
