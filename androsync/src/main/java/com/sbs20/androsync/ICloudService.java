package com.sbs20.androsync;

import java.io.IOException;
import java.util.List;

public interface ICloudService extends IDirectoryProvider {
    boolean isAuthenticated();
    void login();
    void logout();
    List<FileItem> files(String remotePath) throws IOException;
    void move(FileItem remoteFile, String desiredName) throws Exception;
    void upload(FileItem localFile, String remotePath) throws Exception;
    void download(FileItem remoteFile, String localPath) throws Exception ;
    void delete(FileItem remoteFile) throws Exception ;
}
