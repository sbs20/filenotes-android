package com.sbs20.androsync;

import java.io.IOException;
import java.util.List;

public interface ICloudService extends IDirectoryProvider {
    boolean isAuthenticated();
    void login();
    void logout();
    List<File> files(String remotePath) throws IOException;
    void move(File remoteFile, String desiredName) throws Exception;
    void upload(File localFile, String remotePath) throws Exception;
    void download(File remoteFile, String localPath) throws Exception ;
    void delete(File remoteFile) throws Exception ;
}
