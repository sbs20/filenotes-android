package sbs20.filenotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import sbs20.filenotes.model.StorageManager;

abstract class ThemedActivity extends AppCompatActivity {

    public FilenotesApplication getFilenotesApplication() {
        return (FilenotesApplication)this.getApplication();
    }

    public StorageManager getStorageManager() {
        return this.getFilenotesApplication().getStorageManager();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(this.getFilenotesApplication().getActiveThemeId());
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}