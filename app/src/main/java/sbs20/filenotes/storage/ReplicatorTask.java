package sbs20.filenotes.storage;

import android.os.AsyncTask;

public class ReplicatorTask extends AsyncTask<Replicator, Replicator.Event, Replicator> {

    @Override
    protected Replicator doInBackground(Replicator... params) {
        Replicator replicator = params[0];
        replicator.addObserver(new Replicator.IReplicatorObserver() {
            @Override
            public void update(Replicator source, Replicator.Event event) {
                publishProgress(event);
            }
        });
        replicator.invoke();
        return replicator;
    }
}
