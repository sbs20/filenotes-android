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
    public List<File> files(String remotePath) throws IOException {
        Logger.verbose(this, "files()");
        throw new IOException("NoopService has no remote files");
    }

    @Override
    public void move(File remoteFile, String desiredPath) {
    }

    @Override
    public void upload(File file, String remotePath) {
    }

    @Override
    public void download(File file, String localPath) {
    }

    @Override
    public void delete(File file) {
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
