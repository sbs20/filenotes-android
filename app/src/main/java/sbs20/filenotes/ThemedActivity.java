package sbs20.filenotes;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

abstract class ThemedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Application app = getApplication();
        //setTheme(app.getActiveTheme());
        //setTheme(app.getActiveFont());
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}