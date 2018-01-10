package com.sbs20.androsync;

public class SyncContext {
    private FileSystemService localFilesystem;
    private ICloudService remoteFilesystem;

    private String localPath;
    private String remotePath;
    private String conflictExtension;

    public SyncContext(
            FileSystemService localFilesystem,
            ICloudService remoteFilesystem,
            String localPath,
            String remotePath,
            String conflictExtension) {

        this.localFilesystem = localFilesystem;
        this.remoteFilesystem = remoteFilesystem;
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.conflictExtension = conflictExtension;
    }

    public String getLocalStoragePath() {
        return localPath;
    }

    public String getRemoteStoragePath() {
        return remotePath;
    }

    public String getConflictExtension() {
        return conflictExtension;
    }

    private String asPath(String path) {
        return path == null ? null : path.replace("\\", "/");
    }

    private String toCommonPath(String path, boolean isLocal) {
        String outputPath = isLocal ?
                path.substring(this.getLocalStoragePath().length()) :
                path.substring(this.getRemoteStoragePath().length());

        return asPath(outputPath);
    }

    public String toCommonPath(File file) {
        return this.toCommonPath(file.getPath(), file.isLocal());
    }

    public String toLocalPath(File file) {
        return this.asPath(this.getLocalStoragePath() + this.toCommonPath(file));
    }

    public String toRemotePath(File file) {
        return this.asPath(this.getRemoteStoragePath() + this.toCommonPath(file));
    }

    public String toOppositePath(File file) {
        return file.isLocal() ?
                this.toRemotePath(file) :
                this.toLocalPath(file);
    }

    public FileSystemService getLocalFilesystem() {
        return localFilesystem;
    }

    public ICloudService getRemoteFilesystem() {
        return this.remoteFilesystem;
    }
}
