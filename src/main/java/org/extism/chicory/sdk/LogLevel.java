package org.extism.chicory.sdk;

import com.dylibso.chicory.log.Logger;

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
}

