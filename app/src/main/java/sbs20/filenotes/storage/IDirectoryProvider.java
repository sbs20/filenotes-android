package sbs20.filenotes.storage;

import java.util.List;

public interface IDirectoryProvider {
    List<String> getChildDirectoryPaths(String path) throws Exception;
    String getRootDirectoryPath();
    void createDirectory(String path) throws Exception;
}
