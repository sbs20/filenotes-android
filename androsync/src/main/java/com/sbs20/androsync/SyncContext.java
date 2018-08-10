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

    public String getLocalPath() {
        return localPath;
    }

    public String getRemotePath() {
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
                path.substring(this.getLocalPath().length()) :
                path.substring(this.getRemotePath().length());

        return asPath(outputPath);
    }

    public String toCommonPath(FileItem file) {
        return this.toCommonPath(file.getPath(), file.isLocal());
    }

    public String toLocalPath(FileItem file) {
        return this.asPath(this.getLocalPath() + this.toCommonPath(file));
    }

    public String toRemotePath(FileItem file) {
        return this.asPath(this.getRemotePath() + this.toCommonPath(file));
    }

    public String toOppositePath(FileItem file) {
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
