package sbs20.filenotes;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;

public class AboutActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
