package com.sbs20.androsync;

import android.content.Context;

import java.util.Date;

public interface ISettings {
    Date getLastSync();
    void setLastSync(Date date);
    Date getNextSync();
    void setNextSync(Date date);
    void clearLastSync();
    void clearNextSync();
    long replicationIntervalInMilliseconds();
    boolean replicationSkipError();
    boolean isReplicationOnChange();
    String getAuthToken();
    void setAuthToken(String authToken);
    void clearAuthToken();
}
