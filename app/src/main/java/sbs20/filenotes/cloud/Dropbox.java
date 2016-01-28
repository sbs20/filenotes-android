package sbs20.filenotes.cloud;

import com.dropbox.core.*;
import com.dropbox.core.v2.*;

import sbs20.filenotes.FilenotesApplication;

public class Dropbox extends CloudStorage {

    private String accessToken;

    public Dropbox(FilenotesApplication application, String accessToken) {
        super(application);
        this.accessToken = accessToken;
    }

    public Dropbox sync() {
        String result;
        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        DbxClientV2 client = new DbxClientV2(config, this.accessToken);
        try {
            result = client.users.getCurrentAccount().toJson(true);
        } catch(DbxException e) {
            result = e.toString();
        }

        this.messages.add(result);
        return this;
    }
}

