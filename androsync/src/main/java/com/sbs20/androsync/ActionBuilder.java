package com.sbs20.androsync;

import java.util.Date;

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
                    Logger.debug(tag, "decideActionType:" + pair.key() + ":deleteLocal");
                    return Action.Type.DeleteLocal;
                } else if (pair.remote.getLastModified().compareTo(lastSync) > 0) {
                    Logger.debug(tag, "decideActionType:" + pair.key() + ":download");

                    // Heavy debug - time is out of sync with dropbox?...
                    Logger.verbose(tag, "decideActionType:lastSync:" + lastSync);
                    Logger.verbose(tag, "decideActionType:localModified:" + pair.local.getLastModified());
                    Logger.verbose(tag, "decideActionType:remoteServer:" + pair.remote.getLastModified());
                    Logger.verbose(tag, "decideActionType:remoteClient:" + pair.remote.getClientModified());

                    return Action.Type.Download;
                } else {
                    Logger.debug(tag, "decideActionType:" + pair.key() + ":none (skip)");
                    return Action.Type.None;
                }

            } else {
                // Local file changed
                if (pair.remote == null) {
                    Logger.debug(tag, "decideActionType:" + pair.key() + ":upload");
                    return Action.Type.Upload;
                } else if (pair.remote.getLastModified().compareTo(lastSync) <= 0) {
                    Logger.debug(tag, "decideActionType:" + pair.key() + ":upload");
                    return Action.Type.Upload;
                } else if (pair.remote.getLastModified().compareTo(lastSync) > 0) {
                    Logger.debug(tag, "decideActionType:" + pair.key() + ":resolve");
                    return Action.Type.ResolveConflict;
                }
            }
        } else {

            // If the remote file hasn't been touched...
            if (pair.remote.getLastModified().compareTo(lastSync) <= 0) {
                Logger.debug(tag, "decideActionType:" + pair.key() + ":deleteRemote");
                return Action.Type.DeleteRemote;
            } else {
                Logger.debug(tag, "decideActionType:" + pair.key() + ":download");
                return Action.Type.Download;
            }

        }

        Logger.debug(tag, "decideActionType:" + pair.key() + ":none");
        return Action.Type.None;
    }

    public static Action Create(FilePair pair, Date lastSync) {
        Action.Type type = decideActionType(pair, lastSync);
        return new Action(type, pair);
    }
}
