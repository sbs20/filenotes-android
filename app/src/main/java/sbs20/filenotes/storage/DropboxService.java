package sbs20.filenotes.storage;

import com.dropbox.core.*;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sbs20.filenotes.ServiceManager;
import sbs20.filenotes.model.Logger;

public class DropboxService extends CloudService implements IDirectoryListProvider {

    private static final String APP_KEY = "q1p3jfhnraz1k7l";
    private static final String CLIENT_IDENTIFER = "sbs20.filenotes/1.0";
    private static final String LOCALE = "en_UK";

    private static DbxClientV2 client;

    public DropboxService(ServiceManager serviceManager) {
        super(serviceManager);
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
                DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFER, LOCALE);
                client = new DbxClientV2(config, accessToken);
            }
        }

        // It's still possible that client is null...
        return client;
    }

    private boolean isAuthenticated() {
        return this.getClient() != null;
    }

    @Override
    public void login() {
        Logger.info(this, "login()");
        if (!this.isAuthenticated()) {
            Logger.verbose(this, "login():!Authenticated");
            Auth.startOAuth2Authentication(this.serviceManager.getContext(), APP_KEY);
        }
    }

    @Override
    public void logout() {
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
                DbxFiles.ListFolderResult result = client.files.listFolder(this.settings.getRemoteStoragePath());
                while (true) {

                    for (DbxFiles.Metadata m : result.entries) {
                        if (m instanceof DbxFiles.FileMetadata) {
                            DbxFiles.FileMetadata f = (DbxFiles.FileMetadata) m;
                            files.add(new File(f));
                        }
                    }

                    if (result.hasMore) {
                        result = client.files.listFolderContinue(result.cursor);
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
    public void upload(File file) throws Exception {
        Logger.info(this, "upload():Start");
        java.io.File localFile = (java.io.File) file.getFile();

        if (localFile != null) {
            String remoteFolderPath = this.settings.getRemoteStoragePath();

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            InputStream inputStream = new FileInputStream(localFile);
            client.files.uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                    .mode(DbxFiles.WriteMode.overwrite())
                    .run(inputStream);
            Logger.verbose(this, "upload():done");
        }
    }

    @Override
    public void download(File file, String localName) throws Exception {
        Logger.info(this, "download():Start");
        DbxFiles.FileMetadata remoteFile = (DbxFiles.FileMetadata) file.getFile();

        if (remoteFile != null) {

            // Local file
            java.io.File localFile = new FileSystemService().getFile(localName);

            OutputStream outputStream = new FileOutputStream(localFile);

            client.files
                    .downloadBuilder(remoteFile.pathLower)
                    .rev(remoteFile.rev)
                    .run(outputStream);

            // For cosmetic purposes we will attempt to set the last modified time
            // That said it doesn't seem to work. Shame!
            // http://stackoverflow.com/questions/18677438/android-set-last-modified-time-for-the-file
            localFile.setLastModified(remoteFile.serverModified.getTime());

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
        DbxFiles.FileMetadata remoteFile = (DbxFiles.FileMetadata) file.getFile();

        if (remoteFile != null) {
            client.files.delete(remoteFile.pathLower);
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

            DbxFiles.ListFolderResult result = client.files.listFolder(path);

            for (DbxFiles.Metadata entry : result.entries) {
                if (entry instanceof DbxFiles.FolderMetadata) {
                    DbxFiles.FolderMetadata folder = (DbxFiles.FolderMetadata) entry;
                    dirs.add(folder.pathLower);
                    Logger.info(this, "getChildDirectoryPaths() - " + folder.pathLower);
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
}
