package sbs20.filenotes.storage;

import android.os.AsyncTask;

public class SyncotronicTask extends AsyncTask<Syncotron, Void, Syncotron> {

    @Override
    protected Syncotron doInBackground(Syncotron... params) {
        params[0].invoke();
        return params[0];
    }
}
