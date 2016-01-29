package sbs20.filenotes.cloud;

import com.dropbox.core.*;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.*;

import sbs20.filenotes.FilenotesApplication;

public class DropboxSync extends CloudSync {

    private static final String AppKey = "q1p3jfhnraz1k7l";
    private static DbxClientV2 client;

    public DropboxSync(FilenotesApplication application) {
        super(application);
    }

    private String getAuthenticationToken() {
        String accessToken = this.settings.getDropboxAccessToken();

        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                this.settings.setDropboxAccessToken(accessToken);
            }
        }

        return accessToken;
    }

    private DbxClientV2 getClient() {
        if (client == null) {
            String accessToken = this.getAuthenticationToken();
            if (accessToken != null && accessToken.length() > 0) {
                DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
                client = new DbxClientV2(config, accessToken);
            }
        }

        // It's still possible that client is null...
        return client;
    }

    @Override
    public boolean isAuthenticated() {
        return this.getClient() != null;
    }

    @Override
    public void login() {
        if (!this.isAuthenticated()) {
            this.getLogger().verbose(this, "login():!Authenticated");
            Auth.startOAuth2Authentication(this.application, AppKey);
        }
    }

    @Override
    public void logout() {
        this.settings.clearDropboxAccessToken();
        this.getLogger().verbose(this, "logout()");
    }

    @Override
    public DropboxSync trySync() {
        this.getLogger().verbose(this, "trySync():Start");

        if (this.isAuthenticated()) {
            this.getLogger().verbose(this, "trySync():Authenticated");
            String result;

            try {
                result = client.users.getCurrentAccount().toJson(true);
            } catch(DbxException e) {
                result = e.toString();
            } catch (Exception e) {
                result = e.toString();
            }

            this.getLogger().verbose(this, "trySync():Response" + result);
            this.messages.add(result);
        } else {
            this.getLogger().verbose(this, "trySync():!Authenticated");
        }

        return this;
    }
}
