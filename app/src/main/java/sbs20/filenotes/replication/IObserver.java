package sbs20.filenotes.replication;

public interface IObserver {
    void update(Replicator source, Action action);
}
