package sbs20.filenotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sbs20.androsync.Logger;
import sbs20.filenotes.model.Settings;

public class SettingsActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private ServiceManager serviceManager = ServiceManager.getInstance();

        private void refresh() {
            Settings settings = this.serviceManager.getSettings();
            findPreference(Settings.STORAGE_DIRECTORY).setEnabled(!settings.internalStorage());

            boolean isCloudEnabled = !this.serviceManager.array(R.array.pref_cloud_values)[0].equals(settings.getCloudServiceName());
            boolean authenticated = this.serviceManager.getCloudService().isAuthenticated();

            findPreference(Settings.CLOUD_SERVICE_LOGIN).setEnabled(isCloudEnabled && !authenticated);
            findPreference(Settings.CLOUD_STORAGE_PATH).setEnabled(isCloudEnabled && authenticated);
            findPreference(Settings.CLOUD_SERVICE_LOGOUT).setEnabled(isCloudEnabled && authenticated);

            findPreference("Replication").setEnabled(isCloudEnabled && authenticated);
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
            this.refresh();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            if(view != null) {
                ListView lv = (ListView) view.findViewById(android.R.id.list);
                lv.setPadding(0, 0, 0, 0);
            }
            return view;
        }

        @Override
        // This fires on initial click rather than selection....
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            Logger.verbose(this, "onPreferenceTreeClick():" + preference.getKey());
            switch (preference.getKey()) {
                case "pref_cloud_logout":
                    this.serviceManager.getCloudService().logout();
                    this.serviceManager.getSettings().clearCloudServiceName();
                    this.serviceManager.getSettings().clearLastSync();
                    this.serviceManager.getSettings().clearNextSync();
                    this.serviceManager.toast(R.string.logged_out);
                    break;

                case "pref_cloud_login":
                    this.serviceManager.getCloudService().login();
                    break;

                case "pref_replication_clearlast":
                    this.serviceManager.getSettings().clearLastSync();
                    break;

                case "pref_about_overview":
                case "pref_about_license":
                    // Do nothing for now
                    // Intent intent = new Intent(this.getActivity(), AboutActivity.class);
                    // this.startActivity(intent);
                    break;
            }

            return false;
        }

        @Override
        public void onResume() {
            super.onResume();
            this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            // This event gets called after the Dropbox AuthActivity
            this.refresh();
        }

        @Override
        public void onPause() {
            super.onPause();
            this.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Logger.verbose(this, "onSharedPreferenceChanged:" + key);

            switch (key) {
                case Settings.CLOUD_SERVICE:
                    this.serviceManager.resetCloudSync();
                    this.serviceManager.getSettings().clearLastSync();
                    this.serviceManager.getSettings().clearNextSync();
                    this.serviceManager.getCloudService().login();
                    break;

                case Settings.CLOUD_STORAGE_PATH:
                case Settings.STORAGE_DIRECTORY:
                    this.serviceManager.getSettings().clearLastSync();
                    this.serviceManager.getSettings().clearNextSync();
                    break;

                case Settings.STORAGE_USE_INTERNAL:
                    break;

            }

            this.refresh();
        }
    }
}
