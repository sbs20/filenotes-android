package sbs20.filenotes.model;

import android.util.Log;

import sbs20.filenotes.ServiceManager;

public class Logger {

    private static final String VERBOSE = "Verbose";
    private static final String DEBUG = "Debug";
    private static final String INFORMATION = "Info";
    private static final String WARN = "Warn";
    private static final String ERROR = "Error";

    private ServiceManager serviceManager;

    private Logger() {
    }

    private static void log(String level, String tag, String msg) {
        // TODO - write somewhere better
        switch (level) {
            case ERROR:
                Log.e(tag, msg);
                break;
            case VERBOSE:
                Log.v(tag, msg);
                break;
            case INFORMATION:
                Log.i(tag, msg);
                break;
            case DEBUG:
                Log.d(tag, msg);
                break;
            case WARN:
                Log.w(tag, msg);
                break;
        }
    }

    public static void verbose(String tag, String msg) {
        log(VERBOSE, tag, msg);
    }
    public static void debug(String tag, String msg) {
        log(DEBUG, tag, msg);
    }
    public static void info(String tag, String msg) {
        log(INFORMATION, tag, msg);
    }
    public static void warn(String tag, String msg) {
        log(WARN, tag, msg);
    }
    public static void error(String tag, String msg) {
        log(ERROR, tag, msg);
    }

    public static void verbose(Object source, String msg) {
        log(VERBOSE, source.getClass().getName(), msg);
    }
    public static void debug(Object source, String msg) {
        log(DEBUG, source.getClass().getName(), msg);
    }
    public static void info(Object source, String msg) {
        log(INFORMATION, source.getClass().getName(), msg);
    }
    public static void warn(Object source, String msg) {
        log(WARN, source.getClass().getName(), msg);
    }
    public static void error(Object source, String msg) {
        log(ERROR, source.getClass().getName(), msg);
    }
}
