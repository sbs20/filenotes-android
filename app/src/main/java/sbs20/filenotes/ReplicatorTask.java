package sbs20.filenotes;

import android.os.AsyncTask;

import com.sbs20.androsync.Action;
import com.sbs20.androsync.IObserver;
import com.sbs20.androsync.Replicator;

public class ReplicatorTask  {

    protected Replicator replicator;

    public ReplicatorTask() {
        this.replicator = ServiceManager.getInstance().getReplicator();
    }

    protected void onProgressUpdate(Action action) {
    }

    protected void onPostExecute() {
    }

    public void execute() {
        new AsyncTask<Replicator, Action, Replicator>() {

            @Override
            protected Replicator doInBackground(Replicator... params) {
                Replicator replicator = params[0];
                replicator.invoke(new IObserver() {
                    @Override
                    public void update(Replicator source, Action action) {
                        publishProgress(action);
                    }
                });
                return replicator;
            }

            @Override
            protected void onProgressUpdate(Action... actions) {
                ReplicatorTask.this.onProgressUpdate(actions[0]);
            }

            @Override
            protected void onPostExecute(Replicator replicator) {
                switch (replicator.getStatus()) {
                    case Succeeded:
                        ServiceManager.getInstance().getNotesManager().clearChange();
                        break;

                    case Aborted:
                        ServiceManager.getInstance().toast(R.string.replication_abort);
                        break;

                    case Failed:
                        ServiceManager.getInstance().toast(R.string.replication_error);
                        break;
                }

                ReplicatorTask.this.onPostExecute();
            }

        }.execute(this.replicator);
    }
}