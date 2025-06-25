package org.extism.sdk.chicory.http;

public class ExtismHttpException extends RuntimeException {
    public ExtismHttpException(String message) {
        super(message);
    }

    public ExtismHttpException(Throwable cause) {
        super(cause);
    }
}
