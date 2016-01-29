package sbs20.filenotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import sbs20.filenotes.model.NotesManager;
import sbs20.filenotes.model.Settings;

abstract class ThemedActivity extends AppCompatActivity {

    public FilenotesApplication getFilenotesApplication() {
        return (FilenotesApplication) this.getApplication();
    }

    public NotesManager getNotesManager() {
        return this.getFilenotesApplication().getNotesManager();
    }

    public Settings getSettings() {
        return this.getFilenotesApplication().getSettings();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(this.getFilenotesApplication().getActiveThemeId());
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}