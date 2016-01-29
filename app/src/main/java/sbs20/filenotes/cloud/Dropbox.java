package sbs20.filenotes.cloud;

import android.content.SharedPreferences;

import com.dropbox.core.*;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.*;

import sbs20.filenotes.FilenotesApplication;
import sbs20.filenotes.SettingsPreferenceActivity;

public class Dropbox extends CloudStorage {

    private static final String AppKey = "q1p3jfhnraz1k7l";
    private static DbxClientV2 client;

    public Dropbox(FilenotesApplication application) {
        super(application);
    }

    private String getAuthenticationToken() {
        SharedPreferences prefs = this.application.getPreferences();
        String accessToken = prefs.getString(SettingsPreferenceActivity.KEY_DROPBOX_ACCESS_TOKEN, null);

        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                prefs.edit().putString(SettingsPreferenceActivity.KEY_DROPBOX_ACCESS_TOKEN, accessToken).apply();
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
            Auth.startOAuth2Authentication(this.application, AppKey);
        }
    }

    @Override
    public void logout() {
        // TODO
        this.application.getPreferences().edit().remove(SettingsPreferenceActivity.KEY_DROPBOX_ACCESS_TOKEN).commit();
    }

    @Override
    public Dropbox trySync() {
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
