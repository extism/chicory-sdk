package org.extism.chicory.sdk;

public class ExtismFunctionException extends ExtismException {

    private final String error;

    public ExtismFunctionException(String function, String message) {
        super(String.format("function %s returned an error: %s", function, message));
        this.error = message;
    }

    /**
     * Underlying error returned by the plugin call
     */
    public String getError() {
        return error;
    }

}
