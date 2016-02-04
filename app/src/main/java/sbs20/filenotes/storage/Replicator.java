package sbs20.filenotes.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sbs20.filenotes.DateTime;
import sbs20.filenotes.R;
import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Logger;

public class Replicator {

    public enum ActionType {
        LocalUpdate,
        LocalDelete,
        RemoteUpdate,
        RemoteDelete,
        ResolveConflict
    }

    public class Action {
        private ActionType type;
        private File localFile;
        private File remoteFile;

        public Action(ActionType type, File localFile, File remoteFile) {
            this.type = type;
            this.localFile = localFile;
            this.remoteFile = remoteFile;
        }

        private String toString(ActionType type) {
            switch (type) {
                case LocalDelete:
                    return ServiceManager.getInstance().getString(R.string.replication_delete_local);
                case LocalUpdate:
                    return ServiceManager.getInstance().getString(R.string.replication_download);
                case RemoteDelete:
                    return ServiceManager.getInstance().getString(R.string.replication_delete_remote);
                case RemoteUpdate:
                    return ServiceManager.getInstance().getString(R.string.replication_upload);
                case ResolveConflict:
                    return ServiceManager.getInstance().getString(R.string.replication_resolve_conflict);
                default:
                    return ServiceManager.getInstance().getString(R.string.replication_unknown);
            }
        }

        public String filename() {
            return this.localFile != null ? this.localFile.getName() : this.remoteFile.getName();
        }

        public String message() {
            return toString(this.type) + ": " + this.filename();
        }
    }

    public interface IReplicatorObserver {
        void update(Replicator source, Action action);
    }

    private static boolean isRunning = false;
    private List<File> localFiles;
    private List<File> remoteFiles;
    private CloudService cloudService;
    private List<Action> actions;

    private List<IReplicatorObserver> observers;

