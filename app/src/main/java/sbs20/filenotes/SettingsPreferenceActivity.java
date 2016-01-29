package sbs20.filenotes;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import sbs20.filenotes.model.Settings;

public class SettingsPreferenceActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(ServiceManager.getInstance().getSettings().getThemeId());
        super.onCreate(savedInstanceState);
        setupActionBar();

        this.getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsPreferenceFragment())
                .commit();
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

        private ServiceManager serviceManager = ServiceManager.getInstance();

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }

        @Override
        // This fires on initial click rather than selection....
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()) {
                case "pref_cloud_logout":
                    this.serviceManager.getLogger().verbose(this, "onPreferenceTreeClick():pref_cloud_logout");
                    this.serviceManager.getCloudSync().logout();
                    this.serviceManager.getSettings().clearCloudSyncName();
                    this.serviceManager.toast(R.string.logged_out);
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
            if (key.equals(Settings.CLOUD_SYNC_SERVICE)) {
                this.serviceManager.getLogger().verbose(this, "onSharedPreferenceChanged:pref_cloud");
                this.serviceManager.resetCloudSync();
                this.serviceManager.getCloudSync().login();
            }
        }
    }
}
