package sbs20.filenotes.storage;

import java.io.IOException;
import java.util.List;

import sbs20.filenotes.model.Logger;

public class NoopCloudService implements ICloudService {

    public NoopCloudService() {
    }

    @Override
    public void login() {
    }

    @Override
    public void logout() {
    }

    @Override
    public List<File> files() throws IOException {
        Logger.verbose(this, "files()");
        throw new IOException("NoopService has no remote files");
    }

    @Override
    public void move(File file, String desiredPath) {
    }

    @Override
    public void upload(File file) {
    }

    @Override
    public void download(File file) {
    }

    @Override
    public void download(File file, String localName) {
    }

    @Override
    public void delete(File file) {
    }
}
