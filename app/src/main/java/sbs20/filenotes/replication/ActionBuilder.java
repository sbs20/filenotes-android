package sbs20.filenotes.replication;

import java.util.Date;

import sbs20.filenotes.DateTime;
import sbs20.filenotes.model.Logger;

class ActionBuilder {

    private static Action.Type decideActionType(FilePair pair) {

        String tag = ActionBuilder.class.getName();

        if (pair.remote == null) {

            Logger.debug(tag, "decideActionType:" + pair.key() + ":remote is null");
            return Action.Type.Upload;

        } else if (pair.local == null) {

            Logger.debug(tag, "decideActionType:" + pair.key() + ":local is null");
            return Action.Type.Download;

        } else if (pair.areDatesEqual()) {

            if (pair.local.getSize() == pair.remote.getSize()) {
                Logger.debug(tag, "decideActionType:" + pair.key() + ":local and remote same (skip)");
            } else {
                Logger.debug(tag, "decideActionType:" + pair.key() + ":local and remote different sizes (conflict)");
                return Action.Type.ResolveConflict;
            }

        } else if (pair.local.getLastModified().compareTo(pair.remote.getLastModified()) > 0) {

            Logger.debug(tag, "decideActionType:" + pair.key() + ":local is newer");
            return Action.Type.Upload;

        } else if (pair.local.getLastModified().compareTo(pair.remote.getLastModified()) < 0) {

            Logger.debug(tag, "decideActionType:" + pair.key() + ":remote is newer");
            return Action.Type.Download;
        }

        return Action.Type.None;
    }

    private static Action.Type decideActionType(FilePair pair, Date lastSync) {

        String tag = ActionBuilder.class.getName();

        if (pair.local != null) {

            if (pair.local.getLastModified().compareTo(lastSync) <= 0) {
                // Local file unchanged
                if (pair.remote == null) {
                    return Action.Type.DeleteLocal;
                } else if (pair.remote.getLastModified().compareTo(lastSync) > 0) {

                    // TODO Heavy debug - emulator time is out of sync with dropbox...
                    Logger.debug(tag, "decideActionType:lastSync:" + lastSync + ":skip");
                    Logger.debug(tag, "decideActionType:localModified:" + pair.local.getLastModified() + ":skip");
                    Logger.debug(tag, "decideActionType:remoteServer:" + pair.remote.getLastModified() + ":skip");
                    Logger.debug(tag, "decideActionType:remoteClient:" + pair.remote.getClientModified() + ":skip");

                    return Action.Type.Download;
                } else {
                    Logger.debug(tag, "decideActionType:" + pair.key() + ":skip");
                }

            } else {
                // Local file changed
                if (pair.remote == null) {
                    return Action.Type.Upload;
                } else if (pair.remote.getLastModified().compareTo(lastSync) <= 0) {
                    return Action.Type.Upload;
                } else if (pair.remote.getLastModified().compareTo(lastSync) > 0) {
                    return Action.Type.ResolveConflict;
                }
            }
        } else {

            // If the remote file hasn't been touched...
            if (pair.remote.getLastModified().compareTo(lastSync) <= 0) {
                return Action.Type.DeleteRemote;
            } else {
                return Action.Type.Download;
            }

        }

        return Action.Type.None;
    }

    public static Action Create(FilePair pair, Date lastSync) {
        Action.Type type = decideActionType(pair, lastSync);
        return new Action(type, pair);
    }
}
