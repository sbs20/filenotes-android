package sbs20.filenotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

abstract class ThemedActivity extends AppCompatActivity {

    protected FilenotesApplication getFilenotesApplication() {
        return (FilenotesApplication)this.getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(this.getFilenotesApplication().getActiveThemeId());
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}