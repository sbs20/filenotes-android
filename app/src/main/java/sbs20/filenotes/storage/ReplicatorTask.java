package sbs20.filenotes.storage;

import android.os.AsyncTask;

public class ReplicatorTask extends AsyncTask<Replicator, Void, Replicator> {

    @Override
    protected Replicator doInBackground(Replicator... params) {
        params[0].invoke();
        return params[0];
    }
}
