package com.sbs20.filenotes;

import android.os.AsyncTask;

import com.sbs20.androsync.Action;
import com.sbs20.androsync.IObserver;
import com.sbs20.androsync.Sync;

import sbs20.filenotes.R;

public class ReplicatorTask  {

    protected Sync replicator;

    public ReplicatorTask() {
        this.replicator = ServiceManager.getInstance().getSync();
    }

    protected void onProgressUpdate(Action action) {
    }

    protected void onPostExecute() {
    }

    public void execute() {
        new AsyncTask<Sync, Action, Sync>() {

            @Override
            protected Sync doInBackground(Sync... params) {
                Sync replicator = params[0];
                replicator.invoke(new IObserver() {
                    @Override
                    public void update(Sync source, Action action) {
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
            protected void onPostExecute(Sync replicator) {
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
