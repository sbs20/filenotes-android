package sbs20.filenotes.storage;

import java.io.IOException;
import java.util.List;

public interface ICloudService {

    void login();
    void logout();
    List<File> files() throws IOException;
    void move(File file, String desiredName) throws Exception;
    void upload(File file) throws Exception;
    void download(File file) throws Exception ;
    void download(File file, String localName) throws Exception ;
    void delete(File file) throws Exception ;
}