    public Replicator() {
        this.localFiles = new ArrayList<>();
        this.remoteFiles = new ArrayList<>();
        this.cloudService = ServiceManager.getInstance().getCloudService();
        this.observers = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    private void add(java.io.File localfile) {
        this.localFiles.add(new File(localfile));
    }

    private void add(java.io.File[] localFiles) {
        for (java.io.File f : localFiles) {
            this.add(f);
        }
    }

    private void loadLocalFiles() {
        this.add(new FileSystemService().readAllFilesFromStorage());
    }

    private void add(File file) {
        this.remoteFiles.add(file);
    }

    private void add(List<File> files) {
        for (File file : files) {
            this.add(file);
        }
    }

    private void loadRemoteFiles() throws IOException {
        this.add(cloudService.files());
    }

    private File findLocal(File remoteFile) {
        for (File localFile : this.localFiles) {
            if (localFile.getName().equals(remoteFile.getName())) {
                return localFile;
            }
        }
        return null;
    }

    private File findRemote(File localFile) {
        for (File remoteFile : this.remoteFiles) {
            if (remoteFile.getName().equals(localFile.getName())) {
                return remoteFile;
            }
        }
        return null;
    }

    private void raiseEvent(Action action) {
        for (IReplicatorObserver observer : this.observers) {
            observer.update(this, action);
        }
    }

    private void doAction(Action action) throws Exception {
        Logger.info(this, "doAction(" + action.filename() + ")");

        this.raiseEvent(action);

        switch (action.type) {
            case LocalDelete:
                new FileSystemService().delete(action.localFile.getName());
                break;

            case LocalUpdate:
                cloudService.download(action.remoteFile);
                break;

            case RemoteDelete:
                cloudService.delete(action.remoteFile);
                break;

            case RemoteUpdate:
                cloudService.upload(action.localFile);
                break;

            case ResolveConflict:
                // We already have the local file. So download the server one but call it <file>.server-conflict
                // TODO - this is not really complete
                cloudService.download(action.remoteFile, action.localFile.getName() +
                        ServiceManager.getInstance().getString(R.string.replication_conflict_extension));

                break;
        }
    }

    private void firstAnalysis() throws Exception {
        Logger.info(this, "firstAnalysis:Start");

        // Start with local stuff
        for (File localFile : this.localFiles) {

            // Get remote
            File remoteFile = this.findRemote(localFile);

            if (remoteFile == null) {

                Logger.debug(this, "firstAnalysis:" + localFile.getName() + ":remote is null");
                this.actions.add(new Action(ActionType.RemoteUpdate, localFile, remoteFile));

            } else if (localFile.getLastModified().compareTo(remoteFile.getLastModified()) > 0) {

                Logger.debug(this, "firstAnalysis:" + localFile.getName() + ":local is newer");
                this.actions.add(new Action(ActionType.RemoteUpdate, localFile, remoteFile));

            } else if (localFile.getLastModified().compareTo(remoteFile.getLastModified()) == 0) {

                Logger.debug(this, "firstAnalysis:" + localFile.getName() + ":local and remote same age");
                if (localFile.getSize() == remoteFile.getSize()) {
                    Logger.debug(this, "firstAnalysis:" + localFile.getName() + ":local and remote same size");
                } else {
                    Logger.debug(this, "firstAnalysis:" + localFile.getName() + ":local and remote different sizes");
                    this.actions.add(new Action(ActionType.ResolveConflict, localFile, remoteFile));
                }

            } else if (localFile.getLastModified().compareTo(remoteFile.getLastModified()) < 0) {

                Logger.debug(this, "firstAnalysis:" + localFile.getName() + ":remote is newer");
                this.actions.add(new Action(ActionType.LocalUpdate, localFile, remoteFile));
            }
        }

        // Now remote
        for (File remoteFile : this.remoteFiles) {

            // Get local
            File localFile = this.findLocal(remoteFile);

            if (localFile == null) {
                // We only need to deal with the ones where there isn't a local file
                Logger.debug(this, "firstAnalysis:" + remoteFile.getName() + ":local is null");
                this.actions.add(new Action(ActionType.LocalUpdate, localFile, remoteFile));
            }
        }
    }

    private void incrementalAnalysis(Date lastSync) throws Exception {

        // Start with local stuff
        for (File localFile : this.localFiles) {

            // Get remote
            File remoteFile = this.findRemote(localFile);

            if (localFile.getLastModified().compareTo(lastSync) <= 0) {
                // Local file unchanged
                if (remoteFile == null) {
                    this.actions.add(new Action(ActionType.LocalDelete, localFile, remoteFile));
                } else if (remoteFile.getLastModified().compareTo(lastSync) > 0) {
                    this.actions.add(new Action(ActionType.LocalUpdate, localFile, remoteFile));
                } else {
                    Logger.debug(this, "incrementalAnalysis:" + localFile.getName() + ":skip");
                }

            } else {
                // Local file changed
                if (remoteFile == null) {
                    this.actions.add(new Action(ActionType.RemoteUpdate, localFile, remoteFile));
                } else if (remoteFile.getLastModified().compareTo(lastSync) <= 0) {
                    this.actions.add(new Action(ActionType.RemoteUpdate, localFile, remoteFile));
                } else if (remoteFile.getLastModified().compareTo(lastSync) > 0) {
                    this.actions.add(new Action(ActionType.ResolveConflict, localFile, remoteFile));
                }
            }
        }

        // Now remote
        for (File remoteFile : this.remoteFiles) {

            // Get local
            File localFile = this.findLocal(remoteFile);

            // We only care if the local file is null (we dealt with the others above)
            if (localFile == null) {
                // If the remote file hasn't been touched...
                if (remoteFile.getLastModified().compareTo(lastSync) <= 0) {
                    this.actions.add(new Action(ActionType.RemoteDelete, localFile, remoteFile));
                } else {
                    this.actions.add(new Action(ActionType.LocalUpdate, localFile, remoteFile));
                }
            }
        }
    }

    private void analyse() throws Exception {
        // Load local files
        this.loadLocalFiles();

        // Load remote files
        this.loadRemoteFiles();

        // Look at everything that's happened since the last sync
        Date lastSync = ServiceManager.getInstance().getSettings().getLastSync();

        if (lastSync.equals(DateTime.min())) {
            // This is the first sync. We lack data. One would hope that either one or both
            // nodes of this is completely empty. If both are populated then we don't delete
            // anything and we go last localChange wins
            Logger.debug(this, "First sync");
            this.firstAnalysis();

        } else {

            Logger.debug(this, "sync (previous success:" + DateTime.to8601String(lastSync) + ")");
            this.incrementalAnalysis(lastSync);
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

                // Files just downloaded will have new dates - and there is no workaround to this. So
                // we need to record the date time as of now to avoid unnecessary uploading of files
                Date now = DateTime.now();
                ServiceManager.getInstance().getSettings().setLastSync(now);

                // Also clear the need for further replications
                ServiceManager.getInstance().getNotesManager().setReplicationRequired(false);

            } catch (Exception ex) {
                Logger.debug(this, "invoke():error:loadRemoteFiles:" + ex.toString());
            }

            isRunning = false;
        } else {
            Logger.debug(this, "invoke():already running!");
        }
    }

    public int getActionCount() {
        return this.actions.size();
    }

    public void addObserver(IReplicatorObserver observer) {
        this.observers.add(observer);
    }
}