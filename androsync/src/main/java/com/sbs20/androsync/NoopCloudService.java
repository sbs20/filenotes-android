package com.sbs20.androsync;

import java.io.IOException;
import java.util.List;

public class NoopCloudService implements ICloudService {

    public NoopCloudService() {
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void login() {
    }

    @Override
    public void logout() {
    }

    @Override
    public List<FileItem> files(String remotePath) throws IOException {
        Logger.verbose(this, "files()");
        throw new IOException("NoopService has no remote files");
    }

    @Override
    public void move(FileItem remoteFile, String desiredPath) {
    }

    @Override
    public void upload(FileItem file, String remotePath) {
    }

    @Override
    public void download(FileItem file, String localPath) {
    }

    @Override
    public void delete(FileItem file) {
    }

    @Override
    public List<String> getChildDirectoryPaths(String path) throws Exception {
        return null;
    }

    @Override
    public String getRootDirectoryPath() {
        return null;
    }

    @Override
    public void createDirectory(String path) throws Exception {
    }

    @Override
    public boolean directoryExists(String path) {
        return false;
    }
}
