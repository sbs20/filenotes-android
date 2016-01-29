package sbs20.filenotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(ServiceManager.getInstance().getActiveThemeId());
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}