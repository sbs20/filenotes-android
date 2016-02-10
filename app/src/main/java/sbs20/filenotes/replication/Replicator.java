package sbs20.filenotes.replication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import sbs20.filenotes.DateTime;
import sbs20.filenotes.R;
import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.Settings;
import sbs20.filenotes.storage.ICloudService;
import sbs20.filenotes.storage.File;
import sbs20.filenotes.storage.FileSystemService;

public class Replicator {

    private static Replicator instance;

    private AtomicBoolean isRunning;
    private AtomicBoolean isCancelled;

    private FilePairCollection files;
    private ICloudService cloudService;
    private List<Action> actions;
    private List<IObserver> observers;

    private Replicator() {
        isRunning = new AtomicBoolean(false);
        isCancelled = new AtomicBoolean(false);
        initialise();
    }

    private void initialise() {
        files = new FilePairCollection();
        cloudService = ServiceManager.getInstance().getCloudService();
        observers = new ArrayList<>();
        actions = new ArrayList<>();
    }

    public static Replicator getInstance() {
        if (instance == null) {
            instance = new Replicator();
        }
        return instance;
    }

    private void recordLast() {
        Settings settings = ServiceManager.getInstance().getSettings();
        Date now = DateTime.now();
        settings.setLastSync(now);
    }

    private void scheduleNext() {
        Settings settings = ServiceManager.getInstance().getSettings();
        Date now = DateTime.now();
        Date next = new Date(now.getTime() + settings.replicationIntervalInMilliseconds());
        settings.setNextSync(next);
    }

    public boolean shouldRun() {
        Logger.debug(this, "shouldRun()");
        Settings settings = ServiceManager.getInstance().getSettings();
        Date nextSync = settings.getNextSync();
        Date now = DateTime.now();

        Logger.verbose(this, "shouldRun():next=" + DateTime.to8601String(nextSync));
        if (nextSync.before(now)) {
            return true;
        }

        if (settings.isReplicationOnChange() && ServiceManager.getInstance().getNotesManager().isChanged()) {
            return true;
        }

        return false;
    }

    private void loadFiles() throws IOException {
        for (java.io.File file : FileSystemService.getInstance().readAllFilesFromStorage()) {
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

    private void listenForCancel() throws Exception {
        if (isCancelled.get()) {
            // Handled... reset
            isCancelled.set(false);

            // Now abort
            throw new Exception(ServiceManager.getInstance().getString(R.string.replication_abort));
        }
    }

    private void resolveConflict(FilePair filePair) throws Exception {
        // We have two "new" versions of a file. We have to take a brute force approach
        Logger.info(this, "resolveConflict(" + filePair.key() + ")");

        // We're going to download an alternate version : <filename>.conflict
        String tempFilepath = filePair.local.getName() +
                ServiceManager.getInstance().getString(R.string.replication_conflict_extension);

        // Download the server version
        cloudService.download(filePair.remote, tempFilepath);

        // Compare the files
        FileSystemService fileSystemService = FileSystemService.getInstance();
        java.io.File tempFile = fileSystemService.getFile(tempFilepath);
        java.io.File localFile = (java.io.File)filePair.local.getFile();
        boolean filesEqual = fileSystemService.filesEqual(localFile, tempFile);

        // Act
        if (filesEqual) {

            Logger.info(this, "resolveConflict(" + filePair.key() + "):files equal");

            // Everything is good. Just delete the tempfile
            fileSystemService.delete(tempFilepath);

        } else {

            Logger.info(this, "resolveConflict(" + filePair.key() + "):files not equal");

            // Rename the server version to the conflict
            String serverConflictPath = filePair.remote.getPath() +
                    ServiceManager.getInstance().getString(R.string.replication_conflict_extension);

            cloudService.move(filePair.remote, serverConflictPath);

            // Upload the local version
            cloudService.upload(filePair.local);
        }
    }

    private void doAction(Action action) throws Exception {
        Logger.info(this, "doAction(" + action.key() + ")");

        this.raiseEvent(action);

        switch (action.type) {
            case DeleteLocal:
                FileSystemService.getInstance().delete(action.filePair.local.getName());
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
                resolveConflict(action.filePair);
                break;
        }
    }

    private void analyse() throws Exception {
        // Load all files
        this.loadFiles();

        this.listenForCancel();

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

    private void process() throws Exception {
        // Now actually do stuff...
        for (Action action : this.actions) {
            this.doAction(action);
            this.listenForCancel();
        }
    }

    private void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    public void invoke(IObserver observer) {

        if (isRunning.compareAndSet(false, true)) {

            Logger.info(this, "invoke()");

            try {
                this.initialise();
                this.addObserver(observer);
                this.analyse();
                this.process();

                // Files just downloaded will have new dates - and there is no workaround to this. So
                // we need to record the date time as of now to avoid unnecessary uploading of files
                this.recordLast();
                this.scheduleNext();

                // Also clear the need for further replications
                ServiceManager.getInstance().getNotesManager().clearChange();

            } catch (Exception ex) {
                Logger.debug(this, "invoke():error:" + ex.toString());
                //ServiceManager.getInstance().toast(R.string.replication_error);
                if (ServiceManager.getInstance().getSettings().replicationSkipError()) {
                    this.scheduleNext();
                }
            }

            isRunning.set(false);
        } else {
            Logger.debug(this, "invoke():already running!");
        }
    }

    public void cancel() {
        isCancelled.set(true);
    }

    public void awaitStop() {
        while (isRunning.get()) {
            Logger.debug(this, "awaitStop():waiting");
            try { Thread.sleep(125); } catch (Exception e) {}
        }
    }

    public int getActionCount() {
        return this.actions.size();
    }
}