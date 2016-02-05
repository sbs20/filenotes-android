package sbs20.filenotes.replication;

import android.os.AsyncTask;

public class ReplicatorTask  {

    protected Replicator replicator;

    public ReplicatorTask() {
        this.replicator = new Replicator();
    }

    protected void onProgressUpdate(Replicator.Action action) {
    }

    protected void onPostExecute() {
    }

    public void execute() {
        AsyncTask<Replicator, Replicator.Action, Replicator> task = new AsyncTask<Replicator, Replicator.Action, Replicator>() {

            @Override
            protected Replicator doInBackground(Replicator... params) {
                Replicator replicator = params[0];
                replicator.addObserver(new Replicator.IReplicatorObserver() {
                    @Override
                    public void update(Replicator source, Replicator.Action action) {
                        publishProgress(action);
                    }
                });
                replicator.invoke();
                return replicator;
            }

            @Override
            protected void onProgressUpdate(Replicator.Action... actions) {
                ReplicatorTask.this.onProgressUpdate(actions[0]);
            }

            @Override
            protected void onPostExecute(Replicator replicator) {
                ReplicatorTask.this.onPostExecute();
            }

        }.execute(this.replicator);
    }
}
