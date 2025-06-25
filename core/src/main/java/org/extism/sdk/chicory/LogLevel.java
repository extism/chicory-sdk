package org.extism.sdk.chicory;

import com.dylibso.chicory.log.Logger;

import java.util.logging.Level;

public enum LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR;

    Logger.Level toChicoryLogLevel() {
        switch (this) {
            case TRACE:
                return Logger.Level.TRACE;
            case DEBUG:
                return Logger.Level.DEBUG;
            case INFO:
                return Logger.Level.INFO;
            case WARN:
                return Logger.Level.WARNING;
            case ERROR:
                return Logger.Level.ERROR;
        }
        throw new IllegalArgumentException("unknown type " + this);
    }

    Level toJavaLogLevel() {
        switch (this) {
            case TRACE:
                return Level.FINEST;
            case DEBUG:
                return Level.FINE;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARNING;
            case ERROR:
                return Level.SEVERE;
        }
        throw new IllegalArgumentException("unknown type " + this);
    }

    public static LogLevel fromJavaLogLevel(Level level) {
        switch (level.getName()) {
            case "FINEST":
                return TRACE;
            case "FINE":
                return DEBUG;
            case "INFO":
                return INFO;
            case "WARNING":
                return WARN;
            case "SEVERE":
                return ERROR;
        }
        throw new IllegalArgumentException("unknown type " + level.getName());
    }
}

