package org.extism.chicory.sdk;

public class ExtismException extends RuntimeException{

    public ExtismException(String message) {
        super(message);
    }

    public ExtismException(Throwable cause) {
        super(cause);
    }
}
