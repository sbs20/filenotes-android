package sbs20.filenotes.storage;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sbs20.filenotes.R;
import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Logger;
import sbs20.filenotes.model.Settings;

public class DropboxService implements ICloudService, IDirectoryProvider {

    protected ServiceManager serviceManager;
    protected Settings settings;

    private static DbxClientV2 client;

    public DropboxService() {
        this.serviceManager = ServiceManager.getInstance();
        this.settings = serviceManager.getSettings();
    }

    private String appKey() {
        return serviceManager.string(R.string.dropbox_app_key);
    }

    private String clientId() {
        return serviceManager.string(R.string.dropbox_client_identifier);
    }

    private String locale() {
        return serviceManager.string(R.string.dropbox_locale);
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
                DbxRequestConfig config = DbxRequestConfig
                        .newBuilder(clientId())
                        .withUserLocale(locale())
                        .build();

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
        Logger.info(this, "login()");
        if (!this.isAuthenticated()) {
            Logger.verbose(this, "login():!Authenticated");
            Auth.startOAuth2Authentication(serviceManager.getContext(), appKey());
        }
    }

    @Override
    public void logout() {
        client = null;
        AuthActivity.result = null;
        this.settings.clearDropboxAccessToken();
        Logger.info(this, "logout()");
    }

    @Override
    public List<File> files() throws IOException {
        Logger.info(this, "files():Start");

        List<File> files = new ArrayList<>();

        if (this.isAuthenticated()) {
            Logger.verbose(this, "files():Authenticated");

            try {
                ListFolderResult result = client.files().listFolder(this.settings.getRemoteStoragePath());
                while (true) {

                    for (Metadata m : result.getEntries()) {
                        if (m instanceof FileMetadata) {
                            FileMetadata f = (FileMetadata) m;
                            files.add(new File(f));
                        }
                    }

                    if (result.getHasMore()) {
                        result = client.files().listFolderContinue(result.getCursor());
                    } else {
                        break;
                    }
                }
            } catch (DbxException dbxException) {
                throw new IOException(dbxException);
            }

        } else {
            Logger.verbose(this, "files():!Authenticated");
        }

        return files;
    }

    @Override
    public void move(File file, String desiredPath) throws Exception {
        Logger.info(this, "move():Start");
        FileMetadata remoteFile = (FileMetadata) file.getFile();

        if (remoteFile != null) {
            client.files().moveV2(remoteFile.getPathLower(), desiredPath);
            Logger.verbose(this, "move():done");
        }
    }

    @Override
    public void upload(File file) throws Exception {
        Logger.info(this, "upload():Start");
        java.io.File localFile = (java.io.File) file.getFile();

        if (localFile != null) {
            String remoteFolderPath = this.settings.getRemoteStoragePath();

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            InputStream inputStream = new FileInputStream(localFile);
            client.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
            Logger.verbose(this, "upload():done");
        }
    }

    @Override
    public void download(File file, String localName) throws Exception {
        Logger.info(this, "download():Start");
        FileMetadata remoteFile = (FileMetadata) file.getFile();

        if (remoteFile != null) {

            // Local file
            java.io.File localFile = FileSystemService.getInstance().getFile(localName);

            OutputStream outputStream = new FileOutputStream(localFile);

            client.files()
                    .downloadBuilder(remoteFile.getPathLower())
                    .withRev(remoteFile.getRev())
                    .download(outputStream);

            // We will attempt to set the last modified time. This MIGHT help replication
            // and it certainly looks better. However, it doesn't seem to work reliably with
            // external storage. On the plus side it seems fine for internal storage.
            // http://stackoverflow.com/questions/18677438/android-set-last-modified-time-for-the-file
            localFile.setLastModified(remoteFile.getServerModified().getTime());

            Logger.verbose(this, "download():done");
        }
    }

    @Override
    public void download(File file) throws Exception {
        this.download(file, file.getName());
    }

    @Override
    public void delete(File file) throws DbxException {
        Logger.info(this, "delete():Start");
        FileMetadata remoteFile = (FileMetadata) file.getFile();

        if (remoteFile != null) {
            client.files().deleteV2(remoteFile.getPathLower());
            Logger.verbose(this, "delete():done");
        }
    }

    @Override
    public List<String> getChildDirectoryPaths(String path) throws DbxException {
        List<String> dirs = new ArrayList<>();
        Logger.info(this, "getChildDirectoryPaths(" + path + ")");

        if (this.isAuthenticated()) {

            if (!path.equals(this.getRootDirectoryPath())) {
                // We need to add the parent... to do that...
                String parent = path.substring(0, path.lastIndexOf("/"));
                dirs.add(parent);
            }

            ListFolderResult result = client.files().listFolder(path);

            for (Metadata entry : result.getEntries()) {
                if (entry instanceof FolderMetadata) {
                    FolderMetadata folder = (FolderMetadata) entry;
                    dirs.add(folder.getPathLower());
                    Logger.info(this, "getChildDirectoryPaths() - " + folder.getPathLower());
                }
            }

            Collections.sort(dirs);
        }

        return dirs;
    }

    @Override
    public String getRootDirectoryPath() {
        return "";
    }

    @Override
    public void createDirectory(String path) throws Exception {
        Logger.info(this, "createDirectory(" + path + ")");
        if (this.isAuthenticated()) {
            try {
                client.files().createFolderV2(path);
                Logger.verbose(this, "createDirectory():done");
            } catch (CreateFolderErrorException ex) {
                throw new IOException(serviceManager.string(R.string.exception_directory_already_exists));
            }
        }
    }
}
