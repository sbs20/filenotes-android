package sbs20.filenotes.replication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sbs20.filenotes.DateTime;
import sbs20.filenotes.R;
import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.storage.ICloudService;
import sbs20.filenotes.storage.File;
import sbs20.filenotes.storage.FileSystemService;

public class Replicator {

    private static boolean isRunning = false;
    private FilePairCollection files;
    private ICloudService cloudService;
    private List<Action> actions;

    private List<IObserver> observers;

    public Replicator() {
        files = new FilePairCollection();
        cloudService = ServiceManager.getInstance().getCloudService();
        observers = new ArrayList<>();
        actions = new ArrayList<>();
    }

    private void loadFiles() throws IOException {
        for (java.io.File file : new FileSystemService().readAllFilesFromStorage()) {
            files.add(new File(file));
        }

        for (File file : cloudService.files()) {
            files.add(file);
        }
    }

    private void raiseEvent(Action action) {
        for (IObserver observer : this.observers) {
            observer.update(this, action);
        }
    }

    private void doAction(Action action) throws Exception {
        Logger.info(this, "doAction(" + action.key() + ")");

        this.raiseEvent(action);

        switch (action.type) {
            case DeleteLocal:
                new FileSystemService().delete(action.filePair.local.getName());
                break;

            case Download:
                cloudService.download(action.filePair.remote);
                break;

            case DeleteRemote:
                cloudService.delete(action.filePair.remote);
                break;

            case Upload:
                cloudService.upload(action.filePair.local);
                break;

            case ResolveConflict:
                // We already have the local file. So download the server one but call it <file>.server-conflict
                // TODO - this is not really complete
                cloudService.download(action.filePair.remote, action.filePair.local.getName() +
                        ServiceManager.getInstance().getString(R.string.replication_conflict_extension));

                break;
        }
    }

    private void analyse() throws Exception {
        // Load all files
        this.loadFiles();

        // Look at everything that's happened since the last sync
        Date lastSync = ServiceManager.getInstance().getSettings().getLastSync();
        Logger.debug(this, "sync (previous success:" + DateTime.to8601String(lastSync) + ")");

        // Make decisions about each filePair...
        for (FilePair pair : files) {
            Action action = ActionBuilder.Create(pair, lastSync);

            if (action.type != Action.Type.None) {
                actions.add(action);
            }
        }
    }

    public void invoke() {

        if (!isRunning) {
            isRunning = true;

            Logger.info(this, "invoke()");

            try {
                this.analyse();

                // Now actually do stuff...
                for (Action action : this.actions) {
                    this.doAction(action);
                }

                // Pause briefly. If the local clock is slightly behind the cloud server's then
                // then the next time we replicate we might end up downloading something we just
                // uploaded. This doesn't fix it, but it might help a bit and is mostly harmless
                Thread.sleep(500);

                // Files just downloaded will have new dates - and there is no workaround to this. So
                // we need to record the date time as of now to avoid unnecessary uploading of files
                Date now = DateTime.now();
                ServiceManager.getInstance().getSettings().setLastSync(now);

                // Also clear the need for further replications
                ServiceManager.getInstance().getNotesManager().setReplicationRequired(false);

            } catch (Exception ex) {
                Logger.debug(this, "invoke():error:loadRemoteFiles:" + ex.toString());
                ServiceManager.getInstance().toast(R.string.replication_error);
            }

            isRunning = false;
        } else {
            Logger.debug(this, "invoke():already running!");
        }
    }

    public int getActionCount() {
        return this.actions.size();
    }

    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }
}