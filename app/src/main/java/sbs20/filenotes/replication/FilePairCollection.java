package sbs20.filenotes.replication;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sbs20.filenotes.storage.File;

class FilePairCollection implements Iterable<FilePair> {
    private List<FilePair> list;

    FilePairCollection() {
        list = new ArrayList<>();
    }

    FilePair findByKey(String key) {
        for (FilePair filePair : list) {
            if (key.equals(filePair.key())) {
                return  filePair;
            }
        }

        return null;
    }

    void add(File file) {
        String key = file.key();
        FilePair filePair = findByKey(key);

        if (filePair == null) {
            filePair = new FilePair();
            list.add(filePair);
        }

        if (file.isLocal()) {
            filePair.local = file;
        } else {
            filePair.remote = file;
        }
    }

    @Override
    public Iterator<FilePair> iterator() {
        return list.iterator();
    }
}
