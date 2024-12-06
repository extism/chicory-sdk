package org.extism.chicory.sdk;

public class ExtismTypeConversionException extends ExtismException {

    public ExtismTypeConversionException(ExtismValType expected, ExtismValType given) {
        super(String.format("Illegal type conversion, wanted %s, given %s", expected.name(), given.name()));
    }

}
