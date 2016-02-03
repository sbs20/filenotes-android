package sbs20.filenotes.storage;

import android.os.AsyncTask;

public class ReplicatorTask extends AsyncTask<Replicator, Void, Replicator> {

    @Override
    protected Replicator doInBackground(Replicator... params) {
        Replicator replicator = params[0];
        replicator.addObserver(new Replicator.IReplicatorObserver() {
            @Override
            public void localChange() {
                publishProgress(null);
            }
        });
        replicator.invoke();

        return replicator;
    }
}
