package sbs20.filenotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

abstract class ThemedActivity extends AppCompatActivity {

    protected FilenotesApplication getFilenotesApplication() {
        return (FilenotesApplication)this.getApplication();
    }

    protected StorageManager getStorageManager() {
        return this.getFilenotesApplication().getStorageManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(this.getFilenotesApplication().getActiveThemeId());
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}