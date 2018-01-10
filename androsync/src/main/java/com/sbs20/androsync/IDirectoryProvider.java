package com.sbs20.androsync;

import java.util.List;

public interface IDirectoryProvider {
    List<String> getChildDirectoryPaths(String path) throws Exception;
    String getRootDirectoryPath();
    void createDirectory(String path) throws Exception;
    boolean directoryExists(String path);
}
