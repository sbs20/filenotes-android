package sbs20.filenotes.replication;

import sbs20.filenotes.R;
import sbs20.filenotes.ServiceManager;

public class Action {

    public enum Type {
        None,
        Download,
        Upload,
        DeleteLocal,
        DeleteRemote,
        ResolveConflict
    }

    Type type;
    FilePair filePair;

    public Action(Type type, FilePair filePair) {
        this.type = type;
        this.filePair = filePair;
    }

    private String toString(Type type) {
        switch (type) {
            case None:
                return ServiceManager.getInstance().getString(R.string.replication_none);
            case Download:
                return ServiceManager.getInstance().getString(R.string.replication_download);
            case Upload:
                return ServiceManager.getInstance().getString(R.string.replication_upload);
            case DeleteLocal:
                return ServiceManager.getInstance().getString(R.string.replication_delete_local);
            case DeleteRemote:
                return ServiceManager.getInstance().getString(R.string.replication_delete_remote);
            case ResolveConflict:
                return ServiceManager.getInstance().getString(R.string.replication_resolve_conflict);
            default:
                return ServiceManager.getInstance().getString(R.string.replication_unknown);
        }
    }

    public String key() {
        return this.filePair.key();
    }

    public String message() {
        return toString(this.type) + ": " + this.key();
    }
}
