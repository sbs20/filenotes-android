package com.sbs20.androsync;

public interface IObserver {
    void update(Replicator source, Action action);
}
