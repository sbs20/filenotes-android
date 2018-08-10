package com.sbs20.androsync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Sync {

    public enum Status {
        Idle,
        Running,
        Succeeded,
        Aborted,
        Failed
    }

    private AtomicBoolean isRunning;
    private AtomicBoolean isCancelled;

    private FilePairCollection files;
    private ICloudService cloudService;
    private List<Action> actions;
    private List<IObserver> observers;

    private ISettings settings;
    private SyncContext context;
    private Status status;

    public Sync(ISettings settings, SyncContext context) {
        isRunning = new AtomicBoolean(false);
        isCancelled = new AtomicBoolean(false);

        this.settings = settings;
        this.context = context;
    }

    private void initialise() {
        this.status = Status.Idle;
        this.files = new FilePairCollection();
        this.cloudService = this.context.getRemoteFilesystem();
        this.observers = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    public Status getStatus() {
        return this.status;
    }

    private void recordLast() {
        Date now = DateTime.now();
        settings.setLastSync(now);
    }

    private void scheduleNext() {
        Date now = DateTime.now();
        Date next = new Date(now.getTime() + settings.replicationIntervalInMilliseconds());
        settings.setNextSync(next);
    }

    private void validate() throws Exception {
        if (!this.cloudService.directoryExists(this.context.getRemotePath())) {
            throw new IOException("Remote directory does not exist");
        }

        if (this.context.getRemotePath().equals(this.cloudService.getRootDirectoryPath())) {
            throw new Exception("Remote directory must not be root");
        }
    }

    private void loadFiles() throws IOException {
        for (java.io.File file : this.context.getLocalFilesystem()
                .readAllFilesFromStorage(this.context.getLocalPath())) {
            files.add(new FileItem(file));
        }

        for (FileItem file : cloudService.files(this.context.getRemotePath())) {
            files.add(file);
        }
    }

    private void raiseEvent(Action action) {
        for (IObserver observer : this.observers) {
            observer.update(this, action);
        }
    }

    private void listenForCancel() {
        if (isCancelled.get()) {
            // Handled... reset
            isCancelled.set(false);

            // Now abort
            this.status = Status.Aborted;
        }
    }

    private void resolveConflict(FilePair filePair) throws Exception {
        // We have two "new" versions of a file. We have to take a brute force approach
        Logger.info(this, "resolveConflict(" + filePair.key() + ")");

        // We're going to download an alternate version : <filename>.conflict
        String tempFilepath = this.context.getLocalPath() +
                filePair.local.getName() +
                this.context.getConflictExtension();

        // Download the server version
        cloudService.download(filePair.remote, tempFilepath);

        // Compare the files
        FileSystemService fileSystemService = this.context.getLocalFilesystem();
        java.io.File localFile = (java.io.File)filePair.local.getFile();
        java.io.File tempFile = new java.io.File(tempFilepath);
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
                    this.context.getConflictExtension();

            cloudService.move(filePair.remote, serverConflictPath);

            // Upload the local version
            cloudService.upload(
                    filePair.local,
                    this.context.toOppositePath(filePair.local));
        }
    }

    private void doAction(Action action) throws Exception {
        Logger.info(this, "doAction(" + action.key() + ")");

        this.raiseEvent(action);

        switch (action.type) {
            case DeleteLocal:
                this.context.getLocalFilesystem().delete(action.filePair.local.getPath());
                break;

            case Download:
                cloudService.download(
                        action.filePair.remote,
                        this.context.toOppositePath(action.filePair.remote));
                break;

            case DeleteRemote:
                cloudService.delete(action.filePair.remote);
                break;

            case Upload:
                cloudService.upload(
                        action.filePair.local,
                        this.context.toOppositePath(action.filePair.local));
                break;

            case ResolveConflict:
                resolveConflict(action.filePair);
                break;
        }
    }

    private void analyse() throws Exception {
        this.validate();

        this.loadFiles();

        this.listenForCancel();

        // Look at everything that's happened since the last sync
        Date lastSync = settings.getLastSync();
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
            this.status = Status.Running;

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

                this.status = Status.Succeeded;

            } catch (Exception ex) {
                Logger.debug(this, "invoke():error:" + ex.toString());
                this.status = Status.Failed;

                if (settings.replicationSkipError()) {
                    this.scheduleNext();
                }
            }

            this.status = Status.Succeeded;
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