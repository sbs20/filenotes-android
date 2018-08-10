package com.sbs20.androsync;

public interface IObserver {
    void update(Sync source, Action action);
}
