package sbs20.filenotes;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import java.util.Set;

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
            actionBar.setElevation(0);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
            this.serviceManager.getLogger().verbose(this, "onPreferenceTreeClick():" + preference.getKey());
            switch (preference.getKey()) {
                case "pref_cloud_logout":
                    this.serviceManager.getCloudService().logout();
                    this.serviceManager.getSettings().clearCloudServiceName();
                    this.serviceManager.getSettings().clearLastSync();
                    this.serviceManager.toast(R.string.logged_out);
                    break;

                case "pref_replication_clearlast":
                    this.serviceManager.getSettings().clearLastSync();
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
            this.serviceManager.getLogger().verbose(this, "onSharedPreferenceChanged:" + key);

            if (key.equals(Settings.CLOUD_SERVICE)) {
                this.serviceManager.getLogger().verbose(this, "onSharedPreferenceChanged:pref_cloud");
                this.serviceManager.resetCloudSync();
                this.serviceManager.getSettings().clearLastSync();
                this.serviceManager.getCloudService().login();
            } else if (key.equals(Settings.REMOTE_STORAGE_PATH) ||
                    key.equals(Settings.LOCAL_STORAGE_PATH) ||
                    key.equals(Settings.STORAGE_EXCLUDE_HIDDEN) ||
                    key.equals(Settings.STORAGE_EXCLUDE_NONTEXT)) {
                this.serviceManager.getSettings().clearLastSync();
            }
        }
    }
}
