package sbs20.filenotes.storage;

import java.util.List;

public interface IDirectoryListProvider {
    List<String> getChildDirectoryPaths(String path);
    String getRootDirectoryPath();
}
