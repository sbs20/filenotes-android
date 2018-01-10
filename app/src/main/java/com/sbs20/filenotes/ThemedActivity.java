package com.sbs20.filenotes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.setTheme(ServiceManager.getInstance().getSettings().getThemeId());
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
    }
}